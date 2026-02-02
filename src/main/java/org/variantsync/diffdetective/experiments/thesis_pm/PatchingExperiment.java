package org.variantsync.diffdetective.experiments.thesis_pm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import org.variantsync.diffdetective.AnalysisRunner;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.patching.Patching;
import org.variantsync.diffdetective.variation.diff.transform.CutNonEditedSubtrees;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.diffdetective.variation.tree.view.TreeView;
import org.variantsync.diffdetective.variation.tree.view.relevance.ConfigureWithFullConfig;
import org.variantsync.diffdetective.variation.tree.view.relevance.Unchanged;
import org.variantsync.functjonal.Pair;
import org.variantsync.functjonal.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.*;
import org.variantsync.diffdetective.datasets.PatchDiffParseOptions;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.experiments.thesis_pm.Generator.Error;
import org.variantsync.diffdetective.show.Show;
import org.variantsync.diffdetective.show.engine.GameEngine;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;

public class PatchingExperiment implements Analysis.Hooks {

	private static final AnalysisResult.ResultKey<PTErrorPatchesCounter> PT_ERROR_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"PT - error patches");
	private static final AnalysisResult.ResultKey<PTRejectedPatchesCounter> PT_REJECTED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"PT - rejected patches");
	private static final AnalysisResult.ResultKey<PTIncorrectlyAppliedPatchesCounter> PT_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"PT - incorrectly applied patches");
	private static final AnalysisResult.ResultKey<PTIncorrectlyAppliedPatchesUnchCounter> PT_INCORRECTLY_APPLIED_PATCHES_UNCH_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"PT - incorrectly applied patches (unchanged failed)");
	private static final AnalysisResult.ResultKey<PTIncorrectlyAppliedPatchesConfCounter> PT_INCORRECTLY_APPLIED_PATCHES_CONF_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"PT - incorrectly applied patches (configure failed)");
	private static final AnalysisResult.ResultKey<PTSuccessfullyAppliedPatchesCounter> PT_SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"PT - successfully applied patches");

	private static final AnalysisResult.ResultKey<GNUErrorPatchesCounter> GNU_ERROR_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"GNU - error patches");
	private static final AnalysisResult.ResultKey<GNURejectedPatchesCounter> GNU_REJECTED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"GNU - rejected patches");
	private static final AnalysisResult.ResultKey<GNUIncorrectlyAppliedPatchesCounter> GNU_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"GNU - incorrectly applied patches");
	private static final AnalysisResult.ResultKey<GNUIncorrectlyAppliedPatchesUnchCounter> GNU_INCORRECTLY_APPLIED_PATCHES_UNCH_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"GNU - incorrectly applied patches (unchanged failed)");
	private static final AnalysisResult.ResultKey<GNUIncorrectlyAppliedPatchesConfCounter> GNU_INCORRECTLY_APPLIED_PATCHES_CONF_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"GNU - incorrectly applied patches (configure failed)");
	private static final AnalysisResult.ResultKey<GNUSuccessfullyAppliedPatchesCounter> GNU_SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"GNU - successfully applied patches");

	private static final AnalysisResult.ResultKey<MPATCHErrorPatchesCounter> MPATCH_ERROR_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"MPATCH - error patches");
	private static final AnalysisResult.ResultKey<MPATCHRejectedPatchesCounter> MPATCH_REJECTED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"MPATCH - rejected patches");
	private static final AnalysisResult.ResultKey<MPATCHIncorrectlyAppliedPatchesCounter> MPATCH_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"MPATCH - incorrectly applied patches");
	private static final AnalysisResult.ResultKey<MPATCHIncorrectlyAppliedPatchesUnchCounter> MPATCH_INCORRECTLY_APPLIED_PATCHES_UNCH_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"MPATCH - incorrectly applied patches (unchanged failed)");
	private static final AnalysisResult.ResultKey<MPATCHIncorrectlyAppliedPatchesConfCounter> MPATCH_INCORRECTLY_APPLIED_PATCHES_CONF_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"MPATCH - incorrectly applied patches (configure failed)");
	private static final AnalysisResult.ResultKey<MPATCHSuccessfullyAppliedPatchesCounter> MPATCH_SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"MPATCH - successfully applied patches");

	private static final AnalysisResult.ResultKey<GNUViewErrorPatchesCounter> GNUVIEW_ERROR_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"GNU View - error patches");
	private static final AnalysisResult.ResultKey<GNUViewRejectedPatchesCounter> GNUVIEW_REJECTED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"GNU View - rejected patches");
	private static final AnalysisResult.ResultKey<GNUViewIncorrectlyAppliedPatchesCounter> GNUVIEW_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"GNU View - incorrectly applied patches");
	private static final AnalysisResult.ResultKey<GNUViewIncorrectlyAppliedPatchesUnchCounter> GNUVIEW_INCORRECTLY_APPLIED_PATCHES_UNCH_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"GNU View - incorrectly applied patches (unchanged failed)");
	private static final AnalysisResult.ResultKey<GNUViewIncorrectlyAppliedPatchesConfCounter> GNUVIEW_INCORRECTLY_APPLIED_PATCHES_CONF_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"GNU View - incorrectly applied patches (configure failed)");
	private static final AnalysisResult.ResultKey<GNUViewSuccessfullyAppliedPatchesCounter> GNUVIEW_SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"GNU View - successfully applied patches");

	private static final AnalysisResult.ResultKey<MPATCHViewErrorPatchesCounter> MPATCHVIEW_ERROR_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"MPATCH View - error patches");
	private static final AnalysisResult.ResultKey<MPATCHViewRejectedPatchesCounter> MPATCHVIEW_REJECTED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"MPATCH View - rejected patches");
	private static final AnalysisResult.ResultKey<MPATCHViewIncorrectlyAppliedPatchesCounter> MPATCHVIEW_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"MPATCH View - incorrectly applied patches");
	private static final AnalysisResult.ResultKey<MPATCHViewIncorrectlyAppliedPatchesUnchCounter> MPATCHVIEW_INCORRECTLY_APPLIED_PATCHES_UNCH_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"MPATCH View - incorrectly applied patches (unchanged failed)");
	private static final AnalysisResult.ResultKey<MPATCHViewIncorrectlyAppliedPatchesConfCounter> MPATCHVIEW_INCORRECTLY_APPLIED_PATCHES_CONF_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"MPATCH View - incorrectly applied patches (configure failed)");
	private static final AnalysisResult.ResultKey<MPATCHViewSuccessfullyAppliedPatchesCounter> MPATCHVIEW_SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"MPATCH View - successfully applied patches");

	private static final AnalysisResult.ResultKey<ProcessedPatchesCounter> PROCESSED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"processed patches (my)");

	private static final String PATCH = "patch.txt";
	private static final String CODE = "code.txt";

	private int commits = 0;
	private static int incorrectPatchesPT = 0;
	private static Map<Integer, Pair<PatchScenario<DiffLinesLabel>, VariationTree<DiffLinesLabel>>> failedPatches = new HashMap<>();
	private static Map<String, PatchScenario<DiffLinesLabel>> rejectedPatches = new HashMap<>();

	private static class PTErrorPatchesCounter extends SimpleMetadata<Integer, PTErrorPatchesCounter> {
		public PTErrorPatchesCounter() {
			super(0, "PT - error patches", Integer::sum);
		}
	}

	private static class PTRejectedPatchesCounter extends SimpleMetadata<Integer, PTRejectedPatchesCounter> {
		public PTRejectedPatchesCounter() {
			super(0, "PT - rejected patches", Integer::sum);
		}
	}

	private static class PTIncorrectlyAppliedPatchesCounter
			extends SimpleMetadata<Integer, PTIncorrectlyAppliedPatchesCounter> {
		public PTIncorrectlyAppliedPatchesCounter() {
			super(0, "PT - incorrectly applied patches", Integer::sum);
		}
	}

	private static class PTIncorrectlyAppliedPatchesUnchCounter
			extends SimpleMetadata<Integer, PTIncorrectlyAppliedPatchesUnchCounter> {
		public PTIncorrectlyAppliedPatchesUnchCounter() {
			super(0, "PT - incorrectly applied patches (unchanged failed)", Integer::sum);
		}
	}

	private static class PTIncorrectlyAppliedPatchesConfCounter
			extends SimpleMetadata<Integer, PTIncorrectlyAppliedPatchesConfCounter> {
		public PTIncorrectlyAppliedPatchesConfCounter() {
			super(0, "PT - incorrectly applied patches (configure failed)", Integer::sum);
		}
	}

	private static class PTSuccessfullyAppliedPatchesCounter
			extends SimpleMetadata<Integer, PTSuccessfullyAppliedPatchesCounter> {
		public PTSuccessfullyAppliedPatchesCounter() {
			super(0, "PT - successfully applied patches", Integer::sum);
		}
	}

	private static class GNUErrorPatchesCounter extends SimpleMetadata<Integer, GNUErrorPatchesCounter> {
		public GNUErrorPatchesCounter() {
			super(0, "GNU - error patches", Integer::sum);
		}
	}

	private static class GNURejectedPatchesCounter extends SimpleMetadata<Integer, GNURejectedPatchesCounter> {
		public GNURejectedPatchesCounter() {
			super(0, "GNU - rejected patches", Integer::sum);
		}
	}

	private static class GNUIncorrectlyAppliedPatchesCounter
			extends SimpleMetadata<Integer, GNUIncorrectlyAppliedPatchesCounter> {
		public GNUIncorrectlyAppliedPatchesCounter() {
			super(0, "GNU - incorrectly applied patches", Integer::sum);
		}
	}

	private static class GNUIncorrectlyAppliedPatchesUnchCounter
			extends SimpleMetadata<Integer, GNUIncorrectlyAppliedPatchesUnchCounter> {
		public GNUIncorrectlyAppliedPatchesUnchCounter() {
			super(0, "GNU - incorrectly applied patches (unchanged failed)", Integer::sum);
		}
	}

	private static class GNUIncorrectlyAppliedPatchesConfCounter
			extends SimpleMetadata<Integer, GNUIncorrectlyAppliedPatchesConfCounter> {
		public GNUIncorrectlyAppliedPatchesConfCounter() {
			super(0, "GNU - incorrectly applied patches (configure failed)", Integer::sum);
		}
	}

	private static class GNUSuccessfullyAppliedPatchesCounter
			extends SimpleMetadata<Integer, GNUSuccessfullyAppliedPatchesCounter> {
		public GNUSuccessfullyAppliedPatchesCounter() {
			super(0, "GNU - successfully applied patches", Integer::sum);
		}
	}

	private static class MPATCHErrorPatchesCounter extends SimpleMetadata<Integer, MPATCHErrorPatchesCounter> {
		public MPATCHErrorPatchesCounter() {
			super(0, "MPATCH - error patches", Integer::sum);
		}
	}

	private static class MPATCHRejectedPatchesCounter extends SimpleMetadata<Integer, MPATCHRejectedPatchesCounter> {
		public MPATCHRejectedPatchesCounter() {
			super(0, "MPATCH - rejected patches", Integer::sum);
		}
	}

	private static class MPATCHIncorrectlyAppliedPatchesCounter
			extends SimpleMetadata<Integer, MPATCHIncorrectlyAppliedPatchesCounter> {
		public MPATCHIncorrectlyAppliedPatchesCounter() {
			super(0, "MPATCH - incorrectly applied patches", Integer::sum);
		}
	}

	private static class MPATCHIncorrectlyAppliedPatchesUnchCounter
			extends SimpleMetadata<Integer, MPATCHIncorrectlyAppliedPatchesUnchCounter> {
		public MPATCHIncorrectlyAppliedPatchesUnchCounter() {
			super(0, "MPATCH - incorrectly applied patches (unchanged failed)", Integer::sum);
		}
	}

	private static class MPATCHIncorrectlyAppliedPatchesConfCounter
			extends SimpleMetadata<Integer, MPATCHIncorrectlyAppliedPatchesConfCounter> {
		public MPATCHIncorrectlyAppliedPatchesConfCounter() {
			super(0, "MPATCH - incorrectly applied patches (configure failed)", Integer::sum);
		}
	}

	private static class MPATCHSuccessfullyAppliedPatchesCounter
			extends SimpleMetadata<Integer, MPATCHSuccessfullyAppliedPatchesCounter> {
		public MPATCHSuccessfullyAppliedPatchesCounter() {
			super(0, "MPATCH - successfully applied patches", Integer::sum);
		}
	}

	private static class GNUViewErrorPatchesCounter extends SimpleMetadata<Integer, GNUViewErrorPatchesCounter> {
		public GNUViewErrorPatchesCounter() {
			super(0, "GNU (View) - error patches", Integer::sum);
		}
	}

	private static class GNUViewRejectedPatchesCounter extends SimpleMetadata<Integer, GNUViewRejectedPatchesCounter> {
		public GNUViewRejectedPatchesCounter() {
			super(0, "GNU (View) - rejected patches", Integer::sum);
		}
	}

	private static class GNUViewIncorrectlyAppliedPatchesCounter
			extends SimpleMetadata<Integer, GNUViewIncorrectlyAppliedPatchesCounter> {
		public GNUViewIncorrectlyAppliedPatchesCounter() {
			super(0, "GNU (View) - incorrectly applied patches", Integer::sum);
		}
	}

	private static class GNUViewIncorrectlyAppliedPatchesUnchCounter
			extends SimpleMetadata<Integer, GNUViewIncorrectlyAppliedPatchesUnchCounter> {
		public GNUViewIncorrectlyAppliedPatchesUnchCounter() {
			super(0, "GNU View - incorrectly applied patches (unchanged failed)", Integer::sum);
		}
	}

	private static class GNUViewIncorrectlyAppliedPatchesConfCounter
			extends SimpleMetadata<Integer, GNUViewIncorrectlyAppliedPatchesConfCounter> {
		public GNUViewIncorrectlyAppliedPatchesConfCounter() {
			super(0, "GNU View - incorrectly applied patches (configure failed)", Integer::sum);
		}
	}

	private static class GNUViewSuccessfullyAppliedPatchesCounter
			extends SimpleMetadata<Integer, GNUViewSuccessfullyAppliedPatchesCounter> {
		public GNUViewSuccessfullyAppliedPatchesCounter() {
			super(0, "GNU (View) - successfully applied patches", Integer::sum);
		}
	}

	private static class MPATCHViewErrorPatchesCounter extends SimpleMetadata<Integer, MPATCHViewErrorPatchesCounter> {
		public MPATCHViewErrorPatchesCounter() {
			super(0, "MPATCH (View) - error patches", Integer::sum);
		}
	}

	private static class MPATCHViewRejectedPatchesCounter
			extends SimpleMetadata<Integer, MPATCHViewRejectedPatchesCounter> {
		public MPATCHViewRejectedPatchesCounter() {
			super(0, "MPATCH (View) - rejected patches", Integer::sum);
		}
	}

	private static class MPATCHViewIncorrectlyAppliedPatchesCounter
			extends SimpleMetadata<Integer, MPATCHViewIncorrectlyAppliedPatchesCounter> {
		public MPATCHViewIncorrectlyAppliedPatchesCounter() {
			super(0, "MPATCH (View) - incorrectly applied patches", Integer::sum);
		}
	}

	private static class MPATCHViewIncorrectlyAppliedPatchesUnchCounter
			extends SimpleMetadata<Integer, MPATCHViewIncorrectlyAppliedPatchesUnchCounter> {
		public MPATCHViewIncorrectlyAppliedPatchesUnchCounter() {
			super(0, "MPATCH View - incorrectly applied patches (unchanged failed)", Integer::sum);
		}
	}

	private static class MPATCHViewIncorrectlyAppliedPatchesConfCounter
			extends SimpleMetadata<Integer, MPATCHViewIncorrectlyAppliedPatchesConfCounter> {
		public MPATCHViewIncorrectlyAppliedPatchesConfCounter() {
			super(0, "MPATCH View - incorrectly applied patches (configure failed)", Integer::sum);
		}
	}

	private static class MPATCHViewSuccessfullyAppliedPatchesCounter
			extends SimpleMetadata<Integer, MPATCHViewSuccessfullyAppliedPatchesCounter> {
		public MPATCHViewSuccessfullyAppliedPatchesCounter() {
			super(0, "MPATCH (View) - successfully applied patches", Integer::sum);
		}
	}

	private static class ProcessedPatchesCounter extends SimpleMetadata<Integer, ProcessedPatchesCounter> {
		public ProcessedPatchesCounter() {
			super(0, "processed patches (my)", Integer::sum);
		}
	}

	@Override
	public void initializeResults(Analysis analysis) {
		analysis.append(PT_ERROR_PATCHES_COUNTER_RESULT_KEY, new PTErrorPatchesCounter());
		analysis.append(PT_REJECTED_PATCHES_COUNTER_RESULT_KEY, new PTRejectedPatchesCounter());
		analysis.append(PT_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY, new PTIncorrectlyAppliedPatchesCounter());
		analysis.append(PT_INCORRECTLY_APPLIED_PATCHES_CONF_COUNTER_RESULT_KEY, new PTIncorrectlyAppliedPatchesConfCounter());
		analysis.append(PT_INCORRECTLY_APPLIED_PATCHES_UNCH_COUNTER_RESULT_KEY, new PTIncorrectlyAppliedPatchesUnchCounter());
		analysis.append(PT_SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY, new PTSuccessfullyAppliedPatchesCounter());
		analysis.append(GNU_ERROR_PATCHES_COUNTER_RESULT_KEY, new GNUErrorPatchesCounter());
		analysis.append(GNU_REJECTED_PATCHES_COUNTER_RESULT_KEY, new GNURejectedPatchesCounter());
		analysis.append(GNU_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY, new GNUIncorrectlyAppliedPatchesCounter());
		analysis.append(GNU_INCORRECTLY_APPLIED_PATCHES_CONF_COUNTER_RESULT_KEY, new GNUIncorrectlyAppliedPatchesConfCounter());
		analysis.append(GNU_INCORRECTLY_APPLIED_PATCHES_UNCH_COUNTER_RESULT_KEY, new GNUIncorrectlyAppliedPatchesUnchCounter());
		analysis.append(GNU_SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY,
				new GNUSuccessfullyAppliedPatchesCounter());
		analysis.append(MPATCH_ERROR_PATCHES_COUNTER_RESULT_KEY, new MPATCHErrorPatchesCounter());
		analysis.append(MPATCH_REJECTED_PATCHES_COUNTER_RESULT_KEY, new MPATCHRejectedPatchesCounter());
		analysis.append(MPATCH_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY,
				new MPATCHIncorrectlyAppliedPatchesCounter());
		analysis.append(MPATCH_INCORRECTLY_APPLIED_PATCHES_CONF_COUNTER_RESULT_KEY, new MPATCHIncorrectlyAppliedPatchesConfCounter());
		analysis.append(MPATCH_INCORRECTLY_APPLIED_PATCHES_UNCH_COUNTER_RESULT_KEY, new MPATCHIncorrectlyAppliedPatchesUnchCounter());
		analysis.append(MPATCH_SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY,
				new MPATCHSuccessfullyAppliedPatchesCounter());
		analysis.append(GNUVIEW_ERROR_PATCHES_COUNTER_RESULT_KEY, new GNUViewErrorPatchesCounter());
		analysis.append(GNUVIEW_REJECTED_PATCHES_COUNTER_RESULT_KEY, new GNUViewRejectedPatchesCounter());
		analysis.append(GNUVIEW_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY,
				new GNUViewIncorrectlyAppliedPatchesCounter());
		analysis.append(GNUVIEW_INCORRECTLY_APPLIED_PATCHES_CONF_COUNTER_RESULT_KEY, new GNUViewIncorrectlyAppliedPatchesConfCounter());
		analysis.append(GNUVIEW_INCORRECTLY_APPLIED_PATCHES_UNCH_COUNTER_RESULT_KEY, new GNUViewIncorrectlyAppliedPatchesUnchCounter());
		analysis.append(GNUVIEW_SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY,
				new GNUViewSuccessfullyAppliedPatchesCounter());
		analysis.append(MPATCHVIEW_ERROR_PATCHES_COUNTER_RESULT_KEY, new MPATCHViewErrorPatchesCounter());
		analysis.append(MPATCHVIEW_REJECTED_PATCHES_COUNTER_RESULT_KEY, new MPATCHViewRejectedPatchesCounter());
		analysis.append(MPATCHVIEW_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY,
				new MPATCHViewIncorrectlyAppliedPatchesCounter());
		analysis.append(MPATCHVIEW_INCORRECTLY_APPLIED_PATCHES_CONF_COUNTER_RESULT_KEY, new MPATCHViewIncorrectlyAppliedPatchesConfCounter());
		analysis.append(MPATCHVIEW_INCORRECTLY_APPLIED_PATCHES_UNCH_COUNTER_RESULT_KEY, new MPATCHViewIncorrectlyAppliedPatchesUnchCounter());
		analysis.append(MPATCHVIEW_SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY,
				new MPATCHViewSuccessfullyAppliedPatchesCounter());
		analysis.append(PROCESSED_PATCHES_COUNTER_RESULT_KEY, new ProcessedPatchesCounter());
	}

	@Override
	public boolean beginCommit(Analysis analysis) throws Exception {
		++commits;
		return true;
	}

	@Override
	public boolean analyzeVariationDiff(Analysis analysis) throws Exception {
		VariationDiff<DiffLinesLabel> diff = analysis.getCurrentVariationDiff();
		String commitHash = analysis.getCurrentCommit().getName();
	
		PatchScenario<DiffLinesLabel> scenario = Generator.generatePatchScenario(diff, commitHash);

		if (scenario == null) {
			// something went wrong when generating the patching scenario
			return false;
		}
		analysis.get(PROCESSED_PATCHES_COUNTER_RESULT_KEY).value++;
		Result<VariationTree<DiffLinesLabel>, Error> patchTransformerResult = Generator.runPatchTransformer(
				scenario.sourcePatch, scenario.targetVariantBefore, scenario.sourceVariantConfig,
				scenario.targetVariantConfig);

		patchTransformerResult.match(tree -> {
			if (tree != null) {
				Pair<Boolean, Boolean> equiv = Patching.arePatchedVariantsEquivalent(tree,
						scenario.sourceVariantAfterRedToCrossVarFeatures, scenario.targetVariantBeforeRedToUnchanged,
						scenario.sourceVariantConfig, scenario.unchangedAfter);
				if (!equiv.first()) {
					analysis.get(PT_INCORRECTLY_APPLIED_PATCHES_CONF_COUNTER_RESULT_KEY).value++;
				}
				if (!equiv.second()) {
					analysis.get(PT_INCORRECTLY_APPLIED_PATCHES_UNCH_COUNTER_RESULT_KEY).value++;
				}
				if (equiv.first() && equiv.second()) {
					analysis.get(PT_SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY).value++;
				} else {
					analysis.get(PT_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY).value++;
				}
			} else {
				PatchingExperiment.incorrectPatchesPT++;
				PatchingExperiment.failedPatches.put(PatchingExperiment.incorrectPatchesPT,
						new Pair<PatchScenario<DiffLinesLabel>, VariationTree<DiffLinesLabel>>(scenario, tree));
				analysis.get(PT_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY).value++;
			}
		}, error -> {
			PatchingExperiment.rejectedPatches.put(commitHash, scenario);
			analysis.get(PT_REJECTED_PATCHES_COUNTER_RESULT_KEY).value++;
		});

		Result<VariationTree<DiffLinesLabel>, Error> gnuPatchResult;
		try {
			gnuPatchResult = Generator.runGnuPatch(scenario.targetVariantBefore, PATCH, CODE, commitHash);
			gnuPatchResult.match(tree -> {
				if (tree != null) {
					Pair<Boolean, Boolean> equiv = Patching.arePatchedVariantsEquivalent(tree,
							scenario.sourceVariantAfterRedToCrossVarFeatures,
							scenario.targetVariantBeforeRedToUnchanged, scenario.sourceVariantConfig,
							scenario.unchangedAfter);
					if (!equiv.first()) {
						analysis.get(GNU_INCORRECTLY_APPLIED_PATCHES_CONF_COUNTER_RESULT_KEY).value++;
					}
					if (!equiv.second()) {
						analysis.get(GNU_INCORRECTLY_APPLIED_PATCHES_UNCH_COUNTER_RESULT_KEY).value++;
					}
					if (equiv.first() && equiv.second()) {
						analysis.get(GNU_SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY).value++;
					} else {
						analysis.get(GNU_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY).value++;
					}
				} else {
					analysis.get(GNU_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY).value++;
				}
			}, error -> analysis.get(GNU_REJECTED_PATCHES_COUNTER_RESULT_KEY).value++);
		} catch (IOException e) {
			analysis.get(GNU_ERROR_PATCHES_COUNTER_RESULT_KEY).value++;
		}

		Result<VariationTree<DiffLinesLabel>, Error> mpatchResult;
		try {
			mpatchResult = Generator.runMPatch(scenario.targetVariantBefore, PATCH, CODE, commitHash);
			mpatchResult.match(tree -> {
				if (tree != null) {
					Pair<Boolean, Boolean> equiv = Patching.arePatchedVariantsEquivalent(tree,
							scenario.sourceVariantAfterRedToCrossVarFeatures,
							scenario.targetVariantBeforeRedToUnchanged, scenario.sourceVariantConfig,
							scenario.unchangedAfter);
					if (!equiv.first()) {
						analysis.get(MPATCH_INCORRECTLY_APPLIED_PATCHES_CONF_COUNTER_RESULT_KEY).value++;
					}
					if (!equiv.second()) {
						analysis.get(MPATCH_INCORRECTLY_APPLIED_PATCHES_UNCH_COUNTER_RESULT_KEY).value++;
					}
					if (equiv.first() && equiv.second()) {
						analysis.get(MPATCH_SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY).value++;
					} else {
						analysis.get(MPATCH_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY).value++;
					}
				} else {
					analysis.get(MPATCH_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY).value++;
				}
			}, error -> analysis.get(MPATCH_REJECTED_PATCHES_COUNTER_RESULT_KEY).value++);
		} catch (IOException e) {
			analysis.get(MPATCH_ERROR_PATCHES_COUNTER_RESULT_KEY).value++;
		}

		if (Generator.generateViewVariants(scenario.sourcePatch, scenario.targetVariantBefore,
				scenario.targetVariantConfig, commitHash)) {
			Result<VariationTree<DiffLinesLabel>, Error> gnuPatchResultView;
			try {
				gnuPatchResultView = Generator.runGnuPatch(scenario.targetVariantBefore, PATCH, CODE, commitHash);
				gnuPatchResultView.match(tree -> {
					if (tree != null) {
						Pair<Boolean, Boolean> equiv = Patching.arePatchedVariantsEquivalent(tree,
								scenario.sourceVariantAfterRedToCrossVarFeatures,
								scenario.targetVariantBeforeRedToUnchanged, scenario.sourceVariantConfig,
								scenario.unchangedAfter);
						if (!equiv.first()) {
							analysis.get(GNUVIEW_INCORRECTLY_APPLIED_PATCHES_CONF_COUNTER_RESULT_KEY).value++;
						}
						if (!equiv.second()) {
							analysis.get(GNUVIEW_INCORRECTLY_APPLIED_PATCHES_UNCH_COUNTER_RESULT_KEY).value++;
						}
						if (equiv.first() && equiv.second()) {
							analysis.get(GNUVIEW_SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY).value++;
						} else {
							analysis.get(GNUVIEW_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY).value++;
						}
					} else {
						analysis.get(GNUVIEW_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY).value++;
					}
				}, error -> analysis.get(GNUVIEW_REJECTED_PATCHES_COUNTER_RESULT_KEY).value++);
			} catch (IOException e) {
				analysis.get(GNUVIEW_ERROR_PATCHES_COUNTER_RESULT_KEY).value++;
			}

			Result<VariationTree<DiffLinesLabel>, Error> mpatchResultView;
			try {
				mpatchResultView = Generator.runMPatch(scenario.targetVariantBefore, PATCH, CODE, commitHash);
				mpatchResultView.match(tree -> {
					if (tree != null) {
						Pair<Boolean, Boolean> equiv = Patching.arePatchedVariantsEquivalent(tree,
								scenario.sourceVariantAfterRedToCrossVarFeatures,
								scenario.targetVariantBeforeRedToUnchanged, scenario.sourceVariantConfig,
								scenario.unchangedAfter);
						if (!equiv.first()) {
							analysis.get(MPATCHVIEW_INCORRECTLY_APPLIED_PATCHES_CONF_COUNTER_RESULT_KEY).value++;
						}
						if (!equiv.second()) {
							analysis.get(MPATCHVIEW_INCORRECTLY_APPLIED_PATCHES_UNCH_COUNTER_RESULT_KEY).value++;
						}
						if (equiv.first() && equiv.second()) {
							analysis.get(MPATCHVIEW_SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY).value++;
						} else {
							analysis.get(MPATCHVIEW_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY).value++;
						}
					} else {
						analysis.get(MPATCHVIEW_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY).value++;
					}
				}, error -> analysis.get(MPATCHVIEW_REJECTED_PATCHES_COUNTER_RESULT_KEY).value++);
			} catch (IOException e) {
				analysis.get(MPATCHVIEW_ERROR_PATCHES_COUNTER_RESULT_KEY).value++;
			}

		}

		return true;
	}

	public static void writeToFile(String s, String filename) {
		try {
			File f = new File(Path.of("data", "examples", filename).toUri());
			f.createNewFile();
			BufferedWriter myWriter = new BufferedWriter(new FileWriter(f));
			myWriter.write(s);
			myWriter.close();
		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	@Override
	public void endBatch(Analysis analysis) throws Exception {
		for (Map.Entry<String, PatchScenario<DiffLinesLabel>> entry: PatchingExperiment.rejectedPatches.entrySet()) {
			writeScenarioToFilesystem("rejected", entry.getKey(), entry.getValue(), null);
		}
		PatchingExperiment.failedPatches.clear();
		PatchingExperiment.rejectedPatches.clear();
		Logger.info("Batch done: {} commits analyzed", commits);
	}

	private void writeScenarioToFilesystem(String filePrefix, String key, PatchScenario<DiffLinesLabel> scenario,
			VariationTree<DiffLinesLabel> patchedVariant) {
		VariationDiff<DiffLinesLabel> diff = scenario.sourcePatch;
		VariationTree<DiffLinesLabel> tree = scenario.targetVariantBefore;
		PatchingExperiment.writeToFile(diff.project(Time.BEFORE).unparse(), filePrefix + key + "A1");
		PatchingExperiment.writeToFile(diff.project(Time.AFTER).unparse(), filePrefix + key + "A2");
		PatchingExperiment.writeToFile(scenario.sourceVariantAfterRedToCrossVarFeatures.unparse(),
				filePrefix + key + "A2_red");
		PatchingExperiment.writeToFile(tree.unparse(), filePrefix + key + "B1");
		PatchingExperiment.writeToFile(scenario.targetVariantBeforeRedToUnchanged.unparse(),
				filePrefix + key + "B1_unch");
		if (patchedVariant != null) {
			PatchingExperiment.writeToFile(patchedVariant.unparse(), filePrefix + key + "B2");
			VariationTree<DiffLinesLabel> red = TreeView.tree(patchedVariant, scenario.sourceVariantConfig);
			VariationTree<DiffLinesLabel> unch = TreeView.tree(patchedVariant, scenario.unchangedAfter);
			PatchingExperiment.writeToFile(red.unparse(), filePrefix + key + "B2_red");
			PatchingExperiment.writeToFile(unch.unparse(), filePrefix + key + "B2_unch");

		}
		PatchingExperiment.writeToFile(scenario.sourceVariantConfig.toString(), filePrefix + key + "ConfigA");
		PatchingExperiment.writeToFile(scenario.targetVariantConfig.toString(), filePrefix + key + "ConfigB");
	}

	public static Analysis Create(Repository repo, Path outputDirectory, PatchingExperiment experiment) {
		return new Analysis("my analysis", List.of(experiment, new StatisticsAnalysis()
//	           , new EditClassValidation()
		), repo, outputDirectory);
	}

	public static void main(String[] args) {
		PatchingExperiment experiment = new PatchingExperiment();
		final AnalysisRunner.Options defaultOptions = AnalysisRunner.Options.DEFAULT(args);
		final AnalysisRunner.Options analysisOptions = new AnalysisRunner.Options(Path.of("data", "repos"),
				Path.of("data", "output"), Path.of("data", "demo-dataset.md"),
				repo -> new PatchDiffParseOptions(PatchDiffParseOptions.DiffStoragePolicy.DO_NOT_REMEMBER,
						new VariationDiffParseOptions(false, false)),
				defaultOptions.getFilterForRepo(), true, false);
		try {
			AnalysisRunner.run(analysisOptions, (repository, path) -> Analysis
					.forEachCommit(() -> PatchingExperiment.Create(repository, path, experiment), 1000, 8));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
