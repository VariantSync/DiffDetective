package diff.difftree;

import java.util.function.Consumer;

public enum DiffType {
    ADD("+"),
    REM("-"),
    NON(" ");

    public final String name;

    DiffType(String name) {
        this.name = name;
    }

    final static String addCharacter = "+";
    final static String remCharacter = "-";

    /**
     * Runs the first given procedure if the edited artefact existed before the edit (DiffType != ADD).
     * Runs the second given procedure if the edited artefact exists after the edit (DiffType != REM).
     * Note: Runs both procedures sequentially if the artefact was not edited and thus
     *       exists before and after the edit (DiffType = NON).
     * @param ifExistsBefore Procedure to run if the edited artefact existed before the edit (DiffType != ADD).
     * @param ifExistsAfter Procedure to run if the edited artefact exists after the edit (DiffType != REM).
     */
    public void matchBeforeAfter(final Runnable ifExistsBefore, final Runnable ifExistsAfter) {
        if (this != DiffType.ADD) {
            ifExistsBefore.run();
        }
        if (this != DiffType.REM) {
            ifExistsAfter.run();
        }
    }

    /**
     * Runs the given task once for each argument that is valid w.r.t. the lifetime of the artefact.
     * Runs task on ifExistsBefore if the edited artefact existed before the edit (DiffType != ADD).
     * Runs task on ifExistsAfter if the edited artefact exists after the edit (DiffType != ADD).
     * Note: Runs task on both arguments sequentially if the artefact was not edited (DiffType == NON).
     *
     * @param ifExistsBefore Argument that is valid if the diff did not add.
     * @param ifExistsAfter Argument that is valid if the edit did not remove.
     * @param task Task to run with all given arguments that are valid w.r.t. to this DiffType's lifetime.
     */
    public <T> void matchBeforeAfter(final T ifExistsBefore, final T ifExistsAfter, final Consumer<T> task) {
        if (this != DiffType.ADD) {
            task.accept(ifExistsBefore);
        }
        if (this != DiffType.REM) {
            task.accept(ifExistsAfter);
        }
    }

    public static DiffType ofDiffLine(String line) {
        if (line.startsWith(addCharacter)) {
            return ADD;
        } else if (line.startsWith(remCharacter)) {
            return REM;
        } else {
            return NON;
        }
    }
}
