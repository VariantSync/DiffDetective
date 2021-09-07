package diff;

/**
 * Identifies a line number in a textual diff holds its the numbers of the
 * corresponding line before and after the edit.
 */
public class DiffLineNumber {
    public int inDiff, beforeEdit, afterEdit;

    /**
     * Creates a line number of a diff.
     * @param inDiff line number in the diff
     * @param beforeEdit number of corresponding line before the edit
     * @param afterEdit number of corresponding line after the edit
     */
    public DiffLineNumber(int inDiff, int beforeEdit, int afterEdit) {
        this.inDiff = inDiff;
        this.beforeEdit = beforeEdit;
        this.afterEdit = afterEdit;
    }

    public static DiffLineNumber Invalid() {
        return new DiffLineNumber(-1, -1, -1);
    }

    public DiffLineNumber set(final DiffLineNumber other) {
        this.inDiff = other.inDiff;
        this.beforeEdit = other.beforeEdit;
        this.afterEdit = other.afterEdit;
        return this;
    }

    public DiffLineNumber add(int offset) {
        this.inDiff += offset;
        this.beforeEdit += offset;
        this.afterEdit += offset;
        return this;
    }

    @Override
    public String toString() {
        return '{' + beforeEdit + " -> " + inDiff + " -> " + afterEdit + '}';
    }

    public static Lines rangeInDiff(final DiffLineNumber from, final DiffLineNumber to) {
        return Lines.FromInclToExcl(from.inDiff, to.inDiff);
    }

    public static Lines rangeBeforeEdit(final DiffLineNumber from, final DiffLineNumber to) {
        return Lines.FromInclToExcl(from.beforeEdit, to.beforeEdit);
    }

    public static Lines rangeAfterEdit(final DiffLineNumber from, final DiffLineNumber to) {
        return Lines.FromInclToExcl(from.afterEdit, to.afterEdit);
    }
}
