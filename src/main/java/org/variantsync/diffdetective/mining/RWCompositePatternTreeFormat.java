package org.variantsync.diffdetective.mining;

import org.variantsync.diffdetective.variation.diff.source.VariationDiffSource;
import org.variantsync.diffdetective.variation.diff.serialize.treeformat.VariationDiffLabelFormat;
import org.variantsync.diffdetective.variation.diff.source.PatchFile;

public class RWCompositePatternTreeFormat implements VariationDiffLabelFormat {
    @Override
    public VariationDiffSource fromLabel(String label) {
        throw new UnsupportedOperationException("Cannot read");
    }

    @Override
    public String toLabel(VariationDiffSource variationDiffSource) {
        if (variationDiffSource instanceof PatchFile p) {
            final String fileName = p.path().getFileName().toString();
            return fileName.substring(0, fileName.indexOf('.')).replaceAll("_", " ");
        }

        throw new IllegalArgumentException("Expected a PatchFile but got " + variationDiffSource);
    }
}
