package org.variantsync.diffdetective.variation.diff;

import java.util.List;
import java.util.Optional;

import org.prop4j.Node;
import org.variantsync.diffdetective.util.LineRange;
import org.variantsync.diffdetective.variation.NodeType;
import org.variantsync.diffdetective.variation.tree.VariationNode;
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
public class Projection extends VariationNode<Projection> {
    private DiffNode backingNode;
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
    Projection(DiffNode backingNode, Time time) {
        this.backingNode = backingNode;
        this.time = time;
    }

    public Time getTime() {
        return time;
    }

    public DiffNode getBackingNode() {
        return backingNode;
    }

    @Override
    public Projection upCast() {
        return this;
    }


    @Override
    public NodeType getNodeType() {
        return getBackingNode().nodeType;
    }

    @Override
    public Label getLabel() {
        return getBackingNode().getLabel();
    }

    @Override
    public LineRange getLineRange() {
        return getBackingNode().getLinesAtTime(time);
    }

    @Override
    public void setLineRange(LineRange lineRange) {
        getBackingNode().setLinesAtTime(lineRange, time);
    }

    @Override
    public Projection getParent() {
        var parent = getBackingNode().getParent(time);

        if (parent == null) {
            return null;
        } else {
            return parent.projection(time);
        }
    }


    @Override
    public List<Projection> getChildren() {
        return FilteredMappedListView.filterMap(
            getBackingNode().getChildOrder(),
            (child) -> {
                if (getBackingNode().isChild(child, time)) {
                    return Optional.of(child.projection(time));
                } else {
                    return Optional.empty();
                }
            }
        );
    }

    @Override
    public void addChild(final Projection child) {
        getBackingNode().addChild(child.getBackingNode(), time);
    }

    @Override
    public void insertChild(final Projection child, int index) {
        // The method `DiffNode.addChild` can't be used here because `index` has a different
        // meaning: For `DiffNode.addChild` it counts all children, before and after, but here
        // it only counts children at `time`.

        var iterator = getBackingNode().getChildOrder().listIterator();
        for (int i = 0; i < index; ) {
            if (!iterator.hasNext()) {
                throw new IllegalArgumentException();
            }

            if (iterator.next().getDiffType().existsAtTime(time)) {
                ++i;
            }
        }

        getBackingNode().insertChild(child.getBackingNode(), iterator.nextIndex(), time);
    }

    @Override
    public boolean removeChild(final Projection child) {
        return getBackingNode().removeChild(child.getBackingNode(), time);
    }

    @Override
    public void removeAllChildren() {
        getBackingNode().removeChildren(time);
    }

    @Override
    public Node getFormula() {
        return getBackingNode().getFormula();
    }

    @Override
    public int getID() {
        return getBackingNode().getID();
    }
};
