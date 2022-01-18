package diff.difftree.parse;

import diff.DiffLineNumber;
import diff.difftree.CodeType;
import diff.difftree.DiffNode;
import diff.difftree.DiffType;
import feature.CPPAnnotationParser;
import org.prop4j.Node;

public record DiffNodeParser(CPPAnnotationParser annotationParser) {
    public static final DiffNodeParser Default = new DiffNodeParser(CPPAnnotationParser.Default);

    /**
     * Creates a DiffNode from a line and two parents
     *
     * @param diffLine The line which the new node corresponds to.
     * @return A DiffNode with a code type, diff type, and feature mapping.
     */
    public DiffNode fromDiffLine(String diffLine) throws IllFormedAnnotationException {
        DiffType diffType = DiffType.ofDiffLine(diffLine);
        CodeType codeType = CodeType.ofDiffLine(diffLine);
        String label = diffLine.isEmpty() ? diffLine : diffLine.substring(1);
        Node featureMapping;

        if (codeType == CodeType.CODE || codeType == CodeType.ENDIF || codeType == CodeType.ELSE) {
            featureMapping = null;
        } else {
            featureMapping = annotationParser.parseDiffLine(diffLine);
        }

        return new DiffNode(
                diffType, codeType,
                DiffLineNumber.Invalid(), DiffLineNumber.Invalid(),
                featureMapping,
                label);
    }
}
