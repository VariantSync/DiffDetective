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

public class WrappedVariationTree extends AbstractTree {
    private String cachedLabel;
    private VariationNode<?> backingNode;

    public WrappedVariationTree(VariationNode<?> node) {
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

    protected WrappedVariationTree newInstance(VariationNode<?> node) {
        return new WrappedVariationTree(node);
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
        return backingNode.getLineRange().getFromInclusive();
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
