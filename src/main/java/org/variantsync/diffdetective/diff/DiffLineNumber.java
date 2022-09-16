package org.variantsync.diffdetective.diff;

import org.variantsync.diffdetective.diff.difftree.DiffType;

import java.util.Objects;

/**
 * Identifies a line number in a textual diff.
 * A DiffLineNumber consists of
 * - the line number in the diff,
 * - the corresponding line number before the edit
 * - the corresponding line number after the edit
 * @author Paul Bittner
 */
public class DiffLineNumber {
    /**
     * Index for invalid line numbers.
     */
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

    /**
     * Creates an invalid line number using {@link DiffLineNumber#InvalidLineNumber}.
     * Use this go obtain a line number that represents an invalid state.
     * @return An invalid line number.
     */
    public static DiffLineNumber Invalid() {
        return new DiffLineNumber(InvalidLineNumber, InvalidLineNumber, InvalidLineNumber);
    }

    /**
     * Creates a copy of the given DiffLineNumber.
     */
    public static DiffLineNumber Copy(final DiffLineNumber other) {
        return new DiffLineNumber(other.inDiff, other.beforeEdit, other.afterEdit);
    }

    /**
     * Make this line number become a copy of the given line number.
     * @param other Number to copy. Remains unchanged.
     * @return this
     */
    public DiffLineNumber set(final DiffLineNumber other) {
        this.inDiff = other.inDiff;
        this.beforeEdit = other.beforeEdit;
        this.afterEdit = other.afterEdit;
        return this;
    }

    /**
     * Shifts this line number by adding the given offset.
     * @param offset Value to add to this line number.
     * @return this
     */
    public DiffLineNumber add(int offset) {
        this.inDiff += offset;
        this.beforeEdit += offset;
        this.afterEdit += offset;
        return this;
    }

    /**
     * Filters this line number to only represent line numbers for the given diff type.
     * In particular, added artifacts do not have a line number before the edit
     * and removed artifacts do not have a line number after the edit.
     * Non-existing values will be set to {@link DiffLineNumber#InvalidLineNumber}.
     * @param diffType The diff type according to which this line number should be filtered.
     */
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

    /**
     * Returns the range between two line numbers in the diff.
     * @see DiffLineNumber#inDiff
     * @param from The start line number.
     * @param to The end line number.
     * @return [from.inDiff, to.inDiff)
     */
    public static Lines rangeInDiff(final DiffLineNumber from, final DiffLineNumber to) {
        return Lines.FromInclToExcl(from.inDiff, to.inDiff);
    }

    /**
     * Returns the range between two line numbers before the edit.
     * @see DiffLineNumber#inDiff
     * @param from The start line number.
     * @param to The end line number.
     * @return [from.beforeEdit, to.beforeEdit)
     */
    public static Lines rangeBeforeEdit(final DiffLineNumber from, final DiffLineNumber to) {
        return Lines.FromInclToExcl(from.beforeEdit, to.beforeEdit);
    }

    /**
     * Returns the range between two line numbers before the edit.
     * @see DiffLineNumber#inDiff
     * @param from The start line number.
     * @param to The end line number.
     * @return [from.afterEdit, to.afterEdit)
     */
    public static Lines rangeAfterEdit(final DiffLineNumber from, final DiffLineNumber to) {
        return Lines.FromInclToExcl(from.afterEdit, to.afterEdit);
    }
}
