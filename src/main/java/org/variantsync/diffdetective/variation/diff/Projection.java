package org.variantsync.diffdetective.variation.diff;

import java.util.List;

import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.VariationLabel;
import org.variantsync.diffdetective.variation.tree.TreeNode;
import org.variantsync.functjonal.list.FilteredMappedListView;

/**
 * A view of a {@link DiffNode} as variation node at a specific time.
 *
 * <p>See the {@code project} function in section 3.1 of
 * <a href="https://github.com/SoftVarE-Group/Papers/raw/main/2022/2022-ESECFSE-Bittner.pdf">
 * our paper</a>.
 *
 * <p>This class has to be instantiated using {@link DiffNode#projection}.
 *
 * @see DiffNode#projection
 */
public class Projection<L extends Label> extends TreeNode<Projection<L>, VariationLabel<L>> {
    private DiffNode<L> backingNode;
    private Time time;

    /**
     * Creates a new projection of a {@link DiffNode}.
     * Only {@link DiffNode} is allowed to call this method to guarantee the identity of this class
     * (see above for details). If you want to get the projection of a {@link DiffNode} use
     * {@link DiffNode#projection}.
     *
     * @param backingNode the {@link DiffNode} which should be projected
     * @param time which projection this should be
     */
    Projection(DiffNode<L> backingNode, Time time) {
        this.backingNode = backingNode;
        this.time = time;
    }

    public Time getTime() {
        return time;
    }

    public DiffNode<L> getBackingNode() {
        return backingNode;
    }

    @Override
    public Projection<L> upCast() {
        return this;
    }

    @Override
    public VariationLabel<L> getLabel() {
        return getBackingNode().getVariationLabel();
    }

    @Override
    public Projection<L> getParent() {
        var parent = getBackingNode().getParent(time);

        if (parent == null) {
            return null;
        } else {
            return parent.projection(time);
        }
    }


    @Override
    public List<Projection<L>> getChildren() {
        return FilteredMappedListView.map(
            getBackingNode().getChildOrder(time),
            child -> child.projection(time)
        );
    }

    @Override
    public void addChild(final Projection<L> child) {
        getBackingNode().addChild(child.getBackingNode(), time);
    }

    @Override
    public void insertChild(final Projection<L> child, int index) {
        getBackingNode().insertChild(child.getBackingNode(), index, time);
    }

    @Override
    public void removeChild(final Projection<L> child) {
        getBackingNode().removeChild(child.getBackingNode(), time);
    }

    @Override
    public void removeAllChildren() {
        getBackingNode().removeChildren(time);
    }

    @Override
    public int getID() {
        return getBackingNode().getID();
    }
};
