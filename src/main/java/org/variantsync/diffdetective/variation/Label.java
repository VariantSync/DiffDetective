package org.variantsync.diffdetective.variation;

import java.util.List;

import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.VariationDiff; // For Javadoc
import org.variantsync.diffdetective.variation.tree.VariationTree; // For Javadoc

/**
 * Base interface for labels of {@link VariationTree}s and {@link VariationDiff}s.
 */
public interface Label {
    /**
     * Returns the lines which need to be printed before the children of a node.
     * Most lines associated with a node should be included in this list, for example, {@code #if}s are stored here.
     */
    List<String> getLines();
    /**
     * Returns the lines which need to be printed after the children of a node.
     * For example, {@code #endif}s are stored as trailing lines.
     */
    List<String> getTrailingLines();

    /**
     * Returns a deep copy where the state that is only valid at {@code time} is set to its default
     * value. Note that not all implementations need to have time dependent state.
     *
     * @see withTimeDependentStateFrom
     */
    default Label withoutTimeDependentState(Time time) {
        return this.clone();
    }

    /**
     * Returns a deep copy where the state that is only valid at {@code time} is copied from
     * {@code other}. All time independent state must be equal in {@code this} and {@code other}.
     * Note that not all implementations need to have time dependent state.
     *
     * @see withoutTimeDependentState
     */
    default Label withTimeDependentStateFrom(Label other, Time time) {
        Assert.assertEquals(this, other);
        return this.clone();
    }

    /**
     * Creates a deep copy of this label.
     */
    Label clone();
}
