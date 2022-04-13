package org.variantsync.diffdetective.diff.difftree;

public interface DiffTreeSource {
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
