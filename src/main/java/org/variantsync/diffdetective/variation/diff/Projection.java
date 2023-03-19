package org.variantsync.diffdetective.variation.diff;

import java.util.List;

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
 * <p>Implementation note: It's ensured that identity can be checked using {@code ==}. This
 * prevents unexpected behaviour if some code uses {@code ==} instead of {@link isSameAs} as
 * documented in {@link VariationNode}. Although this is currently guaranteed by all
 * implementations of {@link VariationNode} it should still be considered a bug if {@code ==} is
 * used to check for identity ({@code null} checks are still allowed).
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
    public List<String> getLabelLines() {
        return getBackingNode().getLabelLines();
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
        return FilteredMappedListView.map(
            getBackingNode().getChildOrder(time),
            child -> child.projection(time)
        );
    }

    @Override
    public void addChild(final Projection child) {
        getBackingNode().addChild(child.getBackingNode(), time);
    }

    @Override
    public void insertChild(final Projection child, int index) {
        getBackingNode().insertChild(child.getBackingNode(), index, time);
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

    @Override
    public boolean isSameAs(Projection other) {
        if (other != null && getClass() == other.getClass()) {
            Projection otherProjection = (Projection) other;
            return time.equals(otherProjection.time) && getBackingNode() == otherProjection.getBackingNode();
        } else {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Projection projection = (Projection) o;
        return time.equals(projection.time) && getBackingNode().equals(projection.getBackingNode());
    }
};
