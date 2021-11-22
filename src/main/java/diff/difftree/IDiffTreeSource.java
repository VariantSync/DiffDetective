package diff.difftree;

public interface IDiffTreeSource {
    IDiffTreeSource Unknown = new IDiffTreeSource() {
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
