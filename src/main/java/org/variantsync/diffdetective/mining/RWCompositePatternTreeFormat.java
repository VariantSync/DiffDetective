package org.variantsync.diffdetective.mining;

import java.util.List;

import org.variantsync.diffdetective.util.FileSource;
import org.variantsync.diffdetective.util.Source;
import org.variantsync.diffdetective.variation.diff.serialize.treeformat.VariationDiffLabelFormat;

public class RWCompositePatternTreeFormat implements VariationDiffLabelFormat {
    @Override
    public Source fromLabel(String label) {
        throw new UnsupportedOperationException("Cannot read");
    }

    @Override
    public String toLabel(Source variationDiffSource) {
        List<FileSource> pathSources = Source.findAll(variationDiffSource, FileSource.class);
        if (pathSources.size() != 1) {
            throw new IllegalArgumentException("Expected a single path source but got:\n" + Source.fullExplanation(variationDiffSource));
        }

        final String fileName = pathSources.get(0).getPath().getFileName().toString();
        return fileName.substring(0, fileName.indexOf('.')).replaceAll("_", " ");
    }
}
