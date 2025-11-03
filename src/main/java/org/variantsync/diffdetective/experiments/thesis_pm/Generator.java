package org.variantsync.diffdetective.experiments.thesis_pm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.eclipse.jgit.diff.DiffAlgorithm;
import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.git.PatchDiff;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.shell.DiffCommand;
import org.variantsync.diffdetective.shell.GnuPatchCommand;
import org.variantsync.diffdetective.shell.MPatchCommand;
import org.variantsync.diffdetective.shell.ShellException;
import org.variantsync.diffdetective.shell.ShellExecutor;
import org.variantsync.diffdetective.shell.SimpleCommand;
import org.variantsync.diffdetective.show.Show;
import org.variantsync.diffdetective.show.engine.GameEngine;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;
import org.variantsync.diffdetective.variation.diff.patching.Patching;
import org.variantsync.diffdetective.variation.diff.transform.EliminateEmptyAlternatives;
import org.variantsync.diffdetective.variation.diff.transform.RevertSomeChanges;
import org.variantsync.diffdetective.variation.diff.view.DiffView;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.diffdetective.variation.tree.view.TreeView;
import org.variantsync.diffdetective.variation.tree.view.relevance.Configure;
import org.variantsync.functjonal.Pair;
import org.variantsync.functjonal.Result;

public class Generator {

	private static Random rand1 = new Random(2025);
	private static Random rand2 = new Random(9);
	final private static String directory = "experiment";
	final private static String sourceVariant = "sourceVariant";
	final private static String version1 = "version1";
	final private static String version2 = "version2";
	final private static String targetVariant = "targetVariant";
	final private static String code = "code.txt";
	final private static String patch = "patch.txt";
	final private static ShellExecutor shellSourceVariantDir = new ShellExecutor(Logger::info, Logger::error,
			Path.of(directory, sourceVariant));
	final private static ShellExecutor shellTargetVariantDir = new ShellExecutor(Logger::info, Logger::error,
			Path.of(directory, targetVariant));

	enum Error {
		FAILED, ERROR
	};

	/**
	 * This function mutates a boolean assignment by flipping some values by chance.
	 * This function is pure: It creates a new map and the input map is left
	 * unchanged. A value is flipped with a chance of (100*probability)%. On
	 * average, (100*probability)% of elements will end up flipped.
	 * 
	 * @param probability The probability for an element being assigned 'true'. Must
	 *                    be in range [0, 1].
	 */
	public static <T> Map<T, Boolean> mutateByWeightedCoinFlip(final Map<T, Boolean> a, double probability) {
		Assert.assertTrue(0 <= probability);
		Assert.assertTrue(probability <= 1.0);

		final Map<T, Boolean> mutant = new HashMap<>();
		for (Entry<T, Boolean> e : a.entrySet()) {
			mutant.put(e.getKey(), rand2.nextDouble() <= probability ? !e.getValue() : e.getValue());
		}

		return mutant;
	}

	/**
	 * This function creates a random partition of the given set into two subsets.
	 * The output set is created by virtual coin flip on every element in the input
	 * set. On average, (100*probability)% of elements will end up in the 'true' set
	 * and (100*(1-probability))% elements will end up in the 'false' set.
	 * 
	 * @param probability The probability for an element being assigned 'true'. Must
	 *                    be in range [0, 1].
	 */
	public static <T> Map<T, Boolean> randomPartition(Set<T> s, double probability) {
		Assert.assertTrue(0 <= probability);
		Assert.assertTrue(probability <= 1.0);

		final Map<T, Boolean> subsets = new HashMap<>();
		for (T t : s) {
			subsets.put(t, rand1.nextDouble() <= probability);
		}
		return subsets;
	}

	private static void logDiff(String title, String diff) {
		System.out.println("===== " + title + " =====\n" + diff + "\n===============\n");
	}

	// function to delete subdirectories and files:
	// https://www.geeksforgeeks.org/java/java-program-to-delete-a-directory/
	public static void deleteDirectory(File file) {
		if (file.listFiles() != null) {
			for (File subfile : file.listFiles()) {
				if (subfile.isDirectory()) {
					deleteDirectory(subfile);
				}
				subfile.delete();
			}
		}

	}

