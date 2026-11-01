package top.purpletears.cae2i;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

import top.purpletears.cae2i.ae2.ProcessingPatternEncoder;
import top.purpletears.cae2i.create.ClipboardMaterialReader;
import top.purpletears.cae2i.output.PatternOutputFactory;

/**
 * Mod entry point.
 *
 * <p>The mod has exactly one feature: turning a Create clipboard's material list, together with an
 * AE2 blank pattern, into an encoded processing pattern. That feature only happens through a
 * registered {@link PatternWritingHandler}; nothing else in this mod registers itself, so these
 * are the lines that make the feature exist at all.
 */
@Mod(ClipboardAE2Integration.MODID)
public class ClipboardAE2Integration {
    public static final String MODID = "cae2i";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ClipboardAE2Integration(IEventBus modEventBus, ModContainer modContainer) {
        // Feature: clipboard -> AE2 processing pattern.
        // The readers/encoders are stateless helpers; the service glues them together.
        ClipboardMaterialReader reader = new ClipboardMaterialReader();
        ProcessingPatternEncoder encoder = new ProcessingPatternEncoder();
        PatternOutputFactory outputFactory = new PatternOutputFactory();
        ClipboardPatternService service = new ClipboardPatternService(reader, encoder, outputFactory);

        // PlayerInteractEvent is a game event, so the handler belongs on the NeoForge bus,
        // not on the mod event bus.
        NeoForge.EVENT_BUS.register(new PatternWritingHandler(service, reader));

        LOGGER.info("Clipboard AE2 Integration loaded");
    }
}
