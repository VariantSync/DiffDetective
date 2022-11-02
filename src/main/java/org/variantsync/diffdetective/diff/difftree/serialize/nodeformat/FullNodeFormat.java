package org.variantsync.diffdetective.diff.difftree.serialize.nodeformat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.variantsync.diffdetective.diff.difftree.DiffNode;

/**
 * Labels containing all information encoded into a {@link DiffNode}.
 *
 * The format consists of the following lines:
 * <code>
 * diffType
 * nodeType
 * fromLine
 * toLine
 * directFeatureMapping
 * label
 * </code>.
 *
 * @author Benjamin Moosherr
 */
public class FullNodeFormat implements DiffNodeLabelFormat {
    @Override
    public List<String> toMultilineLabel(final DiffNode node) {
        List<String> lines = new ArrayList<>();

        lines.add(node.diffType.toString());
        lines.add(node.nodeType.toString());
        lines.add(node.getFromLine().toString());
        lines.add(node.getToLine().toString());
        lines.add(node.getDirectFeatureMapping() == null ? "" : node.getDirectFeatureMapping().toString());
        lines.addAll(node.getLines());

        return lines;
    }

    @Override
    public String toLabel(final DiffNode node) {
        return toMultilineLabel(node)
            .stream()
            .collect(Collectors.joining(";"));
    }
}
