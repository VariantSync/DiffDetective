package org.variantsync.diffdetective.experiments.thesis_pm;

import java.io.IOException;
import java.nio.file.Path;

import org.variantsync.diffdetective.AnalysisRunner;
import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.patching.Patching;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import java.util.List;
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
	private int commits = 0;
	private boolean firstDiff = true;

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

	@Override
	public void initializeResults(Analysis analysis) {
		analysis.append(REJECTED_PATCHES_COUNTER_RESULT_KEY, new RejectedPatchesCounter());
		analysis.append(INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY, new IncorrectlyAppliedPatchesCounter());
		analysis.append(SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY, new SuccessfullyAppliedPatchesCounter());
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
			VariationTree<DiffLinesLabel> patchedVariant = Patching.patchVariationTrees(diff, before, firstDiff);
			firstDiff = false;

			if (!Patching.comparePatchedVariantWithExpectedResult(patchedVariant, after)) {
				analysis.get(INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY).value++;
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

	public static Analysis Create(Repository repo, Path outputDirectory) {
		return new Analysis("my analysis", List.of(new PatchingExperiment()
//	                        , new StatisticsAnalysis()
//	                        , new EditClassValidation()
		), repo, outputDirectory);
	}

	public static void main(String[] args) {
		try {
			AnalysisRunner.run(
					new AnalysisRunner.Options(Path.of("data", "repos"), Path.of("data", "output"),
							Path.of("data", "demo-dataset.md")),
					(repository, path) -> Analysis.forEachCommit(() -> PatchingExperiment.Create(repository, path), 20,
							8));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
//			patchVariationTrees(parseVariationTreeFromFile("exampleA1Add.cpp"),
//					parseVariationTreeFromFile("exampleA2Add.cpp"), parseVariationTreeFromFile("exampleBAdd.cpp"));
//			patchVariationTrees(parseVariationTreeFromFile("exampleA1Rem.cpp"),
//					parseVariationTreeFromFile("exampleA2Rem.cpp"), parseVariationTreeFromFile("exampleBRem.cpp"));
//			patchVariationTrees(parseVariationDiffFromFiles("exampleA1RemAdd.cpp", "exampleA2RemAdd.cpp"),
//					parseVariationTreeFromFile("exampleBRemAdd.cpp"));

//			VariationTree<DiffLinesLabel> patchedVariant = patchVariationTrees(
//					parseVariationDiffFromFiles("exampleA1RemAdd.cpp", "exampleA2RemAdd.cpp"),
//					parseVariationTreeFromFile("exampleBRemAdd.cpp"));
//			VariationTree<DiffLinesLabel> expectedResult = parseVariationTreeFromFile("exampleBRemAddExpected.cpp");
//			System.out.println(comparePatchedVariantWithExpectedResult(patchedVariant, expectedResult));

			VariationTree<DiffLinesLabel> patchedVariant = Patching.patchVariationTrees(
					Patching.parseVariationDiffFromFiles("exampleA1NodesWith2Parents.cpp", "exampleA2NodesWith2Parents.cpp"),
					Patching.parseVariationTreeFromFile("exampleA1NodesWith2Parents.cpp"), false);
			VariationTree<DiffLinesLabel> expectedResult = Patching.parseVariationTreeFromFile("exampleA2NodesWith2Parents.cpp");
//			GameEngine.showAndAwaitAll(Show.tree(patchedVariant), Show.tree(expectedResult));
			System.out.println(Patching.comparePatchedVariantWithExpectedResult(patchedVariant, expectedResult));

//			VariationTree<DiffLinesLabel> patchedVariant = patchVariationTrees(
//					parseVariationDiffFromFiles("exampleA1RemAdd.cpp", "exampleA2RemAdd.cpp"),
//					parseVariationTreeFromFile("exampleA1RemAdd.cpp"));
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
