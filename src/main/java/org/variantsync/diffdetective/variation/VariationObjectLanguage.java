package org.variantsync.diffdetective.variation;

public class VariationObjectLanguage<L extends Label> implements ObjectLanguage<VariationTree<L>, VariationLabel<L>> {
    @Override
    VariationTree<L> assemble(Variant<VariationLabel<L>> variant) {
        return new VariationTree(variant.tree(), variant.source());
    }

    @Override
    Variant<VariationLabel<L>> disassemble(VariationTree<L> value) {
        return new Variant(value.root(), value.source());
    }
}
