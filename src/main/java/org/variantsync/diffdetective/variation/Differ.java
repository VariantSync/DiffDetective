package main.java.org.variantsync.diffdetective.variation.tree;

public abstract class Differ<Domain, L extends Label> {
    protected final ObjectLanguage<Domain, Label> objectLanguage;

    public Differ(ObjectLanguage<Domain, Label> objectLanguage) {
        this.objectLanguage = objectLanguage;
    }

    public abstract VariationTree<L> diffVariants(Map<Configuration, Variant<L>> variants);

    public VariationTree<L> diff(Map<Configuration, Domain> values) {
        values.replaceAll((configuration, value) -> objectLanguage.disassemble(value));
        return diffVariants(values);
    }
}
