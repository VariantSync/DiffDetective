package org.variantsync.diffdetective.experiments.thesis_pm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import org.variantsync.diffdetective.variation.diff.patching.Patching;
import org.variantsync.diffdetective.variation.diff.view.DiffView;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.diffdetective.variation.tree.view.TreeView;
import org.variantsync.diffdetective.variation.tree.view.relevance.Configure;

public class Generator {
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
			mutant.put(e.getKey(), Math.random() <= probability ? !e.getValue() : e.getValue());
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
			subsets.put(t, Math.random() <= probability);
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
				if (i != text.size() - 1) {
					myWriter.newLine();	
				}
			}
			myWriter.close();
		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	public static <L extends Label> void generatePatchScenario(VariationDiff<L> spl) throws Exception {
		// ## 1. Sample two variants.
		// Since we have no feature model, we create a naive problem space model:
		// We just collect all features without constraints.
		final Set<String> featureModel = spl.computeAllFeatureNames();
		Logger.info("Extracted feature names: {}", featureModel);

		// To sample variants, we just pick a random subset of features to set to true,
		// set the rest to false
		// We could use more sophisticated algorithms here, for example based on how
		// often features occur.
		// Maybe should ask Sebastian.
		// The configurations we produce are complete.
		// Hypothesis: the lower the probability value (i.e., the more deselected
		// features), the harder the patching challenge.
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
		final VariationDiff<L> sourcePatch = DiffView.optimized(spl, configureTo1); // input patch to apply to the
																					// target variant
		VariationDiff<L> targetPatch = DiffView.optimized(spl, configureTo2); // ground truth for target patch;
																					// this is the "perfect" target
																					// patch
		
		VariationDiff<L> targetView = DiffView.optimized(targetPatch.deepCopy(), configureTo1);
		VariationDiff<L> targetPatchModified = targetPatch.deepCopy();
		GameEngine.showAndAwaitAll(Show.diff(sourcePatch, "source patch"), Show.diff(targetPatch, "target patch"), Show.diff(targetView, "target view"));
		targetPatch.forAll(node -> {
			if (targetView.getNodeWithID(node.getID()) == null && !node.isNon()) {
				node = targetPatchModified.getNodeWithID(node.getID());
				if (node != null) {
					DiffNode<L> newNode = DiffNode.unchanged(node);
					if (node.isRem()) {
						if (node.getParent(Time.BEFORE) != null && node.getParent(Time.BEFORE).isNon()) {
							int index = node.getParent(Time.BEFORE).indexOfChild(node, Time.BEFORE);
							node.getParent(Time.BEFORE).insertChild(newNode, index, Time.BEFORE); 
							node.getParent(Time.BEFORE).insertChild(newNode, index, Time.AFTER); 
						}
					}
					if (node.isAdd()) {
						
						if (node.getParent(Time.AFTER) != null && node.getParent(Time.AFTER).isNon()) {
							int index = node.getParent(Time.AFTER).indexOfChild(node, Time.AFTER);
							node.getParent(Time.AFTER).insertChild(newNode, index, Time.BEFORE); 
							node.getParent(Time.AFTER).insertChild(newNode, index, Time.AFTER); 
						}
					}
					node.drop();
				}
			}
		});
		targetPatch = targetPatchModified;
		GameEngine.showAndAwaitAll(Show.diff(sourcePatch), Show.diff(targetPatch), Show.diff(targetView), Show.diff(targetPatchModified));
		
		// FIXME: Maybe we want to distinguish cases where one of the patches (or both)
		// are empty (i.e., noop / id)?

		final VariationTree<L> sourceVariantBefore = sourcePatch.project(Time.BEFORE); // input
		final VariationTree<L> sourceVariantAfter = sourcePatch.project(Time.AFTER); // input
		final VariationTree<L> targetVariantBefore = targetPatch.project(Time.BEFORE); // input
		final VariationTree<L> targetVariantAfter = targetPatch.project(Time.AFTER); // ground truth for how the patched
																						// target variant should ideally
																						// look like

		// GameEngine.showAndAwaitAll(Show.diff(spl));
		GameEngine.showAndAwaitAll(Show.diff(sourcePatch, "Source Patch " + config1),
				Show.diff(targetPatch, "Target Patch " + config2));
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
		logDiff("Source Before:", sourceVariantCodeBefore);
		logDiff("Source After:", sourceVariantCodeAfter);
		logDiff("Target Before:", targetVariantCodeBefore);
		logDiff("Target After:", targetVariantCodeAfter);

		// TODO: write the code to disk at a reasonable location
		String diffDetective = "DiffDetective";
		String directory = "experiment";
		String sourceVariant = "sourceVariant";
		String version1 = "version1";
		String version2 = "version2";
		String targetVariant = "targetVariant";
		String code = "code.txt";
		String patch = "patch.txt";

        File f = new File(Path.of(directory).toUri());
        deleteDirectory(f);
        f.delete();

        if (!(new File(Path.of(directory, targetVariant).toUri())).mkdirs() ||
                !(new File(Path.of(directory, sourceVariant).toUri())).mkdir()) {
        	throw new Exception("Failed to create directories");
        }
        if (!(new File(Path.of(directory, sourceVariant, version1).toUri())).mkdirs() || 
        		!(new File(Path.of(directory, sourceVariant, version2).toUri())).mkdir()) {
        	throw new Exception("Failed to create directories");
        }

		Path sourceVariantBeforePath = Path.of(directory, sourceVariant, version1, code);
		Path sourceVariantAfterPath = Path.of(directory, sourceVariant, version2, code);
		Path targetVariantBeforePath = Path.of(directory, targetVariant, code);
		Path patchPath = Path.of(directory, sourceVariant, patch);
        writeToFile(sourceVariantCodeBefore, sourceVariantBeforePath);
        writeToFile(sourceVariantCodeAfter, sourceVariantAfterPath);
        writeToFile(targetVariantCodeBefore, targetVariantBeforePath);
//        writeToFile(targetVariantCodeAfter, targetVariantAfterPath);

		final ShellExecutor shell = new ShellExecutor(Logger::info, Logger::error, Path.of(directory)); // maybe we have
																										// to specify
																										// the working
																										// directory as
																										// third
																										// argument here
		try {
			Path pathToVersion1Dir = Path.of(sourceVariant, version1);
			Path pathToVersion2Dir = Path.of(sourceVariant, version2);
			writeToFile(shell.execute(
					new DiffCommand("diff", "-Naur", pathToVersion1Dir.toString(), pathToVersion2Dir.toString())), patchPath);
			
//			System.out.println(list);
		} catch (ShellException e) {
//        	System.out.println(e);
		}

		// ## 4. Run the patchers!

		// TODO: Run Pia's new patcher here and store the result.
		VariationTree<DiffLinesLabel> patchTransformerResult = null;
		try {
			VariationDiff<DiffLinesLabel> diff = Patching.patch((VariationDiff<DiffLinesLabel>) sourcePatch, (VariationTree<DiffLinesLabel>) targetVariantBefore, false, true);
			patchTransformerResult = diff.project(Time.AFTER);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		VariationTree<DiffLinesLabel> gnuPatchResult = null;
		VariationTree<DiffLinesLabel> mpatchResult = null;
		// TODO: run mpatch and gnu patch. Here is a sketch for this can be done.
		try {
			// TODO: configure GNU patch
			Path pathToTargetVariantCode = Path.of(targetVariant, code);
			Path pathToSourceVariantPatch = Path.of(sourceVariant, patch);
			shell.execute(new GnuPatchCommand("patch", pathToTargetVariantCode.toString(),
					pathToSourceVariantPatch.toString()));
			gnuPatchResult = VariationTree.fromFile(Path.of(directory, targetVariant, code));
			
			// reset target variant
			writeToFile(targetVariantCodeBefore, targetVariantBeforePath);
			
			// TODO: configure mpatch
			Path mpatchPath = Path.of("..", "..", "..", "mpatch", "target", "debug", "mpatch");
			pathToSourceVariantPatch = Path.of("..", sourceVariant, patch);
			// FIXME: how to change directory?
			final ShellExecutor shell2 = new ShellExecutor(Logger::info, Logger::error,
					Path.of(directory, targetVariant));
			MPatchCommand command = new MPatchCommand(mpatchPath.toString(), "--strip", "2", "--sourcedir",
					Path.of("..", sourceVariant).toString(), "--patchfile", pathToSourceVariantPatch.toString());
			shell2.execute(command);
			mpatchResult = VariationTree.fromFile(Path.of(directory, targetVariant, code));
			
		} catch (ShellException e) {
			// FIXME: When a shell exception occurs, we know that patching failed.
			// Either there is a bug or the patcher was not successful!
			// We should not throw and catch exceptions in these cases.
			// It is probably best to write new ShellCommand subclasses for mpatch and GNU
			// patch with a proper interpretResult method.
			// We have to distinguish patch success from patch failure anyway somewhere.
			Logger.error(e);
		}
		
		// TODO: Read the results of the patchers. The patchers should produce the
		// target variants as string if they did not fail.
//		GameEngine.showAndAwaitAll(Show.tree(targetVariantAfter, "ground truth"), Show.tree(patchTransformerResult, "patchTransformer"), Show.tree(mpatchResult, "mpatch"), Show.tree(gnuPatchResult, "gnu patch"));

		// ## 5. Compare the results of patchers here!
		
		Map<String, Boolean> configTargetVariantSpecificFeatures = new HashMap<>();
		Map<String, Boolean> configCrossVariantFeatures = new HashMap<>();
		for (String feature : config1.keySet()) {
			if (config2.containsKey(feature) && config2.get(feature) && !config1.get(feature)) {
				// feature is only true in target variant
				configTargetVariantSpecificFeatures.put(feature, true);
			} else {
				configTargetVariantSpecificFeatures.put(feature, false);
			}
			if (config2.containsKey(feature) && config2.get(feature) && config1.get(feature)) {
				configCrossVariantFeatures.put(feature, true);
			} else {
				configCrossVariantFeatures.put(feature, false);
			}
		}
		
		Configure configureToTargetVarSpecificFeatures = new Configure(configTargetVariantSpecificFeatures);
		Configure configureToCrossVariantSpecificFeatures = new Configure(configCrossVariantFeatures);		
		
		boolean isMpatchCorrect = mpatchResult == null ? false : Patching.arePatchedVariantsEquivalent((VariationTree<DiffLinesLabel>) sourceVariantAfter, (VariationTree<DiffLinesLabel>) targetVariantBefore, mpatchResult, configureToCrossVariantSpecificFeatures, configureToTargetVarSpecificFeatures);
		boolean isGnuPatchCorrect = gnuPatchResult == null ? false : Patching.arePatchedVariantsEquivalent((VariationTree<DiffLinesLabel>) sourceVariantAfter, (VariationTree<DiffLinesLabel>) targetVariantBefore, gnuPatchResult, configureToCrossVariantSpecificFeatures, configureToTargetVarSpecificFeatures);
		boolean isPatchTransformerCorrect = patchTransformerResult == null ? false : Patching.arePatchedVariantsEquivalent((VariationTree<DiffLinesLabel>) sourceVariantAfter, (VariationTree<DiffLinesLabel>) targetVariantBefore, patchTransformerResult, configureToCrossVariantSpecificFeatures, configureToTargetVarSpecificFeatures);
		
		System.out.println("mpatch: " + isMpatchCorrect);
		System.out.println("GNU patch: " + isGnuPatchCorrect);
		System.out.println("patch transformer " + isPatchTransformerCorrect);
		// TODO
	}
}
