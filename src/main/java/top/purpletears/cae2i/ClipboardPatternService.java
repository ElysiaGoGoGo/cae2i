package top.purpletears.cae2i;

import appeng.api.stacks.GenericStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import top.purpletears.cae2i.ae2.ProcessingPatternEncoder;
import top.purpletears.cae2i.create.ClipboardMaterialReader;
import top.purpletears.cae2i.output.PatternCounter;
import top.purpletears.cae2i.output.PatternOutputFactory;

import java.util.List;

/**
 * Orchestrates a single conversion: read the Create material list, build the unique
 * output receipt, and encode an AE2 processing pattern. Does not depend on any Level,
 * so it can be unit-tested without a running game.
 */
public class ClipboardPatternService {

    private final ClipboardMaterialReader reader;
    private final ProcessingPatternEncoder encoder;
    private final PatternOutputFactory outputFactory;

    public ClipboardPatternService(ClipboardMaterialReader reader, ProcessingPatternEncoder encoder,
            PatternOutputFactory outputFactory) {
        this.reader = reader;
        this.encoder = encoder;
        this.outputFactory = outputFactory;
    }

    public PatternWriteResult write(ItemStack clipboard, PatternCounter counter) {
        List<GenericStack> inputs = reader.readInputs(clipboard);
        if (inputs.isEmpty()) {
            return PatternWriteResult.failure(Component.translatableWithFallback("chat.cae2i.EmptyMaterials", "The clipboard contains no usable material list"));
        }
        // NOTE: 81-input上限检查已按要求注释掉，允许生成超过 81 种输入的处理样板。
        // if (inputs.size() > ProcessingPatternEncoder.MAX_INPUTS) {
        //     return PatternWriteResult.failure(Component.translatableWithFallback("chat.cae2i.TooManyInputs", "Too many material types (max 81) for a processing pattern"));
        // }

        GenericStack output = outputFactory.nextOutput(counter);
        ItemStack pattern = encoder.encode(inputs, List.of(output));
        return PatternWriteResult.success(pattern);
    }
}