	public static void writeToFile(String text, Path filePath) {
		try {
			File f = new File(filePath.toUri());
			f.createNewFile();
			BufferedWriter myWriter = new BufferedWriter(new FileWriter(f));
			myWriter.write(text);
			myWriter.close();
		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	public static void writeToFile(List<String> text, Path filePath) {
		try {
			File f = new File(filePath.toUri());
			f.createNewFile();
			BufferedWriter myWriter = new BufferedWriter(new FileWriter(f));
			for (int i = 0; i < text.size(); i++) {
				myWriter.write(text.get(i));
				myWriter.newLine();
			}
			myWriter.close();
		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	public static <L extends Label> PatchScenario<L> generatePatchScenario(VariationDiff<L> spl) throws Exception {
		// ## 1. Sample two variants.
		// Since we have no feature model, we create a naive problem space model:
		// We just collect all features without constraints.
		final Set<String> featureModel = spl.computeAllFeatureNames();
//		Logger.info("Extracted feature names: {}", featureModel);

		// To sample variants, we just pick a random subset of features to set to true,
		// set the rest to false
		// We could use more sophisticated algorithms here, for example based on how
		// often features occur.
		// Maybe should ask Sebastian.
		// The configurations we produce are complete.
		// Hypothesis: the lower the probability value (i.e., the more deselected
		// features), the harder the patching challenge.

		mutateByWeightedCoinFlip(randomPartition(featureModel, 0.6), 0.5);
		randomPartition(featureModel, 0.6);
		mutateByWeightedCoinFlip(randomPartition(featureModel, 0.6), 0.5);
		final Map<String, Boolean> config1 = randomPartition(featureModel, 0.6);
		final Map<String, Boolean> config2 = mutateByWeightedCoinFlip(config1, 0.5);
		// FIXME: We should probably ensure that config1 != config2.
		Logger.info("Configuration 1: {}", config1);
		Logger.info("Configuration 2: {}", config2);
		// TODO: We could also distinguish the two major scenarios from these
		// configurations:
		// There are features in source that are not in target and vice versa? (assumes
		// that nothing is labeled to the negation of a feature)
		// TODO: Decide how many variants we want to generate per commit / patch and how
		// we want to compare them? Have every generated variant be the source once? I
		// think this is what Alex did in his ICSME'22 paper.

		// To configure our variation trees and diffs, we need to convert our
		// configurations to relevance predicates.
		final Configure configureTo1 = new Configure(config1);
		final Configure configureTo2 = new Configure(config2);

		// ## 2. We need two variants and two versions of each variant.

		VariationDiff<L> sourcePatchRaw = DiffView.optimized(spl, configureTo1); // input patch to apply to the
																					// target variant
		VariationDiff<L> targetPatch = DiffView.optimized(spl, configureTo2); // ground truth for target patch;
																				// this is the "perfect" target
																				// patch
		// Eliminate empty alternatives in source and target patch
		VariationTree<L> sourceBefore = sourcePatchRaw.project(Time.BEFORE);
		new EliminateEmptyAlternatives().transform((VariationTree<DiffLinesLabel>) sourceBefore);
		VariationTree<L> sourceAfter = sourcePatchRaw.project(Time.AFTER);
		new EliminateEmptyAlternatives().transform((VariationTree<DiffLinesLabel>) sourceAfter);
		VariationDiff<L> sourcePatchElimEmptyAlt;
		try {
			sourcePatchElimEmptyAlt = (VariationDiff<L>) VariationDiff.fromLines(sourceBefore.unparse(),
					sourceAfter.unparse(), DiffAlgorithm.SupportedAlgorithm.MYERS, VariationDiffParseOptions.Default);
		} catch (DiffParseException e) {
			return null;
		}

		final VariationDiff<L> sourcePatch = sourcePatchElimEmptyAlt;

		VariationTree<L> before = targetPatch.project(Time.BEFORE);
		new EliminateEmptyAlternatives().transform((VariationTree<DiffLinesLabel>) before);
		VariationTree<L> after = targetPatch.project(Time.AFTER);
		new EliminateEmptyAlternatives().transform((VariationTree<DiffLinesLabel>) after);
		VariationDiff<L> targetPatchElimEmptyAlt;
		try {
			targetPatchElimEmptyAlt = (VariationDiff<L>) VariationDiff.fromLines(before.unparse(), after.unparse(),
					DiffAlgorithm.SupportedAlgorithm.MYERS, VariationDiffParseOptions.Default);
		} catch (DiffParseException e) {
			return null;
		}

		VariationDiff<L> targetPatchModified = targetPatchElimEmptyAlt.deepCopy();

		// split nodes and subtrees with two parents
		Patching.resolve((DiffNode<DiffLinesLabel>) targetPatchElimEmptyAlt.getRoot(),
				(VariationDiff<DiffLinesLabel>) targetPatchModified);

		VariationDiff<L> targetView = DiffView.optimized(targetPatchModified.deepCopy(), configureTo1);

//		GameEngine.showAndAwaitAll(Show.diff(targetPatchModified));
//		logDiff("modified target patch (before)", targetPatchModified.project(Time.BEFORE).unparse());

//		GameEngine.showAndAwaitAll(Show.diff(sourcePatch, "source patch"),
//				Show.diff(targetPatchModified, "target patch elim empty altern and resolved"),
//				Show.diff(targetPatchElimEmptyAlt, "target patch elim empty altern"),
//				Show.diff(targetView, "V' (from target patch)"));

		// revert the changes made in B but not in A
		new RevertSomeChanges<DiffLinesLabel>(node -> {
			if (targetView.getNodeWithID(node.getID()) == null && !node.isNon()) {
				return true;
			}
			return false;
		}).transform((VariationDiff<DiffLinesLabel>) targetPatchModified);

		targetPatch = targetPatchModified;

//		GameEngine.showAndAwaitAll(Show.diff(sourcePatch, "source patch"), Show.diff(targetPatch, "target patch"));

		// FIXME: Maybe we want to distinguish cases where one of the patches (or both)
		// are empty (i.e., noop / id)?

		final VariationTree<L> sourceVariantBefore = sourcePatch.project(Time.BEFORE); // input
		final VariationTree<L> sourceVariantAfter = sourcePatch.project(Time.AFTER); // input
		final VariationTree<L> targetVariantBefore = targetPatch.project(Time.BEFORE); // input
		final VariationTree<L> targetVariantAfter = targetPatch.project(Time.AFTER); // ground truth for how the patched
																						// target variant should ideally
																						// look like

		// GameEngine.showAndAwaitAll(Show.diff(spl));
//		GameEngine.showAndAwaitAll(Show.diff(sourcePatch, "Source Patch " + config1),
//				Show.diff(targetPatch, "Target Patch " + config2));
//		GameEngine.showAndAwaitAll(Show.tree(sourceVariantBefore, "Source Before " + config1),
//				Show.tree(sourceVariantAfter, "Source After " + config1),
//				Show.tree(targetVariantBefore, "Target Before " + config2),
//				Show.tree(targetVariantAfter, "Target After" + config2));

		// ## 3. To use command-line patchers such as GNU patch and mpatch, we need to
		// write our variants to disk.
		final String sourceVariantCodeBefore = sourceVariantBefore.unparse();
		final String sourceVariantCodeAfter = sourceVariantAfter.unparse();
		final String targetVariantCodeBefore = targetVariantBefore.unparse();
		final String targetVariantCodeAfter = targetVariantAfter.unparse(); // ground truth for fast comparisons (beware
																			// of differences in line breaks and
																			// whitespaces!)
//		logDiff("Source Before:", sourceVariantCodeBefore);
//		logDiff("Source After:", sourceVariantCodeAfter);
//		logDiff("Target Before:", targetVariantCodeBefore);
//		logDiff("Target After:", targetVariantCodeAfter);

		File f = new File(Path.of(directory).toUri());
		deleteDirectory(f);
		f.delete();

		if (!(new File(Path.of(directory, targetVariant).toUri())).mkdirs()
				|| !(new File(Path.of(directory, sourceVariant).toUri())).mkdir()) {
			throw new Exception("Failed to create directories");
		}
		if (!(new File(Path.of(directory, sourceVariant, version1).toUri())).mkdirs()
				|| !(new File(Path.of(directory, sourceVariant, version2).toUri())).mkdir()) {
			throw new Exception("Failed to create directories");
		}

		Path sourceVariantBeforePath = Path.of(directory, sourceVariant, version1, code);
		Path sourceVariantAfterPath = Path.of(directory, sourceVariant, version2, code);
		Path targetVariantBeforePath = Path.of(directory, targetVariant, code);
		Path patchPath = Path.of(directory, targetVariant, patch);
		writeToFile(sourceVariantCodeBefore, sourceVariantBeforePath);
		writeToFile(sourceVariantCodeAfter, sourceVariantAfterPath);
		writeToFile(targetVariantCodeBefore, targetVariantBeforePath);
//        writeToFile(targetVariantCodeAfter, targetVariantAfterPath);

		runGnuDiff(patchPath);

		return new PatchScenario<L>(sourcePatch, targetVariantBefore, targetPatch, targetVariantAfter, configureTo1,
				configureTo2);

	}

	private static void runGnuDiff(Path patchPath) {
		try {
			Path pathToVersion1Dir = Path.of(version1);
			Path pathToVersion2Dir = Path.of(version2);
			writeToFile(shellSourceVariantDir.execute(
					new DiffCommand("diff", "-Naur", pathToVersion1Dir.toString(), pathToVersion2Dir.toString())),
					patchPath);

//			System.out.println(list);
		} catch (ShellException e) {
			System.out.println(e);
		}
	}

	public static <L extends Label> void runPatchers(PatchScenario<L> scenario) {
		// ## 4. Run the patchers!
		boolean isMpatchCorrect = false;
		boolean isGnuPatchCorrect = false;
		boolean isPatchTransformerCorrect = false;
		List<GameEngine> gameEngine = new ArrayList<>();
		gameEngine.add(Show.tree(scenario.targetVariantBefore, "target variant before"));
		gameEngine.add(Show.diff(scenario.patchGroundTruth, "targetPatch"));
		gameEngine.add(Show.tree(scenario.patchedVariantGroundTruth, "ground truth"));

		// TODO: Run Pia's new patcher here and store the result.
		Result<VariationTree<DiffLinesLabel>, Error> patchTransformerResult = runPatchTransformer(scenario.sourcePatch,
				scenario.targetVariantBefore);
		if (patchTransformerResult.isSuccess()) {
			gameEngine.add(Show.tree(patchTransformerResult.getSuccess(), "patch transformer result"));
		}
		isPatchTransformerCorrect = patchTransformerResult.match(tree -> Patching.arePatchedVariantsEquivalent(tree,
				scenario.sourceVariantAfterRedToCrossVarFeatures, scenario.targetVariantBeforeRedToUnchanged,
				scenario.targetVariantConfig, scenario.unchangedAfter), error -> false);

		Result<VariationTree<DiffLinesLabel>, Error> gnuPatchResult;
		try {
			gnuPatchResult = runGnuPatch(scenario.targetVariantBefore);
			if (gnuPatchResult.isSuccess()) {
				gameEngine.add(Show.tree(gnuPatchResult.getSuccess(), "gnu patch result"));
			}
			isGnuPatchCorrect = gnuPatchResult.match(tree -> Patching.arePatchedVariantsEquivalent(tree,
					scenario.sourceVariantAfterRedToCrossVarFeatures, scenario.targetVariantBeforeRedToUnchanged,
					scenario.targetVariantConfig, scenario.unchangedAfter), error -> false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DiffParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Result<VariationTree<DiffLinesLabel>, Error> mpatchResult;
		try {
			mpatchResult = runMPatch(scenario.targetVariantBefore);
			if (mpatchResult.isSuccess()) {
				gameEngine.add(Show.tree(mpatchResult.getSuccess(), "mpatch result"));
			}
			isMpatchCorrect = mpatchResult.match(tree -> Patching.arePatchedVariantsEquivalent(tree,
					scenario.sourceVariantAfterRedToCrossVarFeatures, scenario.targetVariantBeforeRedToUnchanged,
					scenario.targetVariantConfig, scenario.unchangedAfter), error -> false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DiffParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// TODO: Read the results of the patchers. The patchers should produce the
		// target variants as string if they did not fail.
		GameEngine[] gameEngineArray = new GameEngine[gameEngine.size()];
		gameEngineArray = gameEngine.toArray(gameEngineArray);
//		GameEngine.showAndAwaitAll(gameEngineArray);

		// ## 5. Compare the results of patchers here!
		System.out.println("mpatch: " + isMpatchCorrect);
		System.out.println("GNU patch: " + isGnuPatchCorrect);
		System.out.println("patch transformer " + isPatchTransformerCorrect);
		// TODO
	}

	public static <L extends Label> Result<VariationTree<DiffLinesLabel>, Error> runMPatch(
			final VariationTree<L> targetVariantBefore) throws IOException, DiffParseException {
		// TODO: configure mpatch
		// reset target variant
		resetTargetVariantBefore(targetVariantBefore);
		Path mpatchPath = Path.of("..", "..", "..", "mpatch", "target", "debug", "mpatch");
		MPatchCommand command = new MPatchCommand(mpatchPath.toString(), "--strip", "1", "--sourcedir",
				Path.of("..", sourceVariant, version1).toString(), "--patchfile", patch);
		try {
			shellTargetVariantDir.execute(command);
		} catch (ShellException e) {
			// FIXME: When a shell exception occurs, we know that patching failed.
			// Either there is a bug or the patcher was not successful!
			// We should not throw and catch exceptions in these cases.
			// It is probably best to write new ShellCommand subclasses for mpatch and GNU
			// patch with a proper interpretResult method.
			// We have to distinguish patch success from patch failure anyway somewhere.
			Logger.error(e);
			return Result.Failure(Error.ERROR);
		}
		if (command.isPatchingSuccessful()) {
			VariationTree<DiffLinesLabel> mpatchResult = VariationTree
					.fromFile(Path.of(directory, targetVariant, code));
			return Result.Success(mpatchResult);
		}
		return Result.Failure(Error.FAILED);
	}

	private static <L extends Label> void resetTargetVariantBefore(final VariationTree<L> targetVariantBefore) {
		Path targetVariantBeforePath = Path.of(directory, targetVariant, code);
		writeToFile(targetVariantBefore.unparse(), targetVariantBeforePath);
	}

	public static <L extends Label> Result<VariationTree<DiffLinesLabel>, Error> runGnuPatch(
			final VariationTree<L> targetVariantBefore) throws IOException, DiffParseException {
		// TODO: run mpatch and gnu patch. Here is a sketch for this can be done.
		// reset target variant
		resetTargetVariantBefore(targetVariantBefore);
		Path pathToTargetVariantCode = Path.of("..", targetVariant, code);
		Path pathToSourceVariantPatch = Path.of("..", targetVariant, patch);
		GnuPatchCommand command = new GnuPatchCommand("patch", pathToTargetVariantCode.toString(),
				pathToSourceVariantPatch.toString());
		try {
			// TODO: configure GNU patch
			shellSourceVariantDir.execute(command);
			// reset target variant
		} catch (ShellException e) {
			Logger.error(e);
			return Result.Failure(Error.ERROR);
		}
		if (command.isPatchingSuccessful()) {
			VariationTree<DiffLinesLabel> gnuPatchResult = VariationTree
					.fromFile(Path.of(directory, targetVariant, code));
			return Result.Success(gnuPatchResult);
		}
		return Result.Failure(Error.FAILED);
	}

	public static <L extends Label> Result<VariationTree<DiffLinesLabel>, Error> runPatchTransformer(
			VariationDiff<L> sourcePatch, final VariationTree<L> targetVariantBefore) {
		VariationTree<DiffLinesLabel> patchTransformerResult = null;
		try {
//			GameEngine.showAndAwaitAll(Show.tree(targetVariantBefore));
			VariationDiff<DiffLinesLabel> diff = Patching.patch((VariationDiff<DiffLinesLabel>) sourcePatch,
					(VariationTree<DiffLinesLabel>) targetVariantBefore, false, true);
			patchTransformerResult = diff.project(Time.AFTER);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Result.Failure(Error.FAILED);
		}
		return Result.Success(patchTransformerResult);
	}
}
