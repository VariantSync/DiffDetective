package org.variantsync.diffdetective.diff.difftree;

import java.util.List;
import java.util.Optional;

import org.prop4j.Node;
import org.variantsync.diffdetective.diff.DiffLineNumber;
import org.variantsync.diffdetective.variationtree.VariationNode;
import org.variantsync.functjonal.list.FilteredMappedListView;

import static org.variantsync.diffdetective.diff.difftree.Time.AFTER;
import static org.variantsync.diffdetective.diff.difftree.Time.BEFORE;

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
public class Projection extends VariationNode<DiffNode> {
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

    @Override
    public DiffNode getBackingNode() {
        return backingNode;
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
    public int getFromLine() {
        return getBackingNode().getFromLine().atTime(time);
    }

    @Override
    public void setFromLine(int from) {
        var node = getBackingNode();
        var current = node.getFromLine();
        node.setFromLine(new DiffLineNumber(
            current.inDiff(),
            time == BEFORE ? from : current.beforeEdit(),
            time == AFTER ? from : current.afterEdit()
        ));
    }

    @Override
    public int getToLine() {
        return getBackingNode().getToLine().atTime(time);
    }

    @Override
    public void setToLine(int to) {
        var node = getBackingNode();
        var current = node.getToLine();
        node.setToLine(new DiffLineNumber(
            current.inDiff(),
            time == BEFORE ? to : current.beforeEdit(),
            time == AFTER ? to : current.afterEdit()
        ));
    }

    @Override
    public VariationNode<DiffNode> getParent() {
        var parent = getBackingNode().getParent(time);

        if (parent == null) {
            return null;
        } else {
            return parent.projection(time);
        }
    }


    @Override
    public List<VariationNode<DiffNode>> getChildren() {
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
    public void addChild(final VariationNode<DiffNode> child) {
        getBackingNode().addChild(child.getBackingNode(), time);
    }

    @Override
    public void insertChild(final VariationNode<DiffNode> child, int index) {
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
    public boolean removeChild(final VariationNode<DiffNode> child) {
        return getBackingNode().removeChild(child.getBackingNode(), time);
    }

    @Override
    public void removeAllChildren() {
        getBackingNode().removeChildren(time);
    }

    @Override
    public Node getDirectFeatureMapping() {
        return getBackingNode().getDirectFeatureMapping();
    }

    @Override
    public int getID() {
        return getBackingNode().getID();
    }

    @Override
    public boolean isSameAs(VariationNode<DiffNode> other) {
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
