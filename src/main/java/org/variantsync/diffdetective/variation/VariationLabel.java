package org.variantsync.diffdetective.variation;

import java.util.List;

import org.variantsync.diffdetective.variation.tree.VariationTree; // For Javadoc
import org.variantsync.functjonal.Cast;

/**
 * Extends an inner {@link Label}, the object language, with variability, the meta language.
 *
 * This class allows encoding of multiple levels of variability in the sense of variation trees of
 * variation trees. Such higher-order variation trees can be encoded in a single tree structure
 * because the result of configuring a variation tree is still a tree. In other words: the object
 * language of a {@code VariationTree<L>} is a tree with labels {@code L}. The trick is that a
 * variation tree is just a tree with labels {@code VariationLabel<L>}.
 *
 * @param <L> the label of the trees (the object language) over which variability is introduced
 * @see VariationTree
 */
public class VariationLabel<L extends Label> implements Label, HasNodeType {
    private NodeType type;
    private L innerLabel;

    public VariationLabel(NodeType type, L innerLabel) {
        this.type = type;
        this.innerLabel = innerLabel;
    }

    public L getInnerLabel() {
        return innerLabel;
    }

    public void setInnerLabel(L innerLabel) {
        this.innerLabel = innerLabel;
    }

    @Override
    public List<String> getLines() {
        return innerLabel.getLines();
    }

    @Override
    public NodeType getNodeType() {
        return type;
    }

    @Override
    public VariationLabel<L> clone() {
        return new VariationLabel<L>(type, Cast.unchecked(innerLabel.clone()));
    }
}
