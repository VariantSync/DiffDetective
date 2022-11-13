package org.variantsync.diffdetective.mining;

import org.variantsync.diffdetective.variation.diff.DiffTreeSource;
import org.variantsync.diffdetective.variation.diff.serialize.treeformat.DiffTreeLabelFormat;
import org.variantsync.diffdetective.variation.diff.source.PatchFile;

public class RWCompositePatternTreeFormat implements DiffTreeLabelFormat {
    @Override
    public DiffTreeSource fromLabel(String label) {
        throw new UnsupportedOperationException("Cannot read");
    }

    @Override
    public String toLabel(DiffTreeSource diffTreeSource) {
        if (diffTreeSource instanceof PatchFile p) {
            final String fileName = p.path().getFileName().toString();
            return fileName.substring(0, fileName.indexOf('.')).replaceAll("_", " ");
        }

        throw new IllegalArgumentException("Expected a PatchFile but got " + diffTreeSource);
    }
}
