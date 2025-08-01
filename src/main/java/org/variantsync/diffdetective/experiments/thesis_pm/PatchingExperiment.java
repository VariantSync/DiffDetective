package org.variantsync.diffdetective.experiments.thesis_pm;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.show.Show;
import org.variantsync.diffdetective.show.engine.GameEngine;
import org.variantsync.diffdetective.util.fide.FixTrueFalse.Formula;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.DiffType;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;
import org.variantsync.diffdetective.variation.diff.patching.Patching;
import org.variantsync.diffdetective.variation.diff.source.VariationDiffSource;
import org.variantsync.diffdetective.variation.diff.view.DiffView;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.diffdetective.variation.tree.view.relevance.Configure;
import org.variantsync.diffdetective.variation.tree.view.relevance.Relevance;

public class PatchingExperiment {

	

	public static void main(String[] args) {
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
					Patching.parseVariationTreeFromFile("exampleA1NodesWith2Parents.cpp"));
			VariationTree<DiffLinesLabel> expectedResult = Patching.parseVariationTreeFromFile("exampleA2NodesWith2Parents.cpp");
			GameEngine.showAndAwaitAll(Show.tree(patchedVariant), Show.tree(expectedResult));
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
