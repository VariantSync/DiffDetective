package org.variantsync.diffdetective.experiments.thesis_pm;

import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.diffdetective.variation.tree.view.relevance.Configure;

public record PatchScenario<L extends Label>(VariationDiff<L> sourcePatch,
		VariationTree<L> targetVariantBefore, VariationDiff<L> patchGroundTruth,
		VariationTree<L> patchedVariantGroundTruth, Configure sourceVariantConfig, Configure targetVariantConfig) {
}
