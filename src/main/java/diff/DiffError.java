package diff;

public record DiffError(String id) {
    public final static DiffError COMMIT_HAS_NO_PARENTS = new DiffError("commit has no parents");
    public final static DiffError JGIT_ERROR = new DiffError("error when operating jgit");
    public final static DiffError COULD_NOT_OBTAIN_FULLDIFF = new DiffError("could not obtain full diff");
    public final static DiffError NOT_ALL_ANNOTATIONS_CLOSED = new DiffError("Not all annotations closed!");
    public final static DiffError ENDIF_WITHOUT_IF = new DiffError("ENDIF without IF");
    public final static DiffError MLMACRO_WITHIN_MLMACRO = new DiffError("Definition of multiline macro within multiline macro");
    public final static DiffError ELSE_OR_ELIF_WITHOUT_IF = new DiffError("#else or #elif without if");

    public static DiffError from(Exception e) {
        return new DiffError(e.toString());
    }
}
