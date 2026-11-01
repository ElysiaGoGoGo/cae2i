package top.purpletears.cae2i;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;
import top.purpletears.cae2i.ae2.ProcessingPatternEncoder;
import top.purpletears.cae2i.create.ClipboardMaterialReader;
import top.purpletears.cae2i.output.PatternCounter;
import top.purpletears.cae2i.output.PatternCounterSavedData;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * One hand holds a blank pattern, the other holds a Create clipboard; right-clicking
 * writes the material list into an AE2 processing pattern.
 *
 * <h2>Why both interaction events must be handled</h2>
 * In the client's hand loop ({@code Minecraft.startUseItem}), aiming at a block takes the
 * {@code case BLOCK} branch first:
 * <pre>
 *   InteractionResult useItemOn = gameMode.useItemOn(player, hand, hitResult);
 *   if (useItemOn.consumesAction()) { ...; return; }   // never reaches gameMode.useItem below
 * </pre>
 * So whenever {@code useItemOn} consumes the click, {@code gameMode.useItem(...)} is
 * <b>never called</b>: the client never sends {@code ServerboundUseItemPacket} and the
 * server-side {@link PlayerInteractEvent.RightClickItem} never fires. The symptom is a
 * dead right-click whose behaviour depends on what the player happens to be aiming at.
 *
 * <p>Handling {@link PlayerInteractEvent.RightClickBlock} as well closes that gap:
 * <ul>
 *   <li>aiming at a block: RightClickBlock matches and is cancelled, so {@code useItemOn}
 *       returns SUCCESS, {@code startUseItem} returns immediately and RightClickItem is
 *       never reached (so a single click can never convert twice);</li>
 *   <li>aiming at air: RightClickBlock does not fire and RightClickItem matches.</li>
 * </ul>
 *
 * <p>Cancelling does not swallow the request: both client entry points send their packet
 * unconditionally through {@code MultiPlayerGameMode.startPrediction}
 * ({@code useItemOn:306-309}, {@code useItem:380-408}), so the server always receives it.
 */
public class PatternWritingHandler {

    private final ClipboardPatternService service;
    private final ClipboardMaterialReader reader;

    /**
     * Belt-and-braces de-duplication: one click must only ever consume one blank pattern.
     * Keyed by the player instance so stale entries disappear with the player.
     */
    private final Map<Player, Integer> lastConvertTick = new WeakHashMap<>();

    public PatternWritingHandler(ClipboardPatternService service, ClipboardMaterialReader reader) {
        this.service = service;
        this.reader = reader;
    }

    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (process(event.getEntity(), event.getLevel())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (process(event.getEntity(), event.getLevel())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    /**
     * Detects the blank-pattern + clipboard combination and, on the server only, performs
     * the conversion.
     *
     * @return whether the combination matched; the caller must then cancel the event.
     */
    private boolean process(Player player, Level level) {
        InteractionHand blankHand = findBlankHand(player);
        if (blankHand == null) {
            return false;
        }

        // Both sides run the cheap, side-independent detection so the client knows to
        // cancel the interaction; only the server actually writes anything.
        if (level.isClientSide()) {
            return true;
        }

        int tick = player.tickCount;
        Integer previous = this.lastConvertTick.put(player, tick);
        if (previous != null && previous == tick) {
            return true; // already converted for this click
        }

        convert(player, (ServerLevel) level, blankHand);
        return true;
    }

    @Nullable
    private InteractionHand findBlankHand(Player player) {
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();

        if (ProcessingPatternEncoder.isBlankPattern(main) && this.reader.isClipboard(off)) {
            return InteractionHand.MAIN_HAND;
        }
        if (ProcessingPatternEncoder.isBlankPattern(off) && this.reader.isClipboard(main)) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }

    private void convert(Player player, ServerLevel level, InteractionHand blankHand) {
        InteractionHand clipboardHand = blankHand == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
        ItemStack clipboard = player.getItemInHand(clipboardHand);

        PatternCounter counter = PatternCounterSavedData.get(level);
        PatternWriteResult result = this.service.write(clipboard, counter);

        if (result.success()) {
            ItemStack blank = player.getItemInHand(blankHand);
            blank.shrink(1);
            if (blank.isEmpty()) {
                player.setItemInHand(blankHand, result.pattern());
            } else if (!player.getInventory().add(result.pattern())) {
                player.drop(result.pattern(), false);
            }
            ClipboardAE2Integration.LOGGER.debug("cae2i: wrote processing pattern for {}",
                    player.getGameProfile().getName());
        }

        if (result.message() != null) {
            player.sendSystemMessage(result.message());
        }
    }
}
