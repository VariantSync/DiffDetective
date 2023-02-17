package org.variantsync.diffdetective.variation.diff.bad;

import org.variantsync.diffdetective.diff.text.DiffLineNumber;

public record DiffLineNumberRange(DiffLineNumber from, DiffLineNumber to) {
}
