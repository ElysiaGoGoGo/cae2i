package top.purpletears.cae2i.output;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Persists the receipt counter in the overworld save data so numbering survives
 * restarts and stays unique across the whole world.
 */
public class PatternCounterSavedData extends SavedData implements PatternCounter {

    private int counter;

    public PatternCounterSavedData() {
        this(0);
    }

    public PatternCounterSavedData(int counter) {
        this.counter = counter;
    }

    @Override
    public int nextId() {
        setDirty();
        return counter++;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("counter", counter);
        return tag;
    }

    private static PatternCounterSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        return new PatternCounterSavedData(tag.getInt("counter"));
    }

    public static PatternCounterSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                new Factory<>(PatternCounterSavedData::new, PatternCounterSavedData::load),
                "cae2i_pattern_counter");
    }
}
