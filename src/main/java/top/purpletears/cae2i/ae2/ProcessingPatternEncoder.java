package top.purpletears.cae2i.ae2;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.GenericStack;
import appeng.core.definitions.AEItems;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * The only place that touches AE2's processing-pattern API. Wraps
 * {@link PatternDetailsHelper#encodeProcessingPattern} and identifies blank patterns.
 */
public class ProcessingPatternEncoder {

    /** Matches {@code AEProcessingPattern.MAX_INPUT_SLOTS} (9 * 9). */
    public static final int MAX_INPUTS = 9 * 9;

    public static boolean isBlankPattern(ItemStack stack) {
        return AEItems.BLANK_PATTERN.is(stack);
    }

    public ItemStack encode(List<GenericStack> inputs, List<GenericStack> outputs) {
        if (inputs.isEmpty()) {
            throw new IllegalArgumentException("Cannot encode a processing pattern without inputs");
        }
        return PatternDetailsHelper.encodeProcessingPattern(inputs, outputs);
    }
}
