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
import org.variantsync.diffdetective.variation.tree.view.relevance.ConfigureWithFullConfig;
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

	public static <L extends Label> PatchScenario<L> generatePatchScenario(VariationDiff<L> spl, String commitHash)
			throws Exception {
		// ## 1. Sample two variants.
		// Since we have no feature model, we create a naive problem space model:
		// We just collect all features without constraints.
		final Set<Object> featureModel = spl.computeAllFeatureNames();
		Logger.info("Extracted feature names: {}", featureModel);

		// To sample variants, we just pick a random subset of features to set to true,
		// set the rest to false
		// We could use more sophisticated algorithms here, for example based on how
		// often features occur.
		// The configurations we produce are complete.

		final Map<Object, Boolean> config1 = randomPartition(featureModel, 0.6);
		final Map<Object, Boolean> config2 = mutateByWeightedCoinFlip(config1, 0.5);

		Logger.info("Configuration 1: {}", config1);
		Logger.info("Configuration 2: {}", config2);

		logDiff("spl before",spl.project(Time.BEFORE).unparse());
		logDiff("spl after",spl.project(Time.AFTER).unparse());
		
		final ConfigureWithFullConfig configureTo1 = new ConfigureWithFullConfig(config1);
		final ConfigureWithFullConfig configureTo2 = new ConfigureWithFullConfig(config2);

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
					sourceAfter.unparse(), sourceBefore, sourceAfter, DiffAlgorithm.SupportedAlgorithm.MYERS, VariationDiffParseOptions.Default);
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
					before, after, DiffAlgorithm.SupportedAlgorithm.MYERS, VariationDiffParseOptions.Default);
		} catch (DiffParseException e) {
			return null;
		}

		VariationDiff<L> targetPatchModified = targetPatchElimEmptyAlt.deepCopy();

		// split nodes and subtrees with two parents
		Patching.resolve((DiffNode<DiffLinesLabel>) targetPatchElimEmptyAlt.getRoot(),
				(VariationDiff<DiffLinesLabel>) targetPatchModified);

		VariationDiff<L> targetView = DiffView.optimized(targetPatchModified.deepCopy(), configureTo1);

		// revert the changes made in B but not in A
		new RevertSomeChanges<DiffLinesLabel>(node -> {
			if (targetView.getNodeWithID(node.getID()) == null && !node.isNon()) {
				return true;
			}
			return false;
		}).transform((VariationDiff<DiffLinesLabel>) targetPatchModified);

		targetPatch = targetPatchModified;

		final VariationTree<L> sourceVariantBefore = sourcePatch.project(Time.BEFORE); // input
		final VariationTree<L> sourceVariantAfter = sourcePatch.project(Time.AFTER); // input
		final VariationTree<L> targetVariantBefore = targetPatch.project(Time.BEFORE); // input
		final VariationTree<L> targetVariantAfter = targetPatch.project(Time.AFTER); 

		// ## 3. To use command-line patchers such as GNU patch and mpatch, we need to
		// write our variants to disk.
		deleteFilesAndCreateNewDirectories(commitHash);
		Path patchPath = writeVariantsToFileSystem(sourceVariantBefore, sourceVariantAfter, targetVariantBefore, code,
				patch, commitHash);

		if (!runGnuDiff(patchPath, commitHash)) {
			return null;
		}

		return new PatchScenario<L>(sourcePatch, targetVariantBefore, targetPatch, targetVariantAfter, configureTo1,
				configureTo2);
	}

	public static <L extends Label> boolean generateViewVariants(VariationDiff<L> sourcePatch,
			VariationTree<L> targetVariantBefore, ConfigureWithFullConfig configureTo2, String commitHash) {
		try {
			deleteFilesAndCreateNewDirectories(commitHash);
		} catch (Exception e) {
			e.printStackTrace();
		}
		final VariationDiff<L> sourceVariantCrossVariantView = DiffView.optimized(sourcePatch, configureTo2);
		final VariationTree<L> sourceVariantCrossVariantViewBefore = sourceVariantCrossVariantView.project(Time.BEFORE);
		final VariationTree<L> sourceVariantCrossVariantViewAfter = sourceVariantCrossVariantView.project(Time.AFTER);

		Path patchPath2;
		try {
			patchPath2 = writeVariantsToFileSystem(sourceVariantCrossVariantViewBefore,
					sourceVariantCrossVariantViewAfter, targetVariantBefore, code, patch, commitHash);
			return runGnuDiff(patchPath2, commitHash);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private static <L extends Label> Path writeVariantsToFileSystem(final VariationTree<L> sourceVariantBefore,
			final VariationTree<L> sourceVariantAfter, final VariationTree<L> targetVariantBefore, final String code,
			final String patch, String commitHash) throws Exception {
		final String sourceVariantCodeBefore = sourceVariantBefore.unparse();
		final String sourceVariantCodeAfter = sourceVariantAfter.unparse();
		final String targetVariantCodeBefore = targetVariantBefore.unparse(); 
		String newDir = directory + commitHash;
		Path sourceVariantBeforePath = Path.of(newDir, sourceVariant, version1, code);
		Path sourceVariantAfterPath = Path.of(newDir, sourceVariant, version2, code);
		Path targetVariantBeforePath = Path.of(newDir, targetVariant, code);
		Path patchPath = Path.of(newDir, targetVariant, patch);
		writeToFile(sourceVariantCodeBefore, sourceVariantBeforePath);
		writeToFile(sourceVariantCodeAfter, sourceVariantAfterPath);
		writeToFile(targetVariantCodeBefore, targetVariantBeforePath);
		return patchPath;
	}

	private static void deleteFilesAndCreateNewDirectories(String commitHash) throws Exception {
		String newDir = directory + commitHash;
		File f = new File(Path.of(newDir).toUri());
		deleteDirectory(f);
		f.delete();

		if (!(new File(Path.of(newDir, targetVariant).toUri())).mkdirs()
				|| !(new File(Path.of(newDir, sourceVariant).toUri())).mkdir()) {
			throw new Exception("Failed to create directories");
		}
		if (!(new File(Path.of(newDir, sourceVariant, version1).toUri())).mkdirs()
				|| !(new File(Path.of(newDir, sourceVariant, version2).toUri())).mkdir()) {
			throw new Exception("Failed to create directories");
		}
	}

	private static boolean runGnuDiff(Path patchPath, String commitHash) {
		try {
			Path pathToVersion1Dir = Path.of(version1);
			Path pathToVersion2Dir = Path.of(version2);
			ShellExecutor shell = new ShellExecutor(Logger::info, Logger::error,
					Path.of(directory + commitHash, sourceVariant));
			DiffCommand command = new DiffCommand("diff", "-Naur", pathToVersion1Dir.toString(),
					pathToVersion2Dir.toString());
			List<String> output = shell.execute(command);
			if (command.areFilesDifferent()) {
				writeToFile(output, patchPath);
			}
			return command.areFilesDifferent();
//			System.out.println(list);
		} catch (ShellException e) {
			System.out.println(e);
		}
		return false;
	}

	public static <L extends Label> void runPatchers(PatchScenario<L> scenario, String commitHash) {
		// ## 4. Run the patchers!
		boolean isMpatchCorrect = false;
		boolean isMpatchCorrect2 = false;
		boolean isGnuPatchCorrect = false;
		boolean isGnuPatchCorrect2 = false;
		boolean isPatchTransformerCorrect = false;
		List<GameEngine> gameEngine = new ArrayList<>();
		gameEngine.add(Show.diff(scenario.sourcePatch, "source patch"));
		gameEngine.add(Show.tree(scenario.targetVariantBefore, "target variant before"));
		gameEngine.add(Show.diff(scenario.patchGroundTruth, "targetPatch"));
		gameEngine.add(Show.tree(scenario.patchedVariantGroundTruth, "ground truth"));

		// Runs Pia's new patcher here and stores the result.
		Result<VariationTree<DiffLinesLabel>, Error> patchTransformerResult = runPatchTransformer(scenario.sourcePatch,
				scenario.targetVariantBefore, scenario.sourceVariantConfig, scenario.targetVariantConfig);
		if (patchTransformerResult.isSuccess()) {
			gameEngine.add(Show.tree(patchTransformerResult.getSuccess(), "patch transformer result"));
		}
		isPatchTransformerCorrect = patchTransformerResult.match(tree -> {
			Pair<Boolean, Boolean> equiv = Patching.arePatchedVariantsEquivalent(tree,
					scenario.sourceVariantAfterRedToCrossVarFeatures, scenario.targetVariantBeforeRedToUnchanged,
					scenario.sourceVariantConfig, scenario.unchangedAfter);
			return equiv.first() && equiv.second();
		}, error -> false);

		Result<VariationTree<DiffLinesLabel>, Error> gnuPatchResult;
		try {
			gnuPatchResult = runGnuPatch(scenario.targetVariantBefore, patch, code, commitHash);
			if (gnuPatchResult.isSuccess()) {
				gameEngine.add(Show.tree(gnuPatchResult.getSuccess(), "gnu patch result"));
			}
			isGnuPatchCorrect = gnuPatchResult.match(tree -> {
				Pair<Boolean, Boolean> equiv = Patching.arePatchedVariantsEquivalent(tree,
						scenario.sourceVariantAfterRedToCrossVarFeatures, scenario.targetVariantBeforeRedToUnchanged,
						scenario.sourceVariantConfig, scenario.unchangedAfter);
				return equiv.first() && equiv.second();
			}, error -> false);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Result<VariationTree<DiffLinesLabel>, Error> mpatchResult;
		try {
			mpatchResult = runMPatch(scenario.targetVariantBefore, patch, code, commitHash);
			if (mpatchResult.isSuccess()) {
				gameEngine.add(Show.tree(mpatchResult.getSuccess(), "mpatch result"));
			}
			isMpatchCorrect = mpatchResult.match(tree -> {
				Pair<Boolean, Boolean> equiv = Patching.arePatchedVariantsEquivalent(tree,
						scenario.sourceVariantAfterRedToCrossVarFeatures, scenario.targetVariantBeforeRedToUnchanged,
						scenario.sourceVariantConfig, scenario.unchangedAfter);
				return equiv.first() && equiv.second();
			}, error -> false);
		} catch (IOException e) {
			e.printStackTrace();
		}

		generateViewVariants(scenario.sourcePatch, scenario.targetVariantBefore, scenario.targetVariantConfig, "");

		Result<VariationTree<DiffLinesLabel>, Error> mpatchResult2;
		try {
			mpatchResult2 = runMPatch(scenario.targetVariantBefore, patch, code, commitHash);
			if (mpatchResult2.isSuccess()) {
				gameEngine.add(Show.tree(mpatchResult2.getSuccess(), "mpatch result"));
			}
			isMpatchCorrect2 = mpatchResult2.match(tree -> {
				Pair<Boolean, Boolean> equiv = Patching.arePatchedVariantsEquivalent(tree,
						scenario.sourceVariantAfterRedToCrossVarFeatures, scenario.targetVariantBeforeRedToUnchanged,
						scenario.sourceVariantConfig, scenario.unchangedAfter);
				return equiv.first() && equiv.second();
			}, error -> false);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Result<VariationTree<DiffLinesLabel>, Error> gnuPatchResult2;
		try {
			gnuPatchResult2 = runGnuPatch(scenario.targetVariantBefore, patch, code, commitHash);
			if (gnuPatchResult2.isSuccess()) {
				gameEngine.add(Show.tree(gnuPatchResult2.getSuccess(), "gnu patch result"));
			}
			isGnuPatchCorrect2 = gnuPatchResult2.match(tree -> {
				Pair<Boolean, Boolean> equiv = Patching.arePatchedVariantsEquivalent(tree,
						scenario.sourceVariantAfterRedToCrossVarFeatures, scenario.targetVariantBeforeRedToUnchanged,
						scenario.sourceVariantConfig, scenario.unchangedAfter);
				return equiv.first() && equiv.second();
			}, error -> false);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Read the results of the patchers. The patchers should produce the
		// target variants as string if they did not fail.
		GameEngine[] gameEngineArray = new GameEngine[gameEngine.size()];
		gameEngineArray = gameEngine.toArray(gameEngineArray);
		GameEngine.showAndAwaitAll(gameEngineArray);

		// ## 5. Compare the results of patchers here!
		System.out.println("mpatch: " + isMpatchCorrect);
		System.out.println("mpatch2: " + isMpatchCorrect2);
		System.out.println("GNU patch: " + isGnuPatchCorrect);
		System.out.println("GNU patch2: " + isGnuPatchCorrect2);
		System.out.println("patch transformer " + isPatchTransformerCorrect);
		// TODO
	}

	public static <L extends Label> Result<VariationTree<DiffLinesLabel>, Error> runMPatch(
			final VariationTree<L> targetVariantBefore, String patch, String code, String commitHash)
			throws IOException {
		// configure mpatch
		// reset target variant
		resetTargetVariantBefore(targetVariantBefore, code, commitHash);
		Path mpatchPath = Path.of("..", "..", "..", "mpatch", "target", "release", "mpatch");
		ShellExecutor shell = new ShellExecutor(Logger::info, Logger::error,
				Path.of(directory + commitHash, targetVariant));
		MPatchCommand command = new MPatchCommand(mpatchPath.toString(), "--strip", "1", "--sourcedir",
				Path.of("..", sourceVariant, version1).toString(), "--patchfile", patch);
		try {
			shell.execute(command);
		} catch (ShellException e) {
			Logger.error(e);
			return Result.Failure(Error.ERROR);
		}
		if (command.isPatchingSuccessful()) {
			try {
				VariationTree<DiffLinesLabel> mpatchResult = VariationTree
						.fromFile(Path.of(directory + commitHash, targetVariant, code));
				return Result.Success(mpatchResult);
			} catch (DiffParseException e) {
				return Result.Success(null);
			}
		}
		return Result.Failure(Error.FAILED);
	}

	private static <L extends Label> void resetTargetVariantBefore(final VariationTree<L> targetVariantBefore,
			String code, String commitHash) {
		Path targetVariantBeforePath = Path.of(directory + commitHash, targetVariant, code);
		writeToFile(targetVariantBefore.unparse(), targetVariantBeforePath);
	}

	public static <L extends Label> Result<VariationTree<DiffLinesLabel>, Error> runGnuPatch(
			final VariationTree<L> targetVariantBefore, String patch, String code, String commitHash)
			throws IOException {
		// run mpatch and gnu patch. Here is a sketch for this can be done.
		// reset target variant
		resetTargetVariantBefore(targetVariantBefore, code, commitHash);
		Path pathToTargetVariantCode = Path.of("..", targetVariant, code);
		Path pathToSourceVariantPatch = Path.of("..", targetVariant, patch);
		ShellExecutor shell = new ShellExecutor(Logger::info, Logger::error,
				Path.of(directory + commitHash, sourceVariant));
		GnuPatchCommand command = new GnuPatchCommand("patch", pathToTargetVariantCode.toString(),
				pathToSourceVariantPatch.toString());
		try {
			shell.execute(command);
			// reset target variant
		} catch (ShellException e) {
			Logger.error(e);
			return Result.Failure(Error.ERROR);
		}
		if (command.isPatchingSuccessful()) {
			try {
				VariationTree<DiffLinesLabel> gnuPatchResult = VariationTree
						.fromFile(Path.of(directory + commitHash, targetVariant, code));
				return Result.Success(gnuPatchResult);
			} catch (DiffParseException e) {
				return Result.Success(null);
			}

		}
		return Result.Failure(Error.FAILED);
	}

	public static <L extends Label> Result<VariationTree<DiffLinesLabel>, Error> runPatchTransformer(
			VariationDiff<L> sourcePatch, final VariationTree<L> targetVariantBefore,
			ConfigureWithFullConfig sourceVariantConfig, ConfigureWithFullConfig targetVariantConfig) {
		VariationTree<DiffLinesLabel> patchTransformerResult = null;
		try {
			VariationDiff<DiffLinesLabel> diff = Patching.patch((VariationDiff<DiffLinesLabel>) sourcePatch,
					(VariationTree<DiffLinesLabel>) targetVariantBefore, sourceVariantConfig, targetVariantConfig,
					false, true);
			patchTransformerResult = diff.project(Time.AFTER);
		} catch (Exception e) {
			e.printStackTrace();
			return Result.Failure(Error.FAILED);
		}
		return Result.Success(patchTransformerResult);
	}
}
