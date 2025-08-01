package org.variantsync.diffdetective.variation.diff.patching;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jgit.diff.DiffAlgorithm;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.show.Show;
import org.variantsync.diffdetective.show.engine.GameEngine;
import org.variantsync.diffdetective.util.fide.FixTrueFalse.Formula;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.DiffType;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;
import org.variantsync.diffdetective.variation.diff.source.VariationDiffSource;
import org.variantsync.diffdetective.variation.diff.view.DiffView;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.diffdetective.variation.tree.view.relevance.Configure;
import org.variantsync.diffdetective.variation.tree.view.relevance.Relevance;

public class Patching {

	public static <L extends Label> boolean isSameAs(VariationDiff<L> diff1, VariationDiff<L> diff2) {
		return isSameAsWithoutLabel(diff1.getRoot(), diff2.getRoot());
	}

	public static <L extends Label> boolean isSameAs(DiffNode<L> a, DiffNode<L> b) {
		return isSameAs(a, b, new HashSet<>());
	}

	public static <L extends Label> boolean isSameAsWithoutLabel(DiffNode<L> a, DiffNode<L> b) {
		return isSameAsWithoutLabel(a, b, new HashSet<>());
	}

	private static <L extends Label> boolean isSameAs(DiffNode<L> a, DiffNode<L> b, Set<DiffNode<L>> visited) {
		if (!visited.add(a)) {
			return true;
		}

		if (!(a.getNodeType().equals(b.getNodeType()) && a.getLabel().toString().equals(b.getLabel().toString()) &&
//				a.getLabel().equals(b.getLabel()) &&
//                a.getFromLine().atTime(time) == (b.getFromLine().atTime(time)) &&
//                a.getToLine().atTime(time) == (b.getToLine().atTime(time)) &&
				(a.getFormula() == null ? b.getFormula() == null : a.getFormula().equals(b.getFormula()))
				&& a.getLabel().getLines().equals(b.getLabel().getLines()))) {
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

	private static <L extends Label> boolean isSameAsWithoutLabel(DiffNode<L> a, DiffNode<L> b,
			Set<DiffNode<L>> visited) {
		if (!visited.add(a)) {
			return true;
		}

		if (!(a.getNodeType().equals(b.getNodeType()) &&
//				a.getLabel().equals(b.getLabel()) &&
//                a.getFromLine().atTime(time) == (b.getFromLine().atTime(time)) &&
//                a.getToLine().atTime(time) == (b.getToLine().atTime(time)) &&
				(a.getFormula() == null ? b.getFormula() == null : a.getFormula().equals(b.getFormula()))
				&& a.getLabel().getLines().equals(b.getLabel().getLines()))) {
			return false;
		}

		Iterator<DiffNode<L>> aIt = a.getAllChildren().iterator();
		Iterator<DiffNode<L>> bIt = b.getAllChildren().iterator();
		while (aIt.hasNext() && bIt.hasNext()) {
			if (!isSameAsWithoutLabel(aIt.next(), bIt.next(), visited)) {
				return false;
			}
		}

		return aIt.hasNext() == bIt.hasNext();
	}

	private static Set<String> calculateSetMinusOfFeatureSets(Set<String> featureSet1, Set<String> featureSet2,
			boolean debug) {
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

	private static Set<String> calculateFeatureSetToDeselectFromDiff(VariationDiff<DiffLinesLabel> diff,
			VariationTree<DiffLinesLabel> variant2, boolean debug) {
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

	private static boolean checkForZeroVariantDrift(VariationDiff<DiffLinesLabel> diffVariant1,
			VariationTree<DiffLinesLabel> variant2, Relevance deselectedFeatures, boolean debug) {
		diffVariant1 = DiffView.optimized(diffVariant1.project(Time.BEFORE).toCompletelyUnchangedVariationDiff(),
				deselectedFeatures);
		VariationDiff<DiffLinesLabel> diffVariant2 = DiffView.optimized(variant2.toCompletelyUnchangedVariationDiff(),
				deselectedFeatures);
		if (debug)
			GameEngine.showAndAwaitAll(Show.diff(diffVariant1), Show.diff(diffVariant2));
		if (Patching.isSameAs(diffVariant1, diffVariant2)) {
			return true;
		}
		return false;
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

	private static ArrayList<DiffNode<DiffLinesLabel>> getChildrenFromListIfIndexInRange(
			List<DiffNode<DiffLinesLabel>> childrenList, int index, int numberOfChildren, boolean isBefore) {
		int number = 0;
		if (isBefore)
			index = index - numberOfChildren;
		ArrayList<DiffNode<DiffLinesLabel>> children = new ArrayList<>();
		while (number < numberOfChildren) {
			if (index + number >= 0 && index + number < childrenList.size()) {
				children.add(childrenList.get(index + number));
			}
			number++;
		}
		return children;
	}

	private static boolean checkNeighbors(DiffNode<DiffLinesLabel> root, DiffNode<DiffLinesLabel> targetNodeInPatch,
			DiffNode<DiffLinesLabel> node, Set<String> deselectedFeatures, Time time, boolean debug) throws Exception {

		// TODO
		int contextSize = 10;

		List<DiffNode<DiffLinesLabel>> orderedChildrenTarget = targetNodeInPatch.getChildOrder(time);
		if (!orderedChildrenTarget.contains(node)) {
			// TODO: throw exception
			return false;
		}
		int indexTarget = orderedChildrenTarget.indexOf(node);
		List<DiffNode<DiffLinesLabel>> neighborBeforeTarget = getChildrenFromListIfIndexInRange(orderedChildrenTarget,
				indexTarget, contextSize, true);
		List<DiffNode<DiffLinesLabel>> neighborAfterTarget = getChildrenFromListIfIndexInRange(orderedChildrenTarget,
				indexTarget, contextSize, false);

		int indexSourceMatchingNeighborBefore = -2;
		int indexSourceMatchingNeighborAfter = -2;
		List<DiffNode<DiffLinesLabel>> orderedChildrenSource = root.getParent(time).getChildOrder(time);

		if (debug) {
			System.out.println("Children Target: " + orderedChildrenTarget);
			System.out.println("Children Source: " + orderedChildrenSource);
		}

		if (neighborBeforeTarget != null) {
			List<Integer> positions = findPositionsOfMatchingNeighborsInList(neighborBeforeTarget,
					orderedChildrenSource, time);
			indexSourceMatchingNeighborBefore = findNearestPositionToIndex(positions, indexTarget, true) + contextSize;
		}
		if (neighborAfterTarget != null) {
			List<Integer> positions = findPositionsOfMatchingNeighborsInList(neighborAfterTarget, orderedChildrenSource,
					time);
			indexSourceMatchingNeighborAfter = findNearestPositionToIndex(positions, indexTarget, false);
		}
		if (indexSourceMatchingNeighborBefore == -2 && indexSourceMatchingNeighborAfter == -2) {
			if (debug)
				System.out.println("No neighbors before or after the target");
			return true;
		}
		if (indexSourceMatchingNeighborBefore > -1 && indexSourceMatchingNeighborAfter > -1) {
			if (indexSourceMatchingNeighborAfter - indexSourceMatchingNeighborBefore > 2) {
				// Alignment Problem ?
				// Check if code belongs to features which are only present in target
				// variant
				List<DiffNode<DiffLinesLabel>> orderedChildrenTargetSubList = orderedChildrenTarget
						.subList(indexSourceMatchingNeighborBefore + 1, indexSourceMatchingNeighborAfter);
				if (debug)
					System.out.println(orderedChildrenTargetSubList);
				if (isAlignmentProblem(orderedChildrenTargetSubList, deselectedFeatures, time, debug)) {
					if (debug)
						System.out.println("ALIGNMENT PROBLEM. Node can be removed.");
				} else {
					return false;
				}
			}
		}
		return true;
	}

	private static int findNearestPositionToIndex(List<Integer> positions, int indexTarget, boolean isBefore) {
		Optional<Integer> pos;
		if (isBefore) {
			pos = positions.stream().map(p -> (indexTarget - p)).filter(p -> p > 0)
					.min(Integer::compare);
		} else {
			pos = positions.stream().map(p -> (p - indexTarget)).filter(p -> p > 0)
					.min(Integer::compare);
		}
		try {
			return pos.get();
		} catch (NoSuchElementException e) {
			return -1;
		}

	}

	private static ArrayList<Integer> findPositionsOfMatchingNeighborsInList(List<DiffNode<DiffLinesLabel>> neighbors,
			List<DiffNode<DiffLinesLabel>> list, Time time) throws Exception {
		ArrayList<Integer> positions = new ArrayList<Integer>();
		if (neighbors.size() == 0)
			return positions;
		for (int i = 0; i < list.size(); i++) {
			boolean allNeighborsAreSame = false;
			if (Patching.isSameAs(neighbors.get(0), list.get(i))) {
				allNeighborsAreSame = true;
				for (int j = 1; j < neighbors.size(); j++) {
					if (list.size() > (i + j) && !Patching.isSameAs(neighbors.get(j), list.get(i + j))) {
						allNeighborsAreSame = false;
					}
				}
				if (allNeighborsAreSame)
					positions.add(i);
			}
		}
		return positions;
	}

	private static boolean isAlignmentProblem(List<DiffNode<DiffLinesLabel>> subList, Set<String> deselectedFeatures,
			Time time, boolean debug) {
		boolean isAlignmentProblem = false;
		for (DiffNode<DiffLinesLabel> node : subList) {
			Set<String> containedFeatures = node.getPresenceCondition(time).getUniqueContainedFeatures();
			if (debug)
				System.out.println(containedFeatures);
			isAlignmentProblem = containedFeatures.stream().anyMatch(feature -> deselectedFeatures.contains(feature));
			if (debug)
				System.out.println("is alignment problem: " + isAlignmentProblem);
		}
		return isAlignmentProblem;
	}

	private static int findInsertPosition(DiffNode<DiffLinesLabel> root, DiffNode<DiffLinesLabel> targetNodeInPatch,
			Set<String> deselectedFeatures, Time time, boolean debug) throws Exception {

		// TODO
		int contextSize = 10;

		if (debug)
			System.out.println("Root node to insert: " + root.toString());
		List<DiffNode<DiffLinesLabel>> orderedChildrenTarget = targetNodeInPatch.getChildOrder(time);
		if (debug)
			System.out.println("Children of target node: " + orderedChildrenTarget.toString());
		List<DiffNode<DiffLinesLabel>> orderedChildrenSource = root.getParent(time).getChildOrder(time);
		if (debug)
			System.out.println("Children of source node: " + orderedChildrenSource.toString());
		int indexSource = orderedChildrenSource.indexOf(root);
		List<DiffNode<DiffLinesLabel>> neighborBeforeSource = getChildrenFromListIfIndexInRange(orderedChildrenSource,
				indexSource, contextSize, true);
		List<DiffNode<DiffLinesLabel>> neighborAfterSource = getChildrenFromListIfIndexInRange(orderedChildrenSource,
				indexSource, contextSize, false);
		int currentIndexAfter = indexSource + 1;
		//ignore neighbors with DiffType ADD because they are added afterwards
		neighborAfterSource = neighborAfterSource.stream().filter(n -> n.diffType != DiffType.ADD).toList();
//		while (neighborAfterSource != null && neighborAfterSource.diffType == DiffType.ADD) {
//			currentIndexAfter++;
//			neighborAfterSource = getChildrenFromListIfIndexInRange(orderedChildrenSource, currentIndexAfter);
//		}

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

		if (!neighborBeforeSource.isEmpty()) {
			List<Integer> positions = findPositionsOfMatchingNeighborsInList(neighborBeforeSource,
					orderedChildrenTarget, time);
			if (positions.size() == 1)
				indexBefore = positions.get(0);
			else
				throw new Exception(
						"too many insert positions found: " + positions.size() + " with context size: " + contextSize);
		}
		if (!neighborAfterSource.isEmpty()) {
			List<Integer> positions = findPositionsOfMatchingNeighborsInList(neighborAfterSource, orderedChildrenTarget,
					time);
			if (positions.size() == 1)
				indexAfter = positions.get(0);
			else
				throw new Exception(
						"too many insert positions found: " + positions.size() + " with context size: " + contextSize);
		}
		if (indexBefore == -2 && indexAfter == -2) {
			if (orderedChildrenTarget.isEmpty()) {
				if (debug)
					System.out.println("No neighbors before or after the target");
				return 0;
			}
			if (isAlignmentProblem(orderedChildrenTarget, deselectedFeatures, time, debug)) {
				if (debug)
					System.out.println("ALIGNMENT PROBLEM. Possible insert positions: from " + 1 + " to "
							+ (orderedChildrenTarget.size() - 1));
				return -1;
			} else {
				throw new Exception("Reject");
			}
		}
		if (indexBefore == -2) {
			List<DiffNode<DiffLinesLabel>> orderedChildrenTargetSubList = orderedChildrenTarget.subList(0, indexAfter);
			if (orderedChildrenTargetSubList.size() > 1) {
				if (isAlignmentProblem(orderedChildrenTargetSubList, deselectedFeatures, time, debug)) {
					if (debug)
						System.out.println(
								"ALIGNMENT PROBLEM. Possible insert positions: from " + 1 + " to " + indexAfter);
				} else {
					throw new Exception("Reject");
				}
				return -1;
			}
		}
		if (indexAfter == -2) {
			List<DiffNode<DiffLinesLabel>> orderedChildrenTargetSubList = orderedChildrenTarget.subList(indexBefore + 1,
					orderedChildrenTarget.size());
			if (debug)
				System.out.println(orderedChildrenTargetSubList);
			if (orderedChildrenTargetSubList.size() > 1) {
				if (isAlignmentProblem(orderedChildrenTargetSubList, deselectedFeatures, time, debug)) {
					if (debug)
						System.out.println("ALIGNMENT PROBLEM. Possible insert positions: from " + (indexBefore + 1)
								+ " to " + (orderedChildrenTarget.size() - 1));
				} else {
					throw new Exception("Reject");
				}
				return -1;
			}
		}
		if (indexBefore > -1 && indexAfter > -1) {
			if (indexAfter - (indexBefore + 1) > 1) {
				// Alignment Problem ?
				// TODO: Check if code belongs to features which are only present in target
				// variant
				List<DiffNode<DiffLinesLabel>> orderedChildrenTargetSubList = orderedChildrenTarget
						.subList(indexBefore + 1, indexAfter);
				if (debug)
					System.out.println(orderedChildrenTargetSubList);
				if (isAlignmentProblem(orderedChildrenTargetSubList, deselectedFeatures, time, debug)) {
					if (debug)
						System.out.println("ALIGNMENT PROBLEM. Possible insert positions: from " + (indexBefore + 1)
								+ " to " + indexAfter);
				} else {
					throw new Exception("Reject");
				}
				return -1;
			}
			if (indexAfter - indexBefore < 0) {
				// TODO: root must be between neighbors
				if (debug)
					System.out.println("Neighbors in wrong order");
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
				if (debug)
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

			List<DiffNode<DiffLinesLabel>> targetNodes2 = new ArrayList<DiffNode<DiffLinesLabel>>();
			for (DiffNode<DiffLinesLabel> targetNode : targetNodes) {
				DiffNode<DiffLinesLabel> targetNodeInPatch = targetVariantDiffPatched.getNodeWithID(targetNode.getID());
				if (debug)
					System.out.println(targetNodeInPatch.toString());
				if (type == DiffType.ADD) {

					int insertPosition = findInsertPosition(root, targetNodeInPatch, deselectedFeatures, time, debug);
					if (insertPosition < 0) {
						if (debug)
							System.out.println("no matching insert position found");
					} else {
						targetNodes2.add(targetNodeInPatch);
					}

				} else if (type == DiffType.REM) {
					List<DiffNode<DiffLinesLabel>> nodesToRem = new ArrayList<DiffNode<DiffLinesLabel>>();
					if (debug)
						System.out.println("Root: " + root.toString());
					if (debug)
						System.out.println("Children: " + targetNodeInPatch.getAllChildrenSet());
					targetNodeInPatch.getAllChildrenStream().forEach(node -> {
						try {
							if (Patching.isSameAs(node, root)
									&& checkNeighbors(root, targetNodeInPatch, node, deselectedFeatures, time, debug))
								nodesToRem.add(node);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					});
					if (debug)
						System.out.println("Nodes to remove: " + nodesToRem);

					if (nodesToRem.size() != 1) {
						if (debug)
							System.out.println("too much or too less target nodes found");
						if (debug)
							System.out.println(nodesToRem.toString());
					} else {
						targetNodes2.add(nodesToRem.get(0));
					}
				}
				if (debug)
					GameEngine.showAndAwaitAll(Show.diff(targetVariantDiffPatched));
			}

			if (targetNodes2.size() != 1) {
				throw new Exception("too much or too less targetNodes found: " + targetNodes2.size());
			} else {
				DiffNode<DiffLinesLabel> targetNodeInPatch = targetNodes2.get(0);
				if (type == DiffType.ADD) {
					if (debug)
						System.out.println("subtree added");
					targetNodeInPatch.insertChild(root.deepCopy(),
							findInsertPosition(root, targetNodeInPatch, deselectedFeatures, time, debug), time);
					if (debug)
						System.out.println(targetNodeInPatch.getChildOrder(time));
				} else if (type == DiffType.REM) {
					if (debug)
						System.out.println("subtree removed");
					removeNode(targetNodeInPatch);
					if (debug)
						System.out.println(targetNodes2.get(0).getChildOrder(Time.AFTER));
				}
			}

		}
	}

	private static void removeNode(DiffNode<DiffLinesLabel> node) {
		Set<DiffNode<DiffLinesLabel>> children = node.getAllChildrenSet();
		if (!children.isEmpty()) {
			children.forEach(child -> removeNode(child));
		}
		node.diffType = DiffType.REM;
		node.drop(Time.AFTER);
	}

	private static VariationTree<DiffLinesLabel> patchVariationTrees(
			VariationTree<DiffLinesLabel> sourceVariantVersion1, VariationTree<DiffLinesLabel> sourceVariantVersion2,
			VariationTree<DiffLinesLabel> targetVariant, boolean debug) throws Exception {
		if (sourceVariantVersion1 == null || sourceVariantVersion2 == null || targetVariant == null) {
			if (debug)
				System.out.println("Parsing error");
			return null;
		}
		VariationDiff<DiffLinesLabel> diff = VariationDiff.fromTrees(sourceVariantVersion1, sourceVariantVersion2);
		return patchVariationTrees(diff, targetVariant, debug);
	}

	public static VariationTree<DiffLinesLabel> patchVariationTrees(VariationDiff<DiffLinesLabel> diff,
			VariationTree<DiffLinesLabel> targetVariant, boolean debug) throws Exception {

//		Relevance rho = calculateFeatureSetToDeselectFromTrees(sourceVariantVersion1, sourceVariantVersion2, targetVariant,
//				false);
		Set<String> deselectedFeatures = calculateFeatureSetToDeselectFromDiff(diff, targetVariant, debug);
		Relevance rho = calculateFormulaForDeselection(deselectedFeatures, debug);

		if (!checkForZeroVariantDrift(diff, targetVariant, rho, debug)) {
			throw new Exception("Variants evolved independently: No Zero Variant Drift");
		}

		VariationDiff<DiffLinesLabel> optimizedDiff = DiffView.optimized(diff, rho);
		VariationDiffSource source = optimizedDiff.getSource();
		VariationDiff<DiffLinesLabel> targetVariantDiffUnchanged = targetVariant.toCompletelyUnchangedVariationDiff();
		VariationDiff<DiffLinesLabel> targetVariantDiffPatched = targetVariant.toCompletelyUnchangedVariationDiff();

		if (debug)
			GameEngine.showAndAwaitAll(Show.diff(targetVariantDiffPatched), Show.diff(optimizedDiff));

		Set<DiffNode<DiffLinesLabel>> removedNodes = new HashSet<DiffNode<DiffLinesLabel>>();
		Set<DiffNode<DiffLinesLabel>> addedNodes = new HashSet<DiffNode<DiffLinesLabel>>();

		// find nodes with DiffType NON but changed parents
		optimizedDiff.forAll(node -> {
			if (node.isNon() && node.getParent(Time.BEFORE) != node.getParent(Time.AFTER)) {
				removedNodes.add(node);
				addedNodes.add(node);
			}
		});

		// remove old nodes
		optimizedDiff.forAll(node -> {
			if (node.isRem()) {
				removedNodes.add(node);
			}
		});
		Set<DiffNode<DiffLinesLabel>> removedSubtreeRoots = findRootsOfSubtrees(removedNodes, DiffType.REM, debug);
		List<DiffNode<DiffLinesLabel>> removedSortedSubtreeRoots = removedSubtreeRoots.stream()
				.sorted((n1, n2) -> Integer.compare(n1.getLinesAtTime(Time.BEFORE).fromInclusive(),
						n2.getLinesAtTime(Time.BEFORE).fromInclusive()))
				.collect(Collectors.toList());
		applyChanges(DiffType.REM, targetVariantDiffUnchanged, targetVariantDiffPatched, removedSortedSubtreeRoots,
				source, deselectedFeatures, debug);

		// add new nodes
		optimizedDiff.forAll(node -> {
			if (node.isAdd()) {
				addedNodes.add(node);
			}
		});
		Set<DiffNode<DiffLinesLabel>> addedSubtreeRoots = findRootsOfSubtrees(addedNodes, DiffType.ADD, debug);
		List<DiffNode<DiffLinesLabel>> addedSortedSubtreeRoots = addedSubtreeRoots.stream().sorted((n1, n2) -> Integer
				.compare(n1.getLinesAtTime(Time.AFTER).fromInclusive(), n2.getLinesAtTime(Time.AFTER).fromInclusive()))
				.collect(Collectors.toList());
		applyChanges(DiffType.ADD, targetVariantDiffUnchanged, targetVariantDiffPatched, addedSortedSubtreeRoots,
				source, deselectedFeatures, debug);

		if (debug)
			GameEngine.showAndAwaitAll(Show.diff(diff));
		if (debug)
			GameEngine.showAndAwaitAll(Show.diff(optimizedDiff));
//		GameEngine.showAndAwaitAll(Show.diff(diff),
//				Show.tree(targetVariant), Show.diff(optimizedDiff), Show.diff(targetVariantDiffPatched),
//				Show.tree(targetVariantDiffPatched.project(Time.AFTER)));
		return targetVariantDiffPatched.project(Time.AFTER);
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

	public static boolean comparePatchedVariantWithExpectedResult(VariationTree<DiffLinesLabel> patchedVariant,
			VariationTree<DiffLinesLabel> expectedResult) {
		return Patching.isSameAs(patchedVariant.toCompletelyUnchangedVariationDiff(),
				expectedResult.toCompletelyUnchangedVariationDiff());
	}
}
