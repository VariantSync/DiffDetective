package org.variantsync.diffdetective.variation;

import org.variantsync.diffdetective.variation.tree.VariationTree;

/**
 * TODO.
 *
 * Invariants: {@code disassemble(assemble(variant)).equals(variant)} if {@code variant} was
 * constructed using {@code disassemble}.
 *
 * Note it's not required that {@code assemble(disassemble(n)).equals(n)}
 */
public interface ObjectLanguage<Domain, L extends Label> {
    Domain assemble(Variant<L> variant);
    Variant<L> disassemble(Domain value);
}
