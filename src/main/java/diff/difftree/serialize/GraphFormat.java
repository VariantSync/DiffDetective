package diff.difftree.serialize;

/**
 * Format of the graph.
 * A DIFFGRAPH contains no roots. Any kind of graph, except a tree.
 * A DIFFTREE contains exactly one root. A specific kind of graph, a tree.
 */
public enum GraphFormat {
    DIFFGRAPH,
    DIFFTREE
}
