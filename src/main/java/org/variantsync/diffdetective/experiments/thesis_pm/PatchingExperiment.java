package org.variantsync.diffdetective.experiments.thesis_pm;

import java.io.IOException;
import java.nio.file.Path;

import org.variantsync.diffdetective.AnalysisRunner;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.patching.Patching;
import org.variantsync.diffdetective.variation.diff.transform.CutNonEditedSubtrees;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.*;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.show.Show;
import org.variantsync.diffdetective.show.engine.GameEngine;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.VariationDiff;

public class PatchingExperiment implements Analysis.Hooks {

	private static final AnalysisResult.ResultKey<RejectedPatchesCounter> REJECTED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"rejected patches");
	private static final AnalysisResult.ResultKey<IncorrectlyAppliedPatchesCounter> INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"incorrectly applied patches");
	private static final AnalysisResult.ResultKey<SuccessfullyAppliedPatchesCounter> SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"successfully applied patches");
	private static final AnalysisResult.ResultKey<SkippedPatchesCounter> SKIPPED_PATCHES_COUNTER_RESULT_KEY = new AnalysisResult.ResultKey<>(
			"skipped patches");
	private int commits = 0;
	private boolean firstDiff = true;
	private VariationDiff<DiffLinesLabel> firstIncorrectlyPatchedDiff;

	private static class RejectedPatchesCounter extends SimpleMetadata<Integer, RejectedPatchesCounter> {
		public RejectedPatchesCounter() {
			super(0, "rejected patches", Integer::sum);
		}
	}

	private static class IncorrectlyAppliedPatchesCounter
			extends SimpleMetadata<Integer, IncorrectlyAppliedPatchesCounter> {
		public IncorrectlyAppliedPatchesCounter() {
			super(0, "incorrectly applied patches", Integer::sum);
		}
	}

	private static class SuccessfullyAppliedPatchesCounter
			extends SimpleMetadata<Integer, SuccessfullyAppliedPatchesCounter> {
		public SuccessfullyAppliedPatchesCounter() {
			super(0, "successfully applied patches", Integer::sum);
		}
	}

	private static class SkippedPatchesCounter extends SimpleMetadata<Integer, SkippedPatchesCounter> {
		public SkippedPatchesCounter() {
			super(0, "skipped patches", Integer::sum);
		}
	}

	public VariationDiff<DiffLinesLabel> getFirstIncorrectlyPatchedDiff() {
		return this.firstIncorrectlyPatchedDiff;
	}

	@Override
	public void initializeResults(Analysis analysis) {
		analysis.append(REJECTED_PATCHES_COUNTER_RESULT_KEY, new RejectedPatchesCounter());
		analysis.append(INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY, new IncorrectlyAppliedPatchesCounter());
		analysis.append(SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY, new SuccessfullyAppliedPatchesCounter());
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
		VariationTree<DiffLinesLabel> before = diff.project(Time.BEFORE).deepCopy();
		VariationTree<DiffLinesLabel> after = diff.project(Time.AFTER).deepCopy();
		try {
			VariationDiff<DiffLinesLabel> patchedVariant = Patching.patchVariationTrees(diff, before, false, true);

			if (!Patching.comparePatchedVariantWithExpectedResult(patchedVariant.project(Time.AFTER), after)) {
				analysis.get(INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY).value++;
				VariationDiff<DiffLinesLabel> diffCopy = diff.deepCopy();
				VariationDiff<DiffLinesLabel> patchedCopy = patchedVariant.deepCopy();
				CutNonEditedSubtrees.genericTransform(diffCopy);
				CutNonEditedSubtrees.genericTransform(patchedCopy);
				GameEngine.showAndAwaitAll(Show.diff(diffCopy), Show.diff(patchedCopy));
//				if (firstDiff) {
//					Patching.patchVariationTrees(diff, before, true, true);
//					wait(60000);
//					firstDiff = false;
//				}
				
			} else {
				analysis.get(SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY).value++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			analysis.get(REJECTED_PATCHES_COUNTER_RESULT_KEY).value++;
		}
		return true;
	}

	@Override
	public void endBatch(Analysis analysis) throws Exception {
		Logger.info("Batch done: {} commits analyzed", commits);
	}

	public static Analysis Create(Repository repo, Path outputDirectory, PatchingExperiment experiment) {
		return new Analysis("my analysis", List.of(
				experiment, new StatisticsAnalysis()
//	           , new EditClassValidation()
		), repo, outputDirectory);
	}

	public static void main(String[] args) {
//		PatchingExperiment experiment = new PatchingExperiment();
//		try {
//			AnalysisRunner.run(
//					new AnalysisRunner.Options(Path.of("data", "repos"), Path.of("data", "output"),
//							Path.of("data", "demo-dataset.md")),
//					(repository, path) -> Analysis.forEachCommit(() -> PatchingExperiment.Create(repository, path, experiment), 20,
//							1));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		try {
//			Patching.patchVariationTrees(Patching.parseVariationDiffFromFiles("exampleA1Add.cpp", "exampleA2Add.cpp"), Patching.parseVariationTreeFromFile("exampleBAdd.cpp"), true, true);
//			Patching.patchVariationTrees(Patching.parseVariationDiffFromFiles("exampleA1Rem.cpp", "exampleA2Rem.cpp"), Patching.parseVariationTreeFromFile("exampleBRem.cpp"), true, true);
			Patching.patchVariationTrees(Patching.parseVariationDiffFromFiles("exampleA1RemAdd.cpp", "exampleA2RemAdd.cpp"),
					Patching.parseVariationTreeFromFile("exampleA1RemAdd.cpp"), true, true);
//			Patching.patchVariationTrees(Patching.parseVariationDiffFromFile("exampleCompareAncestors.diff"), Patching.parseVariationTreeFromFile("exampleCompareAncestorsB.cpp"), true, false);
//			VariationTree<DiffLinesLabel> patchedVariant = patchVariationTrees(
//					parseVariationDiffFromFiles("exampleA1RemAdd.cpp", "exampleA2RemAdd.cpp"),
//					parseVariationTreeFromFile("exampleBRemAdd.cpp"));
//			VariationTree<DiffLinesLabel> expectedResult = parseVariationTreeFromFile("exampleBRemAddExpected.cpp");
//			System.out.println(comparePatchedVariantWithExpectedResult(patchedVariant, expectedResult));

//			VariationTree<DiffLinesLabel> patchedVariant = Patching.patchVariationTrees(
//					Patching.parseVariationDiffFromFiles("exampleA1NodesWith2Parents.cpp",
//							"exampleA2NodesWith2Parents.cpp"),
//					Patching.parseVariationTreeFromFile("exampleA1NodesWith2Parents.cpp"), true);
//			VariationTree<DiffLinesLabel> expectedResult = Patching
//					.parseVariationTreeFromFile("exampleA2NodesWith2Parents.cpp");
//			GameEngine.showAndAwaitAll(Show.tree(patchedVariant), Show.tree(expectedResult));
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
