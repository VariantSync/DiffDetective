package org.variantsync.diffdetective.diff.result;

/**
 * Describes an error that occurred when processing a text-based diff.
 * @param id error message
 */
public record DiffError(String id) {
    /**
     * A commit has no parents and thus no real diff.
     */
    public final static DiffError COMMIT_HAS_NO_PARENTS = new DiffError("commit has no parents");

    /**
     * Some internal error occurred when operating JGit.
     * The error is not further specified or unknown.
     */
    public final static DiffError JGIT_ERROR = new DiffError("error when operating jgit");

    /**
     * An error which occurred when obtaining the full diff from a local diff.
     */
    public final static DiffError COULD_NOT_OBTAIN_FULLDIFF = new DiffError("could not obtain full diff");

    /**
     * A preprocessor block was opened but not closed.
     * Typically, this occurs when an #endif is missing.
     */
    public final static DiffError NOT_ALL_ANNOTATIONS_CLOSED = new DiffError("not all annotations closed");

    /**
     * A file or patch contained an expression that closes an annotation block (typically an #endif)
     * but there is no block to close.
     */
    public final static DiffError ENDIF_WITHOUT_IF = new DiffError("#endif without #if");

    /**
     * A multiline macro was defined within a multiline macro.
     */
    public final static DiffError MLMACRO_WITHIN_MLMACRO = new DiffError("definition of multiline macro within multiline macro");

    /**
     * An #else or #elif expression has no corresponding #if (or #ifdef, ...) expression.
     */
    public final static DiffError ELSE_OR_ELIF_WITHOUT_IF = new DiffError("#else or #elif without #if");

    /**
     * An #else expression is followed by another #else expression which has no semantics.
     */
    public final static DiffError ELSE_AFTER_ELSE = new DiffError("#else after #else");

    /**
     * A condition annotation is missing an expression.
     * This typically occurs when an #if macro has no arguments.
     */
    public final static DiffError IF_WITHOUT_CONDITION = new DiffError("conditional macro without expression");

    @Override
    public String toString() {
        return id;
    }
}
