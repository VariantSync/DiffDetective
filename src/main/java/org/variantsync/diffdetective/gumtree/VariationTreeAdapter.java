package org.variantsync.diffdetective.gumtree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.variantsync.diffdetective.variation.tree.VariationNode;

import com.github.gumtreediff.tree.AbstractTree;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.Type;
import com.github.gumtreediff.tree.TypeSet;

/**
 * Adapter for running Gumtree's matching algorithms on variation trees.
 *
 * This class is an <a href="https://en.wikipedia.org/wiki/Adapter_pattern">adapter</a> for a
 * snapshot of a variation tree. This means that it doesn't reflect most changes to the underlying
 * variation tree. Essentially it creates a copy of the tree structure and the labels of a variation
 * tree by stores a reference to the variation node which it adapts.
 */
public class VariationTreeAdapter extends AbstractTree {
    private String cachedLabel;
    private VariationNode<?> backingNode;

    public VariationTreeAdapter(VariationNode<?> node) {
        this.backingNode = node;

        if (backingNode.isConditionalAnnotation()) {
            cachedLabel = backingNode.getFormula().toString();
        } else {
            cachedLabel = backingNode.getLabelLines().stream().collect(Collectors.joining("\n"));
        }

        var children = new ArrayList<Tree>(node.getChildren().size());
        for (var child : node.getChildren()) {
            children.add(newInstance(child));
        }
        setChildren(children);
    }

    protected VariationTreeAdapter newInstance(VariationNode<?> node) {
        return new VariationTreeAdapter(node);
    }

    public VariationNode<?> getVariationNode() {
        return backingNode;
    }


    @Override
    public String getLabel() {
        return cachedLabel;
    }

    /**
     * Uses line numbers instead of byte indexes.
     */
    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public Iterator<Entry<String, Object>> getMetadata() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getMetadata(String arg0) {
        throw new UnsupportedOperationException();
    }

    /**
     * Uses line numbers instead of byte indexes.
     */
    @Override
    public int getPos() {
        return backingNode.getLineRange().fromInclusive();
    }

    @Override
    public Type getType() {
        return TypeSet.type(backingNode.getNodeType().toString());
    }

    @Override
    public void setLabel(String label) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLength(int length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object setMetadata(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPos(int pos) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setType(Type type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Tree deepCopy() {
        throw new UnsupportedOperationException();
    }
}
