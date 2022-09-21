package org.variantsync.diffdetective.diff.difftree.parse;

import org.prop4j.Node;
import org.variantsync.diffdetective.diff.DiffLineNumber;
import org.variantsync.diffdetective.diff.difftree.NodeType;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.diffdetective.feature.CPPAnnotationParser;

import java.util.ArrayList;

/**
 * A parser that parses a {@link DiffNode}s from a line in a text-based diff.
 * @param annotationParser The parser to use for parsing feature annotations.
 */
public record DiffNodeParser(CPPAnnotationParser annotationParser) {
    /**
     * The default node parser that uses {@link CPPAnnotationParser#Default}.
     */
    public static final DiffNodeParser Default = new DiffNodeParser(CPPAnnotationParser.Default);

    /**
     * Parses the given line from a text-based diff to a DiffNode.
     *
     * @param diffLine The line which the new node represents.
     * @return A DiffNode with a node type, diff type, label, and feature mapping.
     */
    public DiffNode fromDiffLine(String diffLine) throws IllFormedAnnotationException {
        DiffType diffType = DiffType.ofDiffLine(diffLine);
        NodeType nodeType = NodeType.ofDiffLine(diffLine);
        String label = diffLine.isEmpty() ? diffLine : diffLine.substring(1);
        Node featureMapping;

        if (nodeType == NodeType.ARTIFACT || nodeType == NodeType.ELSE) {
            featureMapping = null;
        } else {
            featureMapping = annotationParser.parseDiffLine(diffLine);
        }

        ArrayList<String> lines = new ArrayList<>();
        lines.add(label);
        return new DiffNode(
                diffType, nodeType,
                DiffLineNumber.Invalid(), DiffLineNumber.Invalid(),
                featureMapping,
                lines);
    }
}
