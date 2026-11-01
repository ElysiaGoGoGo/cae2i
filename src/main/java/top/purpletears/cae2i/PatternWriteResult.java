package top.purpletears.cae2i;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Outcome of one clipboard-to-pattern conversion.
 * On success, {@link #pattern()} holds the encoded processing pattern and {@link #message()} is null.
 * On failure, {@link #pattern()} is null and {@link #message()} explains why.
 */
public record PatternWriteResult(boolean success, ItemStack pattern, Component message) {

    public static PatternWriteResult success(ItemStack pattern) {
        return new PatternWriteResult(true, pattern, null);
    }

    public static PatternWriteResult failure(Component message) {
        return new PatternWriteResult(false, null, message);
    }
}
