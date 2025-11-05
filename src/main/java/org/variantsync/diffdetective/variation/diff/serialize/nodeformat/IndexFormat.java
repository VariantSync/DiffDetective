package org.variantsync.diffdetective.variation.diff.serialize.nodeformat;

import java.util.ArrayList;
import java.util.List;

import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.functjonal.Cast;

/**
 * Labels nodes using their index in their parent list at all times.
 */
public class IndexFormat<L extends Label> implements DiffNodeLabelFormat<L> {
    @Override
    public String toLabel(final DiffNode<? extends L> n) {
        final DiffNode<L> node = Cast.unchecked(n);
        final Time[] allTimes = Time.values();
        final List<String> values = new ArrayList<>(allTimes.length);
        for (final Time t : allTimes) {
            values.add(node.getDiffType().existsAtTime(t) && !node.isRoot()
                ? Integer.toString(node.getParent(t).indexOfChild(node, t))
                : "_");
        }
        return "(" + String.join(", ", values) + ")";
    }
}
