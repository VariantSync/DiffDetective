package org.variantsync.diffdetective.experiments.thesis_pm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.variantsync.diffdetective.AnalysisRunner;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.patching.Patching;
import org.variantsync.diffdetective.variation.diff.transform.CutNonEditedSubtrees;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.functjonal.Result;

import java.util.ArrayList;
import java.util.List;

import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.*;
import org.variantsync.diffdetective.datasets.PatchDiffParseOptions;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.git.PatchDiff;
import org.variantsync.diffdetective.diff.result.DiffParseException;
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
	private static final AnalysisResult.ResultKey<PTSuccessfullyAppliedPatchesCounter> PT_SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"PT - successfully applied patches");

	private static final AnalysisResult.ResultKey<GNUErrorPatchesCounter> GNU_ERROR_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"GNU - error patches");
	private static final AnalysisResult.ResultKey<GNURejectedPatchesCounter> GNU_REJECTED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"GNU - rejected patches");
	private static final AnalysisResult.ResultKey<GNUIncorrectlyAppliedPatchesCounter> GNU_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"GNU - incorrectly applied patches");
	private static final AnalysisResult.ResultKey<GNUSuccessfullyAppliedPatchesCounter> GNU_SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"GNU - successfully applied patches");

	private static final AnalysisResult.ResultKey<MPATCHErrorPatchesCounter> MPATCH_ERROR_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"MPATCH - error patches");
	private static final AnalysisResult.ResultKey<MPATCHRejectedPatchesCounter> MPATCH_REJECTED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"MPATCH - rejected patches");
	private static final AnalysisResult.ResultKey<MPATCHIncorrectlyAppliedPatchesCounter> MPATCH_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"MPATCH - incorrectly applied patches");
	private static final AnalysisResult.ResultKey<MPATCHSuccessfullyAppliedPatchesCounter> MPATCH_SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"MPATCH - successfully applied patches");

	private static final AnalysisResult.ResultKey<GNUViewErrorPatchesCounter> GNUVIEW_ERROR_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"GNU View - error patches");
	private static final AnalysisResult.ResultKey<GNUViewRejectedPatchesCounter> GNUVIEW_REJECTED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"GNU View - rejected patches");
	private static final AnalysisResult.ResultKey<GNUViewIncorrectlyAppliedPatchesCounter> GNUVIEW_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"GNU View - incorrectly applied patches");
	private static final AnalysisResult.ResultKey<GNUViewSuccessfullyAppliedPatchesCounter> GNUVIEW_SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"GNU View - successfully applied patches");

	private static final AnalysisResult.ResultKey<MPATCHViewErrorPatchesCounter> MPATCHVIEW_ERROR_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"MPATCH View - error patches");
	private static final AnalysisResult.ResultKey<MPATCHViewRejectedPatchesCounter> MPATCHVIEW_REJECTED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"MPATCH View - rejected patches");
	private static final AnalysisResult.ResultKey<MPATCHViewIncorrectlyAppliedPatchesCounter> MPATCHVIEW_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"MPATCH View - incorrectly applied patches");
	private static final AnalysisResult.ResultKey<MPATCHViewSuccessfullyAppliedPatchesCounter> MPATCHVIEW_SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"MPATCH View - successfully applied patches");

	private static final AnalysisResult.ResultKey<SkippedPatchesCounter> SKIPPED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"skipped patches");

	private static final String PATCH = "patch.txt";
	private static final String PATCH_VIEW = "patch2.txt";
	private static final String CODE = "code.txt";
	private static final String CODE_VIEW = "code2.txt";

	private int commits = 0;
	private static int incorrectPatchesPT = 0;

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

	private static class MPATCHViewSuccessfullyAppliedPatchesCounter
			extends SimpleMetadata<Integer, MPATCHViewSuccessfullyAppliedPatchesCounter> {
		public MPATCHViewSuccessfullyAppliedPatchesCounter() {
			super(0, "MPATCH (View) - successfully applied patches", Integer::sum);
		}
	}

	private static class SkippedPatchesCounter extends SimpleMetadata<Integer, SkippedPatchesCounter> {
		public SkippedPatchesCounter() {
			super(0, "skipped patches", Integer::sum);
		}
	}

	@Override
	public void initializeResults(Analysis analysis) {
		analysis.append(PT_ERROR_PATCHES_COUNTER_RESULT_KEY, new PTErrorPatchesCounter());
		analysis.append(PT_REJECTED_PATCHES_COUNTER_RESULT_KEY, new PTRejectedPatchesCounter());
		analysis.append(PT_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY, new PTIncorrectlyAppliedPatchesCounter());
		analysis.append(PT_SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY, new PTSuccessfullyAppliedPatchesCounter());
		analysis.append(GNU_ERROR_PATCHES_COUNTER_RESULT_KEY, new GNUErrorPatchesCounter());
		analysis.append(GNU_REJECTED_PATCHES_COUNTER_RESULT_KEY, new GNURejectedPatchesCounter());
		analysis.append(GNU_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY, new GNUIncorrectlyAppliedPatchesCounter());
		analysis.append(GNU_SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY,
				new GNUSuccessfullyAppliedPatchesCounter());
		analysis.append(MPATCH_ERROR_PATCHES_COUNTER_RESULT_KEY, new MPATCHErrorPatchesCounter());
		analysis.append(MPATCH_REJECTED_PATCHES_COUNTER_RESULT_KEY, new MPATCHRejectedPatchesCounter());
		analysis.append(MPATCH_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY,
				new MPATCHIncorrectlyAppliedPatchesCounter());
		analysis.append(MPATCH_SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY,
				new MPATCHSuccessfullyAppliedPatchesCounter());
		analysis.append(GNUVIEW_ERROR_PATCHES_COUNTER_RESULT_KEY, new GNUViewErrorPatchesCounter());
		analysis.append(GNUVIEW_REJECTED_PATCHES_COUNTER_RESULT_KEY, new GNUViewRejectedPatchesCounter());
		analysis.append(GNUVIEW_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY,
				new GNUViewIncorrectlyAppliedPatchesCounter());
		analysis.append(GNUVIEW_SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY,
				new GNUViewSuccessfullyAppliedPatchesCounter());
		analysis.append(MPATCHVIEW_ERROR_PATCHES_COUNTER_RESULT_KEY, new MPATCHViewErrorPatchesCounter());
		analysis.append(MPATCHVIEW_REJECTED_PATCHES_COUNTER_RESULT_KEY, new MPATCHViewRejectedPatchesCounter());
		analysis.append(MPATCHVIEW_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY,
				new MPATCHViewIncorrectlyAppliedPatchesCounter());
		analysis.append(MPATCHVIEW_SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY,
				new MPATCHViewSuccessfullyAppliedPatchesCounter());
		analysis.append(SKIPPED_PATCHES_COUNTER_RESULT_KEY, new SkippedPatchesCounter());
	}

	@Override
	public boolean beginCommit(Analysis analysis) throws Exception {
		++commits;
		return true;
	}

	@Override
	public boolean analyzeVariationDiff(Analysis analysis) throws Exception {
		VariationDiff<DiffLinesLabel> diff = analysis.getCurrentVariationDiff();
		PatchScenario<DiffLinesLabel> scenario = Generator.generatePatchScenario(diff);

		if (scenario == null) {
			// something went wrong when generating the patching scenario
			return true;
		}

		// TODO: Run Pia's new patcher here and store the result.
		Result<VariationTree<DiffLinesLabel>, Error> patchTransformerResult = Generator.runPatchTransformer(
				scenario.sourcePatch, scenario.targetVariantBefore, scenario.sourceVariantConfig,
				scenario.targetVariantConfig);

		patchTransformerResult.match(tree -> {
			if (tree != null && Patching.arePatchedVariantsEquivalent(tree,
					scenario.sourceVariantAfterRedToCrossVarFeatures, scenario.targetVariantBeforeRedToUnchanged,
					scenario.sourceVariantConfig, scenario.unchangedAfter)) {
				analysis.get(PT_SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY).value++;
			} else {
				PatchingExperiment.incorrectPatchesPT++;
				PatchingExperiment.writeToFile(analysis.getCurrentPatch(),
						"patch" + PatchingExperiment.incorrectPatchesPT + ".diff");
				analysis.get(PT_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY).value++;
			}
		}, error -> analysis.get(PT_REJECTED_PATCHES_COUNTER_RESULT_KEY).value++);

		Result<VariationTree<DiffLinesLabel>, Error> gnuPatchResult;
		try {
			gnuPatchResult = Generator.runGnuPatch(scenario.targetVariantBefore, PATCH, CODE);
			gnuPatchResult.match(tree -> tree != null && Patching.arePatchedVariantsEquivalent(tree,
					scenario.sourceVariantAfterRedToCrossVarFeatures, scenario.targetVariantBeforeRedToUnchanged,
					scenario.sourceVariantConfig, scenario.unchangedAfter)
							? analysis.get(GNU_SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY).value++
							: analysis.get(GNU_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY).value++,
					error -> analysis.get(GNU_REJECTED_PATCHES_COUNTER_RESULT_KEY).value++);
		} catch (IOException e) {
			analysis.get(GNU_ERROR_PATCHES_COUNTER_RESULT_KEY).value++;
		}

		Result<VariationTree<DiffLinesLabel>, Error> gnuPatchResultView;
		try {
			gnuPatchResultView = Generator.runGnuPatch(scenario.targetVariantBefore, PATCH_VIEW, CODE_VIEW);
			gnuPatchResultView.match(tree -> tree != null && Patching.arePatchedVariantsEquivalent(tree,
					scenario.sourceVariantAfterRedToCrossVarFeatures, scenario.targetVariantBeforeRedToUnchanged,
					scenario.sourceVariantConfig, scenario.unchangedAfter)
							? analysis.get(GNUVIEW_SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY).value++
							: analysis.get(GNUVIEW_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY).value++,
					error -> analysis.get(GNUVIEW_REJECTED_PATCHES_COUNTER_RESULT_KEY).value++);
		} catch (IOException e) {
			analysis.get(GNUVIEW_ERROR_PATCHES_COUNTER_RESULT_KEY).value++;
		}

		Result<VariationTree<DiffLinesLabel>, Error> mpatchResult;
		try {
			mpatchResult = Generator.runMPatch(scenario.targetVariantBefore, PATCH, CODE);
			mpatchResult.match(tree -> tree != null && Patching.arePatchedVariantsEquivalent(tree,
					scenario.sourceVariantAfterRedToCrossVarFeatures, scenario.targetVariantBeforeRedToUnchanged,
					scenario.sourceVariantConfig, scenario.unchangedAfter)
							? analysis.get(MPATCH_SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY).value++
							: analysis.get(MPATCH_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY).value++,
					error -> analysis.get(MPATCH_REJECTED_PATCHES_COUNTER_RESULT_KEY).value++);
		} catch (IOException e) {
			analysis.get(MPATCH_ERROR_PATCHES_COUNTER_RESULT_KEY).value++;
		}

		Result<VariationTree<DiffLinesLabel>, Error> mpatchResultView;
		try {
			mpatchResultView = Generator.runMPatch(scenario.targetVariantBefore, PATCH_VIEW, CODE_VIEW);
			mpatchResultView.match(tree -> tree != null && Patching.arePatchedVariantsEquivalent(tree,
					scenario.sourceVariantAfterRedToCrossVarFeatures, scenario.targetVariantBeforeRedToUnchanged,
					scenario.sourceVariantConfig, scenario.unchangedAfter)
							? analysis.get(MPATCHVIEW_SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY).value++
							: analysis.get(MPATCHVIEW_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY).value++,
					error -> analysis.get(MPATCHVIEW_REJECTED_PATCHES_COUNTER_RESULT_KEY).value++);
		} catch (IOException e) {
			analysis.get(MPATCHVIEW_ERROR_PATCHES_COUNTER_RESULT_KEY).value++;
		}

//		VariationTree<DiffLinesLabel> before = diff.project(Time.BEFORE).deepCopy();
//		VariationTree<DiffLinesLabel> after = diff.project(Time.AFTER).deepCopy();
//		try {
//			VariationDiff<DiffLinesLabel> patchedVariant = Patching.patch(diff, before, false, true);
//
//			if (!Patching.comparePatchedVariantWithExpectedResult(patchedVariant.project(Time.AFTER), after)) {
//				analysis.get(PT_INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY).value++;
//				VariationDiff<DiffLinesLabel> diffCopy = diff.deepCopy();
//				VariationDiff<DiffLinesLabel> patchedCopy = patchedVariant.deepCopy();
//				CutNonEditedSubtrees.genericTransform(diffCopy);
//				CutNonEditedSubtrees.genericTransform(patchedCopy);
//				GameEngine.showAndAwaitAll(Show.diff(diffCopy), Show.diff(patchedCopy));
//
//			} else {
//				analysis.get(PT_SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY).value++;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			PatchingExperiment.writeToFile(lastPatch, "file6.diff");
//			throw new Exception("break");
////			analysis.get(REJECTED_PATCHES_COUNTER_RESULT_KEY).value++;
//		}
		return true;
	}

	public static void writeToFile(PatchDiff p, String filename) {
		try {
			File f = new File(Path.of("data", "examples", filename).toUri());
			f.createNewFile();
			BufferedWriter myWriter = new BufferedWriter(new FileWriter(f));
			myWriter.write(p.getDiff());
			myWriter.close();
		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	@Override
	public void endBatch(Analysis analysis) throws Exception {
		Logger.info("Batch done: {} commits analyzed", commits);
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
				repo -> new PatchDiffParseOptions(PatchDiffParseOptions.DiffStoragePolicy.REMEMBER_FULL_DIFF,
						new VariationDiffParseOptions(true, false)),
				defaultOptions.getFilterForRepo(), true, false);
		try {
			AnalysisRunner.run(analysisOptions, (repository, path) -> Analysis
					.forEachCommit(() -> PatchingExperiment.Create(repository, path, experiment), 1000, 4));
		} catch (Exception e) {
//			PatchingExperiment.writeToFile(experiment.getLastPatch(), "file7.diff");
//			e.printStackTrace();
		}
		try {
//			VariationDiff diff = Patching.patch(Patching.parseVariationDiffFromFile("test_exampleA.diff"),
//					Patching.parseVariationTreeFromFile("test_exampleB.cpp"), true, true);
//			System.out.println(diff.project(Time.AFTER).unparse());
//			Patching.testSomething(Patching.parseVariationDiffFromFile("equivEnrichedVariants.diff"),
//					Patching.parseVariationTreeFromFile("equivEnrichedVariants.cpp"),
//					Patching.parseVariationTreeFromFile("equivEnrichedVariantsBefore.cpp"));
//			Patching.testSomething(Patching.parseVariationDiffFromFile("example1.diff"), Patching.parseVariationTreeFromFile("example1_B.cpp"));
//			Patching.patchVariationTrees(Patching.parseVariationDiffFromFile("file5.diff"), Patching.parseVariationDiffFromFile("file5.diff").project(Time.BEFORE), true, true);
//			Patching.patch(Patching.parseVariationDiffFromFiles("variantAversion1.cpp", "variantAversion2.cpp"), Patching.parseVariationTreeFromFile("variantBversion1.cpp"), true, true);
//			Patching.patchVariationTrees(Patching.parseVariationDiffFromFiles("exampleA1Rem.cpp", "exampleA2Rem.cpp"), Patching.parseVariationTreeFromFile("exampleBRem.cpp"), true, true);
//			VariationDiff diff = Patching.patchVariationTrees(Patching.parseVariationDiffFromFiles("exampleA1RemAdd.cpp", "exampleA2RemAdd.cpp"),
//					Patching.parseVariationTreeFromFile("exampleBRemAdd.cpp"), true, true);
//			Patching.patchVariationTrees(Patching.parseVariationDiffFromFile("exampleCompareAncestors.diff"), Patching.parseVariationTreeFromFile("exampleCompareAncestorsB.cpp"), true, false);
//			VariationTree<DiffLinesLabel> patchedVariant = patchVariationTrees(
//					parseVariationDiffFromFiles("exampleA1RemAdd.cpp", "exampleA2RemAdd.cpp"),
//					parseVariationTreeFromFile("exampleBRemAdd.cpp"));
//			VariationTree<DiffLinesLabel> expectedResult = parseVariationTreeFromFile("exampleBRemAddExpected.cpp");
//			System.out.println(comparePatchedVariantWithExpectedResult(patchedVariant, expectedResult));
//			GameEngine.showAndAwaitAll(Show.diff(Patching.parseVariationDiffFromFile("presentationExample.diff")));
//			
//			VariationDiff<DiffLinesLabel> patchedVariant = Patching.patchVariationTrees(
//					Patching.parseVariationDiffFromFiles("exampleA1NodesWith2Parents.cpp",
//							"exampleA2NodesWith2Parents.cpp"),
//					Patching.parseVariationTreeFromFile("exampleA1NodesWith2Parents.cpp"), true, true);
//			VariationTree<DiffLinesLabel> expectedResult = Patching
//					.parseVariationTreeFromFile("exampleA2NodesWith2Parents.cpp");
//			GameEngine.showAndAwaitAll(Show.tree(patchedVariant.project(Time.AFTER)), Show.tree(expectedResult));

//			System.out.println(Patching.comparePatchedVariantWithExpectedResult(patchedVariant, expectedResult));
//			VariationDiff<DiffLinesLabel> patchedVariant = Patching.patchVariationTrees(
//					Patching.parseVariationDiffFromFile("example1.diff"),
//					Patching.parseVariationTreeFromFile("example2_B.cpp"), true, true);
//			VariationDiff<DiffLinesLabel> diff = Patching.parseVariationDiffFromFile("motivating_exA_view.diff");
//			VariationTree<DiffLinesLabel> tree = Patching.parseVariationTreeFromFile("motivating_exB.cpp");
//			GameEngine.showAndAwaitAll( Show.tree(diff.project(Time.BEFORE)));
//			VariationTree<DiffLinesLabel> expectedResult = parseVariationTreeFromFile("exampleA2RemAdd.cpp");
//			System.out.println(comparePatchedVariantWithExpectedResult(patchedVariant, expectedResult));

//			patchVariationTrees(parseVariationDiffFromFiles("exampleA1AddAlignmentP.cpp", "exampleA2AddAlignmentP.cpp"),
//					parseVariationTreeFromFile("exampleBAddAlignmentP.cpp"));
//			patchVariationTrees(parseVariationDiffFromFiles("exampleA1RemAlignmentP.cpp", "exampleA2RemAlignmentP.cpp"),
//					parseVariationTreeFromFile("exampleBRemAlignmentP.cpp"));
//			patchVariationTrees(parseVariationDiffFromFiles("exampleA1RemAddAlignmentP.cpp", "exampleA2RemAddAlignmentP.cpp"),
//					parseVariationTreeFromFile("exampleBRemAddAlignmentP.cpp"));
		} catch (Exception e) {
			System.out.println("Rejected");
			e.printStackTrace();
		}
	}

}
