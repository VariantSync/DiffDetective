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
	
	private static Set<String> calculateSetMinusOfFeatureSets(Set<String> featureSet1, Set<String> featureSet2, boolean debug) {
		Set<String> intersectSet1 = new HashSet<>(featureSet1);
		intersectSet1.removeAll(featureSet2);
		Set<String> intersectSet2 = new HashSet<>(featureSet2);
		intersectSet2.removeAll(featureSet1);
		intersectSet1.addAll(intersectSet2);
		if (debug) {
			System.out.println(featureSet1);
			System.out.println(featureSet2);
			System.out.println(intersectSet1);
		}
		return intersectSet1;
	}
	
	private static Relevance calculateFormulaForDeselection(Set<String> set, boolean debug) {
		
		Formula[] f = new Formula[set.size()];
		Iterator<String> iterator = set.iterator();
		for (int i = 0; i < f.length; i++) {
			f[i] = Formula.not(Formula.var(iterator.next()));
		}
		Formula formula = Formula.and(f);

		if (debug)
			System.out.println(formula.get().toString());

		return new Configure(formula);
	}
	
	private static Set<String> calculateFeatureSetToDeselectFromTrees(VariationTree<DiffLinesLabel> variant1version1,
			VariationTree<DiffLinesLabel> variant1version2, VariationTree<DiffLinesLabel> variant2, boolean debug) {
		Set<String> featuresTreeV1 = new HashSet<String>();
		variant1version1.forAllPreorder(node -> {
			featuresTreeV1.addAll(node.getFeatureMapping().getUniqueContainedFeatures());
		});
		if (variant1version2 != null) {
			variant1version2.forAllPreorder(node -> {
				featuresTreeV1.addAll(node.getFeatureMapping().getUniqueContainedFeatures());
			});
		}
		Set<String> featuresTreeV2 = new HashSet<String>();
		variant2.forAllPreorder(node -> {
			featuresTreeV2.addAll(node.getFeatureMapping().getUniqueContainedFeatures());
		});
		
		return calculateSetMinusOfFeatureSets(featuresTreeV1, featuresTreeV2, debug);
	}
	
	private static Set<String> calculateFeatureSetToDeselectFromDiff(VariationDiff<DiffLinesLabel> diff, VariationTree<DiffLinesLabel> variant2, boolean debug) {
		Set<String> featuresV1 = new HashSet<String>();
		diff.forAll(node -> {
			if (node.getDiffType().existsAtTime(Time.BEFORE)) {
				featuresV1.addAll(node.getFeatureMapping(Time.BEFORE).getUniqueContainedFeatures());
			}
			if (node.getDiffType().existsAtTime(Time.AFTER)) {
				featuresV1.addAll(node.getFeatureMapping(Time.AFTER).getUniqueContainedFeatures());
			}
		});
		
		Set<String> featuresV2 = new HashSet<String>();
		variant2.forAllPreorder(node -> {
			featuresV2.addAll(node.getFeatureMapping().getUniqueContainedFeatures());
		});

		return calculateSetMinusOfFeatureSets(featuresV1, featuresV2, debug);
	}

	private static Set<DiffNode<DiffLinesLabel>> findRootsOfSubtrees(Set<DiffNode<DiffLinesLabel>> nodes, DiffType type,
			boolean debug) {
		Time time = (type == DiffType.ADD) ? Time.AFTER : Time.BEFORE;
		Set<DiffNode<DiffLinesLabel>> subtreeRoots = new HashSet<DiffNode<DiffLinesLabel>>();
		for (DiffNode<DiffLinesLabel> node : nodes) {
			if (!nodes.contains(node.getParent(time))) {
				subtreeRoots.add(node);
			}
		}
		if (debug)
			System.out.println(subtreeRoots);

		return subtreeRoots;
	}

	private static DiffNode<DiffLinesLabel> getChildFromListIfIndexInRange(List<DiffNode<DiffLinesLabel>> childrenList,
			int index) {
		if (index >= 0 && index < childrenList.size()) {
			return childrenList.get(index);
		}
		return null;
	}

	private static boolean checkNeighbors(DiffNode<DiffLinesLabel> root, DiffNode<DiffLinesLabel> targetNodeInPatch,
			DiffNode<DiffLinesLabel> node, Time time, boolean debug) {

		List<DiffNode<DiffLinesLabel>> orderedChildrenTarget = targetNodeInPatch.getChildOrder(time);
		if (!orderedChildrenTarget.contains(node)) {
			// TODO: throw exception
			return false;
		}
		int indexTarget = orderedChildrenTarget.indexOf(node);
		DiffNode<DiffLinesLabel> neighborBeforeTarget = getChildFromListIfIndexInRange(orderedChildrenTarget,
				indexTarget - 1);
		DiffNode<DiffLinesLabel> neighborAfterTarget = getChildFromListIfIndexInRange(orderedChildrenTarget,
				indexTarget + 1);

		List<DiffNode<DiffLinesLabel>> orderedChildrenSource = root.getParent(time).getChildOrder(time);
		int indexSource = orderedChildrenSource.indexOf(root);
		DiffNode<DiffLinesLabel> neighborBeforeSource = getChildFromListIfIndexInRange(orderedChildrenSource,
				indexSource - 1);
		DiffNode<DiffLinesLabel> neighborAfterSource = getChildFromListIfIndexInRange(orderedChildrenSource,
				indexSource + 1);

		int indexSourceBefore = indexSource - 1;
		while (neighborBeforeSource != null && neighborBeforeSource.diffType == DiffType.REM && indexSourceBefore > 0) {
			indexSourceBefore--;
			neighborBeforeSource = getChildFromListIfIndexInRange(orderedChildrenSource, indexSourceBefore - 1);
		}

		int indexTargetBefore = indexTarget - 1;
		while (neighborBeforeTarget != null && neighborBeforeTarget.diffType == DiffType.REM && indexTargetBefore > 0) {
			indexTargetBefore--;
			neighborBeforeTarget = getChildFromListIfIndexInRange(orderedChildrenTarget, indexTargetBefore - 1);
		}

		if (debug) {
			if (neighborBeforeSource != null) {
				System.out.print("Neighbor before: " + neighborBeforeSource.toString() + "; ");
			} else {
				System.out.print("No neighbor before; ");
			}
			if (neighborAfterSource != null) {
				System.out.print("Neighbor after: " + neighborAfterSource.toString() + "\n");
			} else {
				System.out.print("No neighbor after\n");
			}
		}

		if ((neighborBeforeSource != null && neighborBeforeTarget == null)
				|| (neighborBeforeSource == null && neighborBeforeTarget != null)) {
			System.out.println("Different neighbors before");
			return false;
		}
		if (neighborBeforeSource != null && neighborBeforeTarget != null) {
			if (!Patching.isSameAs(neighborBeforeSource, neighborBeforeTarget, time)) {
				System.out.println("Different neighbor before");
				return false;
			}
		}
		// neighborBeforeSource isSameAs neighborBeforeTarget OR both are null (no
		// neighbor before)

		if ((neighborAfterSource != null && neighborAfterTarget == null)
				|| (neighborAfterSource == null && neighborAfterTarget != null)) {
			System.out.println("Different neighbors after");
			return false;
		}
		if (neighborAfterSource != null && neighborAfterTarget != null) {
			if (!Patching.isSameAs(neighborAfterSource, neighborAfterTarget, time)) {
				System.out.println("Different neighbors after");
				return false;
			}
		}
		// neighborAfterSource isSameAs neighborAfterTarget OR both are null (no
		// neighbor after)

		return true;

	}

	private static int findPositionOfMatchingNeighborInList(DiffNode<DiffLinesLabel> neighbor,
			List<DiffNode<DiffLinesLabel>> list, Time time) throws Exception {
		ArrayList<Integer> positions = new ArrayList<Integer>();
		for (DiffNode<DiffLinesLabel> node : list) {
			if (Patching.isSameAs(neighbor, node, time)) {
				positions.add(list.indexOf(node));
			}
		}
		if (positions.size() == 1) {
			return positions.get(0);
		}
		if (positions.size() > 1) {
			// TODO: throw exception?
			throw new Exception();
		}
		return -1;
	}
	
	private static boolean isAlignmentProblem(List<DiffNode<DiffLinesLabel>> subList, Set<String> deselectedFeatures, Time time) {
		boolean isAlignmentProblem = false;
		for (DiffNode<DiffLinesLabel> node : subList) {
			Set<String> containedFeatures = node.getPresenceCondition(time).getUniqueContainedFeatures();
			System.out.println(containedFeatures);
			isAlignmentProblem = containedFeatures.stream().anyMatch(feature -> deselectedFeatures.contains(feature));
			System.out.println("is alignment problem: " + isAlignmentProblem);
		}
		return isAlignmentProblem;
	}

	private static int findInsertPosition(DiffNode<DiffLinesLabel> root, DiffNode<DiffLinesLabel> targetNodeInPatch, Set<String> deselectedFeatures,
			Time time, boolean debug) throws Exception {
		if (debug)
			System.out.println("Root node to insert: " + root.toString());
		List<DiffNode<DiffLinesLabel>> orderedChildrenTarget = targetNodeInPatch.getChildOrder(time);
		if (debug)
			System.out.println("Children of target node: " + orderedChildrenTarget.toString());
		List<DiffNode<DiffLinesLabel>> orderedChildrenSource = root.getParent(time).getChildOrder(time);
		if (debug)
			System.out.println("Children of source node: " + orderedChildrenSource.toString());
		int indexSource = orderedChildrenSource.indexOf(root);
		DiffNode<DiffLinesLabel> neighborBeforeSource = getChildFromListIfIndexInRange(orderedChildrenSource,
				indexSource - 1);
		DiffNode<DiffLinesLabel> neighborAfterSource = getChildFromListIfIndexInRange(orderedChildrenSource,
				indexSource + 1);
		if (neighborAfterSource != null && neighborAfterSource.diffType == DiffType.ADD) {
			neighborAfterSource = null;
		}
		if (debug) {
			if (neighborBeforeSource != null) {
				System.out.print("Neighbor before: " + neighborBeforeSource.toString() + "; ");
			} else {
				System.out.print("No neighbor before; ");
			}
			if (neighborAfterSource != null) {
				System.out.print("Neighbor after: " + neighborAfterSource.toString() + "\n");
			} else {
				System.out.print("No neighbor after\n");
			}
		}

		int indexBefore = -2;
		int indexAfter = -2;

		if (neighborBeforeSource != null) {
			indexBefore = findPositionOfMatchingNeighborInList(neighborBeforeSource, orderedChildrenTarget, time);
		}
		if (neighborAfterSource != null) {
			indexAfter = findPositionOfMatchingNeighborInList(neighborAfterSource, orderedChildrenTarget, time);
		}
		if (indexBefore == -2 && indexAfter == -2) {
			System.out.println("No neighbors before or after the target");
			return 0;
		}
		if (indexBefore > -1 && indexAfter > -1) {
			if (indexAfter - indexBefore > 1) {
				// Alignment Problem ?
				// TODO: Check if code belongs to features which are only present in target variant 
				List<DiffNode<DiffLinesLabel>> orderedChildrenTargetSubList = orderedChildrenTarget.subList(indexBefore + 1, indexAfter);
				System.out.println(orderedChildrenTargetSubList);
				if (isAlignmentProblem(orderedChildrenTargetSubList, deselectedFeatures, time)) {
					System.out.println("ALIGNMENT PROBLEM. Possible insert positions: from " + (indexBefore + 1) + " to "
							+ indexAfter);
				} else {
					throw new Exception("Reject");
				}
				return -1;
			}
			return indexAfter;
		}
		return Math.max(indexBefore + 1, indexAfter);
	}

	private static void applyChanges(DiffType type, VariationDiff<DiffLinesLabel> targetVariantDiffUnchanged,
			VariationDiff<DiffLinesLabel> targetVariantDiffPatched, List<DiffNode<DiffLinesLabel>> subtreeRoots,
			VariationDiffSource source, Set<String> deselectedFeatures, boolean debug) throws Exception {

		Time time = (type == DiffType.ADD) ? Time.AFTER : Time.BEFORE;

		for (DiffNode<DiffLinesLabel> root : subtreeRoots) {
			if (debug) {
				DiffNode<DiffLinesLabel> newRoot = DiffNode.createRoot(new DiffLinesLabel());
				newRoot.addChild(root.deepCopy(), time);
				VariationDiff<DiffLinesLabel> subTree = new VariationDiff<DiffLinesLabel>(newRoot, source);
				GameEngine.showAndAwaitAll(Show.diff(subTree));
			}

			List<DiffNode<DiffLinesLabel>> targetNodes = new ArrayList<DiffNode<DiffLinesLabel>>();
			if (root.isArtifact()) {
				targetNodes = targetVariantDiffUnchanged.computeAllNodesThat(
						node -> node.getPresenceCondition(Time.AFTER).equals(root.getPresenceCondition(time))
								&& node.isAnnotation());
			} else if (root.isAnnotation()) {
				targetNodes = targetVariantDiffUnchanged
						.computeAllNodesThat(node -> node.getPresenceCondition(Time.AFTER)
								.equals(root.getParent(time).getPresenceCondition(time)) && node.isAnnotation());
			}

			if (targetNodes.size() != 1) {
				System.out.println("too much or too less target nodes found");
			} else {
				DiffNode<DiffLinesLabel> targetNodeInPatch = targetVariantDiffPatched
						.getNodeWithID(targetNodes.get(0).getID());
				System.out.println(targetNodeInPatch.toString());
				if (type == DiffType.ADD) {

					int insertPosition = findInsertPosition(root, targetNodeInPatch, deselectedFeatures, time, true);
					if (insertPosition < 0) {
						System.out.println("no matching insert position found");
					} else {
						System.out.println("subtree added");
						targetNodeInPatch.insertChild(root.deepCopy(), insertPosition, time);
						System.out.println(targetNodeInPatch.getChildOrder(time));
					}

				} else if (type == DiffType.REM) {
					List<DiffNode<DiffLinesLabel>> nodesToRem = new ArrayList<DiffNode<DiffLinesLabel>>();
					System.out.println("Root: " + root.toString());
					System.out.println("Children: " + targetNodeInPatch.getAllChildrenSet());
					targetNodeInPatch.getAllChildrenStream().forEach(node -> {
						if (Patching.isSameAs(node, root, time))
							nodesToRem.add(node);
					});
					System.out.println("Nodes to remove: " + nodesToRem);

					List<DiffNode<DiffLinesLabel>> nodesToRemAfterCheckingNeighbors = nodesToRem.stream()
							.filter((node) -> checkNeighbors(root, targetNodeInPatch, node, time, true)).toList();

					if (nodesToRemAfterCheckingNeighbors.size() != 1) {
						System.out.println("too much or too less target nodes found");
						System.out.println(nodesToRemAfterCheckingNeighbors.toString());
					} else {
						System.out.println("subtree removed");
						nodesToRemAfterCheckingNeighbors.get(0).diffType = DiffType.REM;
						nodesToRemAfterCheckingNeighbors.get(0).getAllChildrenStream()
								.forEach(node -> node.diffType = DiffType.REM);
						nodesToRemAfterCheckingNeighbors.get(0).drop(Time.AFTER);
						System.out.println(targetNodes.get(0).getChildOrder(Time.AFTER));
					}
				}
				GameEngine.showAndAwaitAll(Show.diff(targetVariantDiffPatched));
			}
		}
	}

	private static void patchVariationTrees(VariationTree<DiffLinesLabel> sourceVariantVersion1,
			VariationTree<DiffLinesLabel> sourceVariantVersion2, VariationTree<DiffLinesLabel> targetVariant)
			throws Exception {
		if (sourceVariantVersion1 == null || sourceVariantVersion2 == null || targetVariant == null) {
			System.out.println("Parsing error");
			return;
		}
		VariationDiff<DiffLinesLabel> diff = VariationDiff.fromTrees(sourceVariantVersion1, sourceVariantVersion2);
		patchVariationTrees(diff, targetVariant);
	}

	private static void patchVariationTrees(VariationDiff<DiffLinesLabel> diff, VariationTree<DiffLinesLabel> targetVariant)
			throws Exception {
		
//		Relevance rho = calculateFeatureSetToDeselectFromTrees(sourceVariantVersion1, sourceVariantVersion2, targetVariant,
//				false);
		Set<String> deselectedFeatures = calculateFeatureSetToDeselectFromDiff(diff, targetVariant, false);
		Relevance rho = calculateFormulaForDeselection(deselectedFeatures, false);
		VariationDiff<DiffLinesLabel> optimizedDiff = DiffView.optimized(diff, rho);
		VariationDiffSource source = optimizedDiff.getSource();
		VariationDiff<DiffLinesLabel> targetVariantDiffUnchanged = targetVariant.toCompletelyUnchangedVariationDiff();
		VariationDiff<DiffLinesLabel> targetVariantDiffPatched = targetVariant.toCompletelyUnchangedVariationDiff();

		// add new nodes
		Set<DiffNode<DiffLinesLabel>> addedNodes = new HashSet<DiffNode<DiffLinesLabel>>();
		optimizedDiff.forAll(node -> {
			if (node.isAdd()) {
				addedNodes.add(node);
			}
		});
		Set<DiffNode<DiffLinesLabel>> addedSubtreeRoots = findRootsOfSubtrees(addedNodes, DiffType.ADD, false);
		List<DiffNode<DiffLinesLabel>> addedSortedSubtreeRoots = addedSubtreeRoots.stream().sorted((n1, n2) -> Integer
				.compare(n1.getLinesAtTime(Time.AFTER).fromInclusive(), n2.getLinesAtTime(Time.AFTER).fromInclusive()))
				.collect(Collectors.toList());
		applyChanges(DiffType.ADD, targetVariantDiffUnchanged, targetVariantDiffPatched, addedSortedSubtreeRoots,
				source, deselectedFeatures, true);

		// remove old nodes
		Set<DiffNode<DiffLinesLabel>> removedNodes = new HashSet<DiffNode<DiffLinesLabel>>();
		optimizedDiff.forAll(node -> {
			if (node.isRem()) {
				removedNodes.add(node);
			}
		});
		Set<DiffNode<DiffLinesLabel>> removedSubtreeRoots = findRootsOfSubtrees(removedNodes, DiffType.REM, false);
		List<DiffNode<DiffLinesLabel>> removedSortedSubtreeRoots = removedSubtreeRoots.stream()
				.sorted((n1, n2) -> Integer.compare(n1.getLinesAtTime(Time.BEFORE).fromInclusive(),
						n2.getLinesAtTime(Time.BEFORE).fromInclusive()))
				.collect(Collectors.toList());
		applyChanges(DiffType.REM, targetVariantDiffUnchanged, targetVariantDiffPatched, removedSortedSubtreeRoots,
				source, deselectedFeatures, true);
		GameEngine.showAndAwaitAll(Show.diff(optimizedDiff));
//		GameEngine.showAndAwaitAll(Show.tree(sourceVariantVersion1), Show.tree(sourceVariantVersion2),
//				Show.tree(targetVariant), Show.diff(optimizedDiff), Show.diff(targetVariantDiffPatched),
//				Show.tree(targetVariantDiffPatched.project(Time.AFTER)));
	}

	private static VariationDiff<DiffLinesLabel> parseVariationDiffFromFiles(String file1, String file2)
			throws IOException, DiffParseException {
		Path examplesDir = Path.of("data", "examples");
		return VariationDiff.fromFiles(examplesDir.resolve(file1), examplesDir.resolve(file2),
				DiffAlgorithm.SupportedAlgorithm.MYERS, VariationDiffParseOptions.Default);
	}

	private static VariationTree<DiffLinesLabel> parseVariationTreeFromFile(String file) {
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

	public static void main(String[] args) {
		try {
//			patchVariationTrees(parseVariationTreeFromFile("exampleA1Add.cpp"),
//					parseVariationTreeFromFile("exampleA2Add.cpp"), parseVariationTreeFromFile("exampleBAdd.cpp"));
//			patchVariationTrees(parseVariationTreeFromFile("exampleA1Rem.cpp"),
//					parseVariationTreeFromFile("exampleA2Rem.cpp"), parseVariationTreeFromFile("exampleBRem.cpp"));
//			patchVariationTrees(parseVariationDiffFromFiles("exampleA1RemAdd.cpp", "exampleA2RemAdd.cpp"),
//					parseVariationTreeFromFile("exampleBRemAdd.cpp"));
//			patchVariationTrees(parseVariationTreeFromFile("exampleA1RemAdd.cpp"),
//					parseVariationTreeFromFile("exampleA2RemAdd.cpp"),
//					parseVariationTreeFromFile("exampleBRemAdd.cpp"));
			patchVariationTrees(parseVariationDiffFromFiles("exampleA1AddAlignmentP.cpp", "exampleA2AddAlignmentP.cpp"),
					parseVariationTreeFromFile("exampleBAddAlignmentP.cpp"));
		} catch (Exception e) {
			System.out.println("Rejected");
			e.printStackTrace();
		}
	}

}
