package top.purpletears.cae2i.create;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * The only place that touches Create's clipboard API. Converts a written material
 * checklist into AE2 inputs ({@link GenericStack}), encoding Create's layout rules:
 * the icon's count is always 1, the real amount lives in {@code itemAmount}.
 */
public class ClipboardMaterialReader {

    public boolean isClipboard(ItemStack stack) {
        return stack.is(AllBlocks.CLIPBOARD.asItem());
    }

    public List<GenericStack> readInputs(ItemStack clipboard) {
        List<GenericStack> inputs = new ArrayList<>();
        for (List<ClipboardEntry> page : ClipboardEntry.readAll(clipboard)) {
            for (ClipboardEntry entry : page) {
                if (entry.checked) continue;          // already collected -> skip
                if (entry.icon.isEmpty()) continue;   // text-only note (blocksNotLoaded etc.) -> skip
                if (entry.itemAmount <= 0) continue;  // zero/negative amount -> skip
                inputs.add(new GenericStack(AEItemKey.of(entry.icon), entry.itemAmount));
            }
        }
        return inputs;
    }
}
