package datasets;

public record DebugOptions(DiffStoragePolicy diffStoragePolicy) {
    public enum DiffStoragePolicy {
        REMEMBER_DIFF,
        REMEMBER_STRIPPED_DIFF,
        REMEMBER_FULL_DIFF,
        DO_NOT_REMEMBER,
    }

    public static final DebugOptions DEFAULT = new DebugOptions(DiffStoragePolicy.DO_NOT_REMEMBER);
}
