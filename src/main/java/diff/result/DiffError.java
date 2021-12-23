package diff.result;

import java.util.Objects;

public record DiffError(String id) {
    public final static DiffError COMMIT_HAS_NO_PARENTS = new DiffError("commit has no parents");
    public final static DiffError JGIT_ERROR = new DiffError("error when operating jgit");
    public final static DiffError COULD_NOT_OBTAIN_FULLDIFF = new DiffError("could not obtain full diff");
    public final static DiffError NOT_ALL_ANNOTATIONS_CLOSED = new DiffError("not all annotations closed");
    public final static DiffError ENDIF_WITHOUT_IF = new DiffError("#endif without #if");
    public final static DiffError MLMACRO_WITHIN_MLMACRO = new DiffError("definition of multiline macro within multiline macro");
    public final static DiffError ELSE_OR_ELIF_WITHOUT_IF = new DiffError("#else or #elif without #if");
    public final static DiffError IF_WITHOUT_CONDITION = new DiffError("conditional macro without expression");

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiffError diffError = (DiffError) o;
        return Objects.equals(id, diffError.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return id;
    }
}
