package top.purpletears.cae2i.output;

/**
 * Provides monotonically increasing ids used to make each generated output unique.
 */
public interface PatternCounter {
    int nextId();
}
