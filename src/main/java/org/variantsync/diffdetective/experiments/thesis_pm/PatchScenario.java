package org.variantsync.diffdetective.experiments.thesis_pm;

import org.variantsync.diffdetective.show.Show;
import org.variantsync.diffdetective.show.engine.GameEngine;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.view.DiffView;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.diffdetective.variation.tree.view.TreeView;
import org.variantsync.diffdetective.variation.tree.view.relevance.Configure;
import org.variantsync.diffdetective.variation.tree.view.relevance.Unchanged;

public class PatchScenario<L extends Label> {
	public VariationDiff<L> sourcePatch;
	public VariationTree<L> targetVariantBefore;
	public VariationDiff<L> patchGroundTruth;
	public VariationTree<L> patchedVariantGroundTruth;
	public Configure sourceVariantConfig;
	public Configure targetVariantConfig;
	public VariationTree<DiffLinesLabel> sourceVariantAfterRedToCrossVarFeatures;
	public Unchanged unchangedAfter;
	public VariationTree<DiffLinesLabel> targetVariantBeforeRedToUnchanged;
	
	public PatchScenario(VariationDiff<L> sourcePatch,
			VariationTree<L> targetVariantBefore, VariationDiff<L> patchGroundTruth,
			VariationTree<L> patchedVariantGroundTruth, Configure sourceVariantConfig, Configure targetVariantConfig) {
		this.sourcePatch = sourcePatch;
		this.targetVariantBefore = targetVariantBefore;
		this.patchGroundTruth = patchGroundTruth;
		this.patchedVariantGroundTruth = patchedVariantGroundTruth;
		this.sourceVariantConfig = sourceVariantConfig;
		this.targetVariantConfig = targetVariantConfig;
		this.sourceVariantAfterRedToCrossVarFeatures = (VariationTree<DiffLinesLabel>) TreeView.tree(sourcePatch.project(Time.AFTER), this.targetVariantConfig);
		VariationDiff<DiffLinesLabel> sourcePatchConfiguredToCrossVarFeatures = (VariationDiff<DiffLinesLabel>) DiffView.optimized(sourcePatch, targetVariantConfig);
		this.unchangedAfter = new Unchanged((VariationDiff<DiffLinesLabel>) sourcePatchConfiguredToCrossVarFeatures, Time.AFTER);		
		Unchanged unchangedBefore = new Unchanged((VariationDiff<DiffLinesLabel>) sourcePatchConfiguredToCrossVarFeatures, Time.BEFORE);
		this.targetVariantBeforeRedToUnchanged = (VariationTree<DiffLinesLabel>) TreeView.tree(this.targetVariantBefore, unchangedBefore);
		GameEngine.showAndAwaitAll(Show.diff(sourcePatch), Show.diff(sourcePatchConfiguredToCrossVarFeatures), Show.tree(targetVariantBeforeRedToUnchanged));
	}
}
