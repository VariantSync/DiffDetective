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
import java.util.List;

import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.*;
import org.variantsync.diffdetective.datasets.PatchDiffParseOptions;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.git.PatchDiff;
import org.variantsync.diffdetective.show.Show;
import org.variantsync.diffdetective.show.engine.GameEngine;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;

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
	private PatchDiff lastPatch;

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

	public PatchDiff getLastPatch() {
		return this.lastPatch;
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
		lastPatch = analysis.getCurrentPatch();
		
		VariationTree<DiffLinesLabel> before = diff.project(Time.BEFORE).deepCopy();
		VariationTree<DiffLinesLabel> after = diff.project(Time.AFTER).deepCopy();
		try {
			VariationDiff<DiffLinesLabel> patchedVariant = Patching.patch(diff, before, false, true);

			if (!Patching.comparePatchedVariantWithExpectedResult(patchedVariant.project(Time.AFTER), after)) {
				analysis.get(INCORRECTLY_APPLIED_PATCHES_COUNTER_RESULT_KEY).value++;
				VariationDiff<DiffLinesLabel> diffCopy = diff.deepCopy();
				VariationDiff<DiffLinesLabel> patchedCopy = patchedVariant.deepCopy();
				CutNonEditedSubtrees.genericTransform(diffCopy);
				CutNonEditedSubtrees.genericTransform(patchedCopy);
				GameEngine.showAndAwaitAll(Show.diff(diffCopy), Show.diff(patchedCopy));
				
			} else {
				analysis.get(SUCCESSFULLY_APPLIED_PATCHES_COUNTER_RESULT_KEY).value++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			PatchingExperiment.writeToFile(lastPatch, "file6.diff");
			throw new Exception("break");
//			analysis.get(REJECTED_PATCHES_COUNTER_RESULT_KEY).value++;
		}
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
		return new Analysis("my analysis", List.of(
				experiment, new StatisticsAnalysis()
//	           , new EditClassValidation()
		), repo, outputDirectory);
	}

	public static void main(String[] args) {
		PatchingExperiment experiment = new PatchingExperiment();
		final AnalysisRunner.Options defaultOptions = AnalysisRunner.Options.DEFAULT(args);
		final AnalysisRunner.Options analysisOptions = new AnalysisRunner.Options(
				Path.of("data", "repos"), 
				Path.of("data", "output"),
				Path.of("data", "demo-dataset.md"),
                repo -> new PatchDiffParseOptions(
                        PatchDiffParseOptions.DiffStoragePolicy.DO_NOT_REMEMBER,
                        new VariationDiffParseOptions(
                                true,
                                false
                        )
                ),
                defaultOptions.getFilterForRepo(),
                true,
                false
        );
//		try {
//			AnalysisRunner.run(analysisOptions
//					,
//					(repository, path) -> Analysis.forEachCommit(() -> PatchingExperiment.Create(repository, path, experiment), 500,
//							8));
//		} catch (Exception e) {
//			PatchingExperiment.writeToFile(experiment.getLastPatch(), "file7.diff");
//			e.printStackTrace();
//		}
		try {
//			VariationDiff diff = Patching.patch(Patching.parseVariationDiffFromFile("test_exampleA.diff"),
//					Patching.parseVariationTreeFromFile("test_exampleB.cpp"), true, true);
//			System.out.println(diff.project(Time.AFTER).unparse());
			Patching.testSomething(Patching.parseVariationDiffFromFile("equivEnrichedVariants.diff"),Patching.parseVariationTreeFromFile("equivEnrichedVariants.cpp"), Patching.parseVariationTreeFromFile("equivEnrichedVariantsBefore.cpp"));
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
