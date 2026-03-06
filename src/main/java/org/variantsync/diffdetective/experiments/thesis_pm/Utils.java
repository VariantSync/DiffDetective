package org.variantsync.diffdetective.experiments.thesis_pm;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.diffdetective.variation.tree.view.TreeView;
import org.variantsync.diffdetective.variation.tree.view.relevance.ConfigureWithFullConfig;
import org.variantsync.diffdetective.variation.tree.view.relevance.Unchanged;
import org.variantsync.functjonal.Pair;

public class Utils {

	public static boolean comparePatchedVariantWithExpectedResult(VariationTree<DiffLinesLabel> patchedVariant,
			VariationTree<DiffLinesLabel> expectedResult) {
		return Utils.isSameAs(patchedVariant.toCompletelyUnchangedVariationDiff(),
				expectedResult.toCompletelyUnchangedVariationDiff());
	}

	public static Pair<Boolean, Boolean> arePatchedVariantsEquivalent(
				VariationTree<DiffLinesLabel> patchedTargetVariant,
				VariationTree<DiffLinesLabel> sourceVariantAfterRedToCrossVarFeatures,
				VariationTree<DiffLinesLabel> targetVariantBeforeRedToUnchanged, ConfigureWithFullConfig configSourceVariant,
				Unchanged unchangedAfter) {
	
			VariationTree<DiffLinesLabel> targetVariantAfterRedToCrossVarFeatures = TreeView.tree(patchedTargetVariant,
					configSourceVariant);
	
			VariationTree<DiffLinesLabel> patchedTargetVariantRedToUnchanged = TreeView.tree(patchedTargetVariant,
					unchangedAfter);
	
	//		GameEngine.showAndAwaitAll(Show.tree(patchedTargetVariant, "patched target variant"),
	//				Show.tree(patchedTargetVariantRedToUnchanged, "patched target variant red. to unchanged"),
	//				Show.tree(sourceVariantAfterRedToCrossVarFeatures,
	//						"patched source variant red. to cross variant features"),
	//				Show.tree(targetVariantAfterRedToCrossVarFeatures,
	//						"patched target variant red. to cross variant features"),
	//				Show.tree(targetVariantBeforeRedToUnchanged, "target variant before red. to unchanged"));
	
			return new Pair<Boolean, Boolean>(
					sourceVariantAfterRedToCrossVarFeatures.unparse().equals(targetVariantAfterRedToCrossVarFeatures.unparse()),
					patchedTargetVariantRedToUnchanged.unparse().equals(targetVariantBeforeRedToUnchanged.unparse()));
	
		}

	public static VariationDiff<DiffLinesLabel> parseVariationDiffFromFiles(String file1, String file2)
			throws IOException, DiffParseException {
		Path examplesDir = Path.of("data", "examples");
		return VariationDiff.fromFiles(examplesDir.resolve(file1), examplesDir.resolve(file2),
				DiffAlgorithm.SupportedAlgorithm.MYERS, VariationDiffParseOptions.Default);
	}

	public static VariationTree<DiffLinesLabel> parseVariationTreeFromFile(String file) {
		Path examplesDir = Path.of("data", "examples");
		Path path = examplesDir.resolve(file);
		try {
			VariationTree<DiffLinesLabel> tree = VariationTree.fromFile(path, VariationDiffParseOptions.Default);
			return tree;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DiffParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static VariationDiff<DiffLinesLabel> parseVariationDiffFromFile(String file)
			throws IOException, DiffParseException {
		Path examplesDir = Path.of("data", "examples");
		return VariationDiff.fromFile(examplesDir.resolve(file), VariationDiffParseOptions.Default);
	}

	public static <L extends Label> boolean isSameAs(VariationDiff<L> diff1, VariationDiff<L> diff2) {
		return Utils.isSameAs(diff1.getRoot(), diff2.getRoot());
	}

	public static <L extends Label> boolean isSameAs(DiffNode<L> a, DiffNode<L> b) {
		return Utils.isSameAs(a, b, new HashSet<>());
	}

	public static <L extends Label> boolean isSameAs(DiffNode<L> a, DiffNode<L> b, Set<DiffNode<L>> visited) {
		if (!visited.add(a)) {
			return true;
		}
	
		if (!(a.getNodeType().equals(b.getNodeType()) && hasSameLabel(a.getLabel(), b.getLabel())
				&& (a.getFormula() == null ? b.getFormula() == null : a.getFormula().equals(b.getFormula())))) {
			return false;
		}
	
		Iterator<DiffNode<L>> aIt = a.getAllChildren().iterator();
		Iterator<DiffNode<L>> bIt = b.getAllChildren().iterator();
		while (aIt.hasNext() && bIt.hasNext()) {
			if (!isSameAs(aIt.next(), bIt.next(), visited)) {
				return false;
			}
		}
	
		return aIt.hasNext() == bIt.hasNext();
	}

	public static <L extends Label> boolean hasSameLabel(L a, L b) {
		String labelA = a.toString().replaceAll(" ", "");
		String labelB = b.toString().replaceAll(" ", "");
		return labelA.equals(labelB);
	}

}
