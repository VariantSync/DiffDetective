package org.variantsync.diffdetective.preliminary.evaluation;

import org.prop4j.Node;

import java.util.Objects;

/**
 * Class representing a feature context.
 * A feature context consists of a propositional formula over features (Node) and an information if weaker feature contexts are also possible
 */
@Deprecated
public class FeatureContext {
    private final Node node;
    private final boolean weakerOrEqual;

    public FeatureContext(Node node) {
        this(node, false);
    }

    public FeatureContext(Node node, boolean weakerOrEqual) {
        this.node = node;
        this.weakerOrEqual = weakerOrEqual;
    }

    public Node getNode() {
        return node;
    }

    public boolean isWeakerOrEqual() {
        return weakerOrEqual;
    }

    public boolean isNull() {
        return node == null;
    }

    @Override
    public boolean equals(Object obj) {
        if(! (obj instanceof FeatureContext)){
            return false;
        }
        FeatureContext other = (FeatureContext) obj;
        if(this.node == null && other.node == null){
            return true;
        }else if(this.node == null){
            return false;
        }
        return this.node.equals(other.node) && this.weakerOrEqual == other.weakerOrEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(node, weakerOrEqual);
    }

    @Override
    public String toString() {
        return (weakerOrEqual ?"weaker than: ":"") + node;
    }
}
