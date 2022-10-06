package org.variantsync.diffdetective.diff.difftree.serialize;

import org.variantsync.diffdetective.diff.difftree.DiffNode;

/**
 * Product of all data relevant for exporting a single edge.
 *
 * Note that an edge doesn't need to describe a parent child relationship between {@code from} and
 * {@code to}. Information related to the type of the relation between {@code from} and {@code to}
 * should be encoded into {@code style}.
 */
public record StyledEdge(DiffNode from, DiffNode to, Style style) {
    public record Style(char lineGraphType, String tikzStyle) {
    }

    public static final Style BEFORE = new Style('b', "before");
    public static final Style AFTER = new Style('a', "after");
}
