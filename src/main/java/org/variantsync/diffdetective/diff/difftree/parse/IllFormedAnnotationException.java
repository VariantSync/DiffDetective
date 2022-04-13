package org.variantsync.diffdetective.diff.difftree.parse;

import org.variantsync.diffdetective.diff.result.DiffError;

public class IllFormedAnnotationException extends Exception {
    private final DiffError errorType;
    private IllFormedAnnotationException(DiffError errorType, String msg) {
        super(msg);
        this.errorType = errorType;
    }

    public static IllFormedAnnotationException IfWithoutCondition(String message) {
        return new IllFormedAnnotationException(DiffError.IF_WITHOUT_CONDITION, message);
    }

    public DiffError getType() {
        return errorType;
    }
}
