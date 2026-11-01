package top.purpletears.cae2i.output;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Produces the single output of every generated pattern: a writable book receipt numbered by the
 * counter. The custom name is part of the AE item key, so different receipts are different outputs
 * in the ME network.
 *
 * <p>The name is a translatable component rather than a literal. That is safe for the key: equality
 * and hashing look at the translation key and its arguments, not at the rendered text, so two
 * players on different languages still agree that two receipts are the same item. Only the display
 * differs — which is the point.
 */
public class PatternOutputFactory {

    /** {@code %s} is the receipt number. */
    private static final String NAME_KEY = "item.cae2i.material_list";
    private static final String NAME_FALLBACK = "Material List %s";

    public GenericStack nextOutput(PatternCounter counter) {
        int id = counter.nextId();
        ItemStack book = new ItemStack(Items.WRITABLE_BOOK);
        book.set(DataComponents.CUSTOM_NAME,
                Component.translatableWithFallback(NAME_KEY, NAME_FALLBACK, id));
        return new GenericStack(AEItemKey.of(book), 1);
    }
}
