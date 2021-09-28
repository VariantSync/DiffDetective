package diff;

import diff.difftree.DiffType;

import java.util.Objects;

/**
 * Identifies a line number in a textual diff holds its the numbers of the
 * corresponding line before and after the edit.
 */
public class DiffLineNumber {
    public static final int InvalidLineNumber = -1;
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
        return new DiffLineNumber(InvalidLineNumber, InvalidLineNumber, InvalidLineNumber);
    }

    public static DiffLineNumber Copy(final DiffLineNumber other) {
        return new DiffLineNumber(other.inDiff, other.beforeEdit, other.afterEdit);
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

    public void as(final DiffType diffType) {
        if (diffType == DiffType.ADD) {
            beforeEdit = InvalidLineNumber;
        } else if (diffType == DiffType.REM) {
            afterEdit = InvalidLineNumber;
        }
    }

    @Override
    public String toString() {
        return "(old: " + beforeEdit + ", diff: " + inDiff + ", new:" + afterEdit + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiffLineNumber that = (DiffLineNumber) o;
        return inDiff == that.inDiff && beforeEdit == that.beforeEdit && afterEdit == that.afterEdit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(inDiff, beforeEdit, afterEdit);
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
