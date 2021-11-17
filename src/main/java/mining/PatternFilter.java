package mining;

import diff.difftree.DiffTree;

import java.util.function.Predicate;

/**
 * A named filter on difftrees.
 * The condition determines whether a DiffTree should be considered for computation or not.
 * Iff the condition returns true, the DiffTree should be considered.
 */
public record PatternFilter(String name, Predicate<DiffTree> condition) {}
