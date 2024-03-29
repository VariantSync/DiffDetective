package org.variantsync.diffdetective.variation.diff.serialize;

import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;

/**
 * Product of all data relevant for exporting a single edge.
 *
 * Note that an edge doesn't need to describe a parent child relationship between {@code from} and
 * {@code to}. Information related to the type of the relation between {@code from} and {@code to}
 * should be encoded into {@code style}.
 */
public record StyledEdge<L extends Label>(DiffNode<L> from, DiffNode<L> to, Style style) {
    public record Style(String lineGraphType, String tikzStyle) {
    }

    public static final Style BEFORE = new Style("b", "before");
    public static final Style AFTER = new Style("a", "after");
    public static final Style ALWAYS = new Style("ba", "always");
}
