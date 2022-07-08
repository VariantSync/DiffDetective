package org.variantsync.diffdetective.diff.difftree;

/**
 * Describes or identifies that data a DiffTree was created or parsed from.
 * This is typically a patch.
 */
public interface DiffTreeSource {
    /**
     * Constant to use when the source of a DiffTree is unknown
     * or if it was created artifically.
     */
    DiffTreeSource Unknown = new DiffTreeSource() {
        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public String toString() {
            return "Unknown DiffTreeSource";
        }
    };
}
