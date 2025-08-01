package org.variantsync.diffdetective.experiments.thesis_pm;

import org.variantsync.diffdetective.show.Show;
import org.variantsync.diffdetective.show.engine.GameEngine;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.patching.Patching;
import org.variantsync.diffdetective.variation.tree.VariationTree;

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
