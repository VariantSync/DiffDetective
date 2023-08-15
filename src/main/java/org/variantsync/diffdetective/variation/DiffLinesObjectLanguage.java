package org.variantsync.diffdetective.variation;

public class DiffLinesObjectLanguage implements ObjectLanguage<String, DiffLinesLabel> {
    private VariationDiffParseOptions parseOptions;

    public DiffLinesObjectLanguage(VariationDiffParseOptions parseOptions) {
        this.parseOptions = parseOptions;
    }

    @Override
    public String assemble(Variant<DiffLinesLabel> variant) {
        var result = new StringBuilder();
        variant.tree().printSourceCode(result);
        return result.toString();
    }

    @Override
    public Variant<DiffLinesLabel> disassemble(String string) {
        try (
            var stringReader = new StringReader(string);
            var bufferedStringReader = new BufferedReader(stringReader)
        ) {
            return new Variant(
                VariationDiffParser.createVariationTree(bufferedStringReader, parseOptions).protection(Time.BEFORE),
                VariationTree.Unknown
            );
        }
    }
}
