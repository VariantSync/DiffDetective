package org.variantsync.diffdetective.variation.diff.serialize.nodeformat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.DiffNode;

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
public class FullNodeFormat implements DiffNodeLabelFormat<DiffLinesLabel> {
    @Override
    public List<String> toMultilineLabel(final DiffNode<? extends DiffLinesLabel> node) {
        List<String> lines = new ArrayList<>();

        lines.add(node.diffType.toString());
        lines.add(node.getNodeType().toString());
        lines.add(node.getFromLine().toString());
        lines.add(node.getToLine().toString());
        lines.add(node.getFormula() == null ? "" : node.getFormula().toString());
        lines.addAll(node.getLabel().getLines());

        return lines;
    }

    @Override
    public String toLabel(final DiffNode<? extends DiffLinesLabel> node) {
        return toMultilineLabel(node)
            .stream()
            .collect(Collectors.joining(";"));
    }
}
