package org.variantsync.diffdetective.variation.diff.patching;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.jgit.diff.DiffAlgorithm;
import org.prop4j.And;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.variantsync.diffdetective.analysis.logic.SAT;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.experiments.thesis_pm.Generator;
import org.variantsync.diffdetective.show.Show;
import org.variantsync.diffdetective.show.engine.GameEngine;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.VariationLabel;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.DiffType;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.construction.JGitDiff;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;
import org.variantsync.diffdetective.variation.diff.source.VariationDiffSource;
import org.variantsync.diffdetective.variation.diff.transform.CutNonEditedSubtrees;
import org.variantsync.diffdetective.variation.diff.view.DiffView;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.diffdetective.variation.tree.VariationTreeNode;
import org.variantsync.diffdetective.variation.tree.view.TreeView;
import org.variantsync.diffdetective.variation.tree.view.relevance.Configure;
import org.variantsync.diffdetective.variation.tree.view.relevance.ConfigureWithFullConfig;
import org.variantsync.diffdetective.variation.tree.view.relevance.Relevance;
import org.variantsync.diffdetective.variation.tree.view.relevance.Trace;
import org.variantsync.diffdetective.variation.tree.view.relevance.TraceSup;
import org.variantsync.diffdetective.variation.tree.view.relevance.Unchanged;
import org.variantsync.functjonal.Pair;

public class Patching {
	public static <L extends Label> boolean hasSameLabel(L a, L b) {
		String labelA = a.toString().replaceAll(" ", "");
		String labelB = b.toString().replaceAll(" ", "");
		return labelA.equals(labelB);
	}

	public static <L extends Label> boolean isSameAs(VariationDiff<L> diff1, VariationDiff<L> diff2) {
		return isSameAs(diff1.getRoot(), diff2.getRoot());
	}

	public static <L extends Label> boolean isSameAs(DiffNode<L> a, DiffNode<L> b) {
		return isSameAs(a, b, new HashSet<>());
	}

	private static <L extends Label> boolean isSameAs(DiffNode<L> a, DiffNode<L> b, Set<DiffNode<L>> visited) {
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

		Node[] f = new Node[set.size()];
		Iterator<String> iterator = set.iterator();
		for (int i = 0; i < f.length; i++) {
			f[i] = new Literal(iterator.next(), false);
		}
		Node formula = new And(f);

		if (debug) {
			System.out.println(formula);
		}

		return new Configure(formula);
	}

	/**
	 * Adds a feature to the feature map, if it is not contained in the map or the
	 * diffType is different from the value in the map. If the diffTypes are
	 * different, then DiffType NON is written in the map as the value.
	 * 
	 * @param featureMap the map has all features of the variant1 as keys and saves
	 *                   if the feature is only occurring in added lines, and
	 *                   therefore a new feature.
	 * @param feature    the current feature to put in the map
	 * @param diffType   the diffType of the node
	 */
	private static void addFeatureToFeatureMap(Map<String, DiffType> featureMap, String feature, DiffType diffType) {
		if (featureMap.containsKey(feature)) {
			if (!diffType.equals(featureMap.get(feature))) {
				featureMap.replace(feature, DiffType.NON);
			}
		} else {
			featureMap.put(feature, diffType);
		}
	}

	private static Set<String> calculateFeatureSetToDeselectFromDiff(VariationDiff<DiffLinesLabel> diff,
			VariationTree<DiffLinesLabel> variant2, boolean debug, boolean patchNewFeatures) {
		// HashMap of Features which only occur in the revision of Variant1 (new
		// features) -> DiffType is ADD
		Map<String, DiffType> featuresMapV1 = new HashMap<String, DiffType>();
		// HashSet of Feature Names of Variant1
		Set<String> featuresV1 = new HashSet<String>();
		// Collect all features of the conditional annotation nodes of variant1
		diff.forAll(node -> {

			if (node.isConditionalAnnotation() && node.getDiffType().existsAtTime(Time.BEFORE)) {
				node.getFeatureMapping(Time.BEFORE).getUniqueContainedFeatures().forEach(feature -> {
					Patching.addFeatureToFeatureMap(featuresMapV1, feature, node.getDiffType());
				});
			}
			if (node.isConditionalAnnotation() && node.getDiffType().existsAtTime(Time.AFTER)) {
				node.getFeatureMapping(Time.AFTER).getUniqueContainedFeatures().forEach(feature -> {
					Patching.addFeatureToFeatureMap(featuresMapV1, feature, node.getDiffType());
				});
			}
		});
		featuresV1 = featuresMapV1.keySet();

		// Collect all features of the conditional annotation nodes of variant2
		Set<String> featuresV2 = new HashSet<String>();
		variant2.forAllPreorder(node -> {
			if (node.isConditionalAnnotation()) {
				featuresV2.addAll(node.getFeatureMapping().getUniqueContainedFeatures());
			}
		});

		// Calculate the features which are not in both variants
		Set<String> features = calculateSetMinusOfFeatureSets(featuresV1, featuresV2, debug);

		// If new features should be patched, then remove new features from the
		// deselected features if they only occur as ADD in the diff
		if (patchNewFeatures) {
			featuresMapV1.forEach((feature, diffType) -> {
				if (diffType == DiffType.ADD) {
					if (features.contains(feature)) {
						features.remove(feature);
					}
				}
			});
		}

		return features;
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

	private static boolean compareAncestors(DiffNode<DiffLinesLabel> node1, DiffNode<DiffLinesLabel> node2, Time time,
			boolean debug) {
		if (node1.getParent(time) == null && node2.getParent(time) == null)
			return true;
		if (node1.getParent(time) != null && node2.getParent(time) == null)
			return false;
		if (node1.getParent(time) == null && node2.getParent(time) != null)
			return false;
		List<DiffNode<DiffLinesLabel>> siblingsNode1 = node1.getParent(time).getChildOrder(time);
		List<DiffNode<DiffLinesLabel>> siblingsNode2 = node2.getParent(time).getChildOrder(time);

		if (debug) {
			System.out.println("CA1: " + siblingsNode1);
			System.out.println("CA2: " + siblingsNode2);
		}

		int indexNode1 = siblingsNode1.indexOf(node1);
		int indexNode2 = siblingsNode2.indexOf(node2);
		if (indexNode1 != indexNode2) {
			return false;
		}
		int index2 = 0;
		for (int i = 0; i < siblingsNode1.size(); i++) {
			if (i > indexNode1 && siblingsNode1.get(i).getDiffType() == DiffType.ADD) {
				continue;
			}
//			if (i > index2 && siblingsNode2.get(index2).getDiffType() == DiffType.REM)
			if (!hasSameLabel(siblingsNode1.get(i).getLabel(), siblingsNode2.get(index2).getLabel())) {
				return false;
			}
			index2++;
		}
		return compareAncestors(node1.getParent(time), node2.getParent(time), time, debug);
	}

	private static boolean checkNeighborsLabels(DiffNode<DiffLinesLabel> root,
			DiffNode<DiffLinesLabel> targetNodeInPatch, Time time, boolean debug) {
		if (root.getParent(time) == null && targetNodeInPatch == null) {
			return true;
		}
		if ((root.getParent(time) != null && targetNodeInPatch == null)
				|| (root.getParent(time) == null && targetNodeInPatch != null)) {
			return false;
		}
		return compareAncestors(root.getParent(time), targetNodeInPatch, time, debug);
	}

	private static boolean isSameList(List<DiffNode<DiffLinesLabel>> sourceList,
			List<DiffNode<DiffLinesLabel>> targetList, ConfigureWithFullConfig configSource) {
		int indexTarget = 0;
		// source must be equal or smaller than target list, because it is the view on
		// the source patch (cross variant features)
		if (sourceList.size() > targetList.size()) {
			return false;
		}
		if (sourceList.size() == 0) {
			if (targetList.size() == 0) {
				return true;
			}
			// check that all nodes in target list are not present in source
			for (DiffNode<DiffLinesLabel> targetNode : targetList) {
				if (isPresentUnderConfiguration(targetNode, configSource)) {
					return false;
				}
			}
			return true;
		}
		for (DiffNode<DiffLinesLabel> sourceNode : sourceList) {
			if (indexTarget >= targetList.size()) {
				return false;
			}
			while (!isPresentUnderConfiguration(targetList.get(indexTarget), configSource)) {
				indexTarget++;
				if (indexTarget >= targetList.size()) {
					return false;
				}
			}
			DiffNode<DiffLinesLabel> targetNode = targetList.get(indexTarget);
			if (!hasSameLabel(sourceNode.getLabel(), targetNode.getLabel())) {
				return false;
			}
			indexTarget++;
		}
		while (indexTarget != targetList.size()) {
			if (isPresentUnderConfiguration(targetList.get(indexTarget), configSource)) {
				return false;
			}
			indexTarget++;
		}
		return true;
	}

	private static boolean isPresentUnderConfiguration(DiffNode<DiffLinesLabel> diffNode, ConfigureWithFullConfig config) {
		return config.test(diffNode.projection(Time.BEFORE));
	}

	private static DiffNode<DiffLinesLabel> checkNeighbors2(DiffNode<DiffLinesLabel> root,
			DiffNode<DiffLinesLabel> targetNodeInPatch, ConfigureWithFullConfig configSource, Time time, boolean debug)
			throws Exception {
		List<DiffNode<DiffLinesLabel>> orderedChildrenTarget = targetNodeInPatch.getChildOrder(time);
		List<DiffNode<DiffLinesLabel>> orderedChildrenSource = root.getParent(time).getChildOrder(time);
		int indexSource = orderedChildrenSource.indexOf(root);
		List<DiffNode<DiffLinesLabel>> candidates = new ArrayList<>();
		for (DiffNode<DiffLinesLabel> node : orderedChildrenTarget) {
			if (!hasSameLabel(node.getLabel(), root.getLabel())) {
				continue;
			}
			int indexTarget = orderedChildrenTarget.indexOf(node);
			// there are nodes in the source patch which a
			List<DiffNode<DiffLinesLabel>> neighborsBeforeSource = orderedChildrenSource.subList(0, indexSource);
			List<DiffNode<DiffLinesLabel>> neighborsAfterSource = orderedChildrenSource.subList(indexSource + 1,
					orderedChildrenSource.size());
			List<DiffNode<DiffLinesLabel>> neighborsBeforeTarget = orderedChildrenTarget.subList(0, indexTarget);
			List<DiffNode<DiffLinesLabel>> neighborsAfterTarget = orderedChildrenTarget.subList(indexTarget + 1,
					orderedChildrenTarget.size());
			if (isSameList(neighborsBeforeSource, neighborsBeforeTarget, configSource)
					&& isSameList(neighborsAfterSource, neighborsAfterTarget, configSource)) {
				candidates.add(node);
			}
		}
		if (candidates.size() != 1) {
			throw new Exception("Reject: too many nodes to remove: " + candidates.size());
		}
		return candidates.get(0);
	}

	private static int findInsertPosition2(DiffNode<DiffLinesLabel> root, DiffNode<DiffLinesLabel> targetNodeInPatch,
			DiffNode<DiffLinesLabel> targetNodeInPatchView, Time time, boolean debug) throws Exception {
		List<DiffNode<DiffLinesLabel>> orderedChildrenTarget = targetNodeInPatch.getChildOrder(time);
		List<DiffNode<DiffLinesLabel>> orderedChildrenSource = root.getParent(time).getChildOrder(time);
		int indexSource = orderedChildrenSource.indexOf(root);
		int indexTarget = 0;
		int insertPosition = indexSource;
		for (int i = 0; i < orderedChildrenSource.size(); i++) {
			if (i == indexSource) {
				insertPosition = indexTarget;
				continue;
			}
			if (i > indexSource && orderedChildrenSource.get(i).getDiffType() == DiffType.ADD) {
				continue;
			}
			if (!hasSameLabel(orderedChildrenSource.get(i).getLabel(),
					orderedChildrenTarget.get(indexTarget).getLabel())) {
				while (!hasSameLabel(orderedChildrenSource.get(i).getLabel(),
						orderedChildrenTarget.get(indexTarget).getLabel())) {
					indexTarget++;
					if (indexTarget >= orderedChildrenTarget.size()) {
						throw new Exception("could not find insert position");
					}
				}
			}
			indexTarget++;
		}
		return insertPosition;
	}

	private static void applyChanges(DiffType type, VariationDiff<DiffLinesLabel> targetVariantDiffUnchanged,
			VariationDiff<DiffLinesLabel> targetVariantDiffPatched, List<DiffNode<DiffLinesLabel>> subtreeRoots,
			VariationDiffSource source, ConfigureWithFullConfig configSource, boolean debug) throws Exception {

		Time time = (type == DiffType.ADD) ? Time.AFTER : Time.BEFORE;

		for (DiffNode<DiffLinesLabel> root : subtreeRoots) {
			if (debug) {
				DiffNode<DiffLinesLabel> newRoot = DiffNode.createRoot(new DiffLinesLabel());
				newRoot.addChild(root.deepCopy(), time);
				VariationDiff<DiffLinesLabel> subTree = new VariationDiff<DiffLinesLabel>(newRoot, source);
				GameEngine.showAndAwaitAll(Show.diff(subTree));
			}

			List<DiffNode<DiffLinesLabel>> targetNodes = new ArrayList<DiffNode<DiffLinesLabel>>();

			if (root.getParent(time).getDiffType().existsAtTime(time)) {
				final Node presenceCondition = root.getParent(time).getPresenceCondition(time);
				targetNodes = targetVariantDiffUnchanged.computeAllNodesThat(
						node -> node.getPresenceCondition(Time.AFTER).equals(presenceCondition) && node.isAnnotation());
			}

			VariationDiff<DiffLinesLabel> targetVariantDiffPatchedView = DiffView
					.optimized(targetVariantDiffPatched.deepCopy(), configSource);
			targetNodes = targetNodes.stream().filter(targetNode -> checkNeighborsLabels(root,
					targetVariantDiffPatchedView.getNodeWithID(targetNode.getID()), time, debug)).toList();
			targetNodes = targetNodes.stream()
					.map(targetNode -> targetVariantDiffPatched.getNodeWithID(targetNode.getID())).toList();
			if (targetNodes.size() != 1) {
				throw new Exception("too much or too less target nodes after filtering: " + targetNodes.size());
			}

			DiffNode<DiffLinesLabel> targetNodeInPatch = targetNodes.get(0);
			if (debug)
				System.out.println(targetNodeInPatch.toString());
			if (type == DiffType.ADD) {
				if (debug) {
					GameEngine.showAndAwaitAll(Show.tree(targetVariantDiffPatched.project(Time.AFTER)));
				}
				int insertPosition = findInsertPosition2(root, targetNodeInPatch,
						targetVariantDiffPatchedView.getNodeWithID(targetNodeInPatch.getID()), time, debug);
				if (insertPosition < 0) {
					if (debug)
						System.out.println("no matching insert position found");
				}
				if (debug)
					System.out.println("subtree added");
				targetNodeInPatch.insertChild(root.deepCopy(), insertPosition, time);
				if (debug)
					System.out.println(targetNodeInPatch.getChildOrder(time));

			} else if (type == DiffType.REM) {
				DiffNode<DiffLinesLabel> nodesToRem = checkNeighbors2(root, targetNodeInPatch, configSource, time,
						debug);
				if (debug)
					System.out.println("Nodes to remove: " + nodesToRem);

				if (debug)
					System.out.println("subtree removed");
				DiffNode<DiffLinesLabel> newRoot = DiffNode.createRoot(new DiffLinesLabel());
				newRoot.addChild(root.deepCopy(), time);
				VariationDiff<DiffLinesLabel> subTree = new VariationDiff<DiffLinesLabel>(newRoot, source);
				DiffNode<DiffLinesLabel> newRoot2 = DiffNode.createRoot(new DiffLinesLabel());
				newRoot2.addChild(nodesToRem.deepCopy(), time);
				VariationDiff<DiffLinesLabel> subTreeB = new VariationDiff<DiffLinesLabel>(newRoot2,
						targetVariantDiffPatchedView.getSource());
				removeNode(subTreeB.project(time), targetVariantDiffPatched, subTree);

				if (debug)
					System.out.println(targetNodeInPatch.getChildOrder(Time.AFTER));
			}
			if (debug) {
				VariationDiff<DiffLinesLabel> targetVariantDiffPatchedCopy = targetVariantDiffPatched.deepCopy();
//				CutNonEditedSubtrees.genericTransform(targetVariantDiffPatchedCopy);
				GameEngine.showAndAwaitAll(Show.diff(targetVariantDiffPatchedCopy));
			}
		}
	}

	private static boolean areAllChildrenPlannedToRemove(List<VariationTreeNode<DiffLinesLabel>> children,
			List<Integer> idsToRemove) {
		for (VariationTreeNode<DiffLinesLabel> child : children) {
			if (!idsToRemove.contains(child.getID())) {
				return false;
			}
		}
		return true;
	}

	private static void removeNode(VariationTree<DiffLinesLabel> node, VariationDiff<DiffLinesLabel> diffToRemoveFrom,
			VariationDiff<DiffLinesLabel> subtree) throws Exception {
		List<Integer> idsToRemove = new ArrayList<>();
		// Nodes with these labels and pc should be removed
		List<String> labelAndPC = new ArrayList<>();
		subtree.forAll(n -> {
			if (!n.isRoot()) {
				String identifier = calcIdentifier(n);
				labelAndPC.add(identifier);
			}
		});

		node.forAllPostorder(n -> {
			String identifier = calcIdentifier(n);
			// this node should be removed, but it can only be removed if it does not have
			// any other children
			if (labelAndPC.contains(identifier)) {
				// n is leaf or all children should be removed
				if (n.isLeaf() || areAllChildrenPlannedToRemove(n.getChildren(), idsToRemove)) {
					idsToRemove.add(n.getID());
				}
			}
		});
		for (Integer id : idsToRemove) {
			DiffNode<DiffLinesLabel> n = diffToRemoveFrom.getNodeWithID(id);
//			n.diffType = DiffType.REM;
			if (n.getParent(Time.AFTER) != null) {
				n.drop(Time.AFTER);
			}
		}
	}

	private static String calcIdentifier(VariationTreeNode<DiffLinesLabel> n) {
		String identifier = "";
		List<String> l = n.getLabel().getLines();
		if (l != null) {
			for (String line : l) {
				identifier += line;
			}
		}
		List<String> tl = n.getLabel().getTrailingLines();
		if (tl != null) {
			for (String line : tl) {
				identifier += line;
			}
		}
		Node pc = n.getPresenceCondition();
		if (pc != null) {
			identifier += pc.toString();
		}
		return identifier;
	}

	private static String calcIdentifier(DiffNode<DiffLinesLabel> n) {
		String identifier = "";
		List<String> l = n.getLabel().getLines();
		if (l != null) {
			for (String line : l) {
				identifier += line;
			}
		}
		List<String> tl = n.getLabel().getTrailingLines();
		if (tl != null) {
			for (String line : tl) {
				identifier += line;
			}
		}
		Node pc = n.getPresenceCondition(Time.BEFORE);
		if (pc != null) {
			identifier += pc.toString();
		}
		return identifier;
	}

//	private static VariationDiff<DiffLinesLabel> patch(VariationTree<DiffLinesLabel> sourceVariantVersion1,
//			VariationTree<DiffLinesLabel> sourceVariantVersion2, VariationTree<DiffLinesLabel> targetVariant,
//			boolean debug, boolean patchNewFeatures) throws Exception {
//		if (sourceVariantVersion1 == null || sourceVariantVersion2 == null || targetVariant == null) {
//			if (debug)
//				System.out.println("Parsing error");
//			return null;
//		}
//		VariationDiff<DiffLinesLabel> diff = VariationDiff.fromTrees(sourceVariantVersion1, sourceVariantVersion2);
//		return patch(diff, targetVariant, debug, patchNewFeatures);
//	}

	public static void changeType(DiffNode<DiffLinesLabel> node, VariationDiff<DiffLinesLabel> modDiff, DiffType type) {
		if (!node.isLeaf()) {
			node.getAllChildrenStream().forEach(child -> changeType(child, modDiff, type));
		}
		DiffNode<DiffLinesLabel> matchingNode = modDiff.getNodeWithID(node.getID());
		if (matchingNode == null) {
			return;
		}
		Time time = type == DiffType.ADD ? Time.BEFORE : Time.AFTER;
		if (matchingNode.isNon()) {
			matchingNode.diffType = type;
			matchingNode.drop(time);
		} else if (matchingNode.diffType != type) {
			matchingNode.drop();
		}
	}

	public static void resolve(DiffNode<DiffLinesLabel> node, VariationDiff<DiffLinesLabel> modDiff) {
		if (!node.isLeaf()) {
			node.getAllChildrenStream().forEach(child -> resolve(child, modDiff));
		}
		if (node.isNon() && node.getParent(Time.BEFORE) != node.getParent(Time.AFTER)) {
//			System.out.println(node.getLabel());
			DiffNode<DiffLinesLabel> matchingNode = modDiff.getNodeWithID(node.getID());
			// matchingNode can be null because it is a child from two parents
			if (matchingNode == null) {
				return;
			}
			DiffNode<DiffLinesLabel> parentAfter = matchingNode.getParent(Time.AFTER);
			DiffNode<DiffLinesLabel> parentBefore = matchingNode.getParent(Time.BEFORE);
			int indexAfter = parentAfter.getChildOrder(Time.AFTER).indexOf(matchingNode);
			int indexBefore = parentBefore.getChildOrder(Time.BEFORE).indexOf(matchingNode);
			matchingNode.drop();
			DiffNode<DiffLinesLabel> nodeAfter = matchingNode.deepCopy();
			nodeAfter.diffType = DiffType.ADD;

			DiffNode<DiffLinesLabel> newRootAfter = DiffNode.createRoot(new DiffLinesLabel());
			newRootAfter.addChild(nodeAfter.deepCopy(), Time.AFTER);
			VariationDiff<DiffLinesLabel> subTreeAfter = new VariationDiff<DiffLinesLabel>(newRootAfter,
					modDiff.getSource());

			if (!nodeAfter.isLeaf()) {
				changeType(nodeAfter, subTreeAfter, DiffType.ADD);
				// remove all children with difftype REM recursively
				// set recursively difftype ADD for all children which have currently difftype
				// NON
			}

//			GameEngine.showAndAwaitAll(Show.diff(subTreeAfter));

			DiffNode<DiffLinesLabel> nodeBefore = matchingNode.deepCopy();
			nodeBefore.diffType = DiffType.REM;

			DiffNode<DiffLinesLabel> newRootBefore = DiffNode.createRoot(new DiffLinesLabel());
			newRootBefore.addChild(nodeBefore.deepCopy(), Time.BEFORE);
			VariationDiff<DiffLinesLabel> subTreeBefore = new VariationDiff<DiffLinesLabel>(newRootBefore,
					modDiff.getSource());

			if (!nodeBefore.isLeaf()) {
				changeType(nodeBefore, subTreeBefore, DiffType.REM);
			}

//			GameEngine.showAndAwaitAll(Show.diff(subTreeBefore));

			parentAfter.insertChild(subTreeAfter.getRoot().getAllChildren().iterator().next(), indexAfter, Time.AFTER);
			parentBefore.insertChild(subTreeBefore.getRoot().getAllChildren().iterator().next(), indexBefore,
					Time.BEFORE);

//			GameEngine.showAndAwaitAll(Show.diff(modDiff));

		}
	}

	public static VariationDiff<DiffLinesLabel> patch(VariationDiff<DiffLinesLabel> sourcePatch,
			VariationTree<DiffLinesLabel> targetVariant, ConfigureWithFullConfig configSource, ConfigureWithFullConfig configTarget, boolean debug,
			boolean patchNewFeatures) throws Exception {

//		if (!checkForZeroVariantDrift(diff, targetVariant, rho, debug)) {
//			throw new Exception("Variants evolved independently: No Zero Variant Drift");
//		}

		VariationDiff<DiffLinesLabel> optimizedDiff = DiffView.optimized(sourcePatch, configTarget);
//		GameEngine.showAndAwaitAll(Show.diff(optimizedDiff));

		if (debug) {
			GameEngine.showAndAwaitAll(Show.diff(optimizedDiff), Show.tree(optimizedDiff.project(Time.AFTER)));
		}

		VariationDiffSource source = optimizedDiff.getSource();
		VariationDiff<DiffLinesLabel> targetVariantDiffUnchanged = targetVariant.deepCopy()
				.toCompletelyUnchangedVariationDiff();
		VariationDiff<DiffLinesLabel> targetVariantDiffPatched = targetVariant.deepCopy()
				.toCompletelyUnchangedVariationDiff();

		Set<DiffNode<DiffLinesLabel>> removedNodes = new HashSet<DiffNode<DiffLinesLabel>>();
		Set<DiffNode<DiffLinesLabel>> addedNodes = new HashSet<DiffNode<DiffLinesLabel>>();

		// resolve nodes with two parents to two nodes, one added, one removed
		VariationDiff<DiffLinesLabel> diffCopy = optimizedDiff.deepCopy();
		List<DiffNode<DiffLinesLabel>> nodesToResolve = new ArrayList<>();

		optimizedDiff.forAll(node -> {
			if (node.isNon() && node.getParent(Time.BEFORE) != node.getParent(Time.AFTER)) {
				DiffNode<DiffLinesLabel> matchingNode = diffCopy.getNodeWithID(node.getID());
				nodesToResolve.add(matchingNode);
			}
		});

		resolve(optimizedDiff.getRoot(), diffCopy);

		if (debug) {
			GameEngine.showAndAwaitAll(Show.diff(diffCopy, "resolved"));
		}
		optimizedDiff = diffCopy;

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
				source, configSource, debug);

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
				source, configSource, debug);

		if (debug) {
			GameEngine.showAndAwaitAll(Show.diff(sourcePatch), Show.tree(targetVariant), Show.diff(optimizedDiff),
					Show.diff(targetVariantDiffPatched), Show.tree(targetVariantDiffPatched.project(Time.AFTER)));
		}

		if (debug) {
			VariationDiff<DiffLinesLabel> targetVariantDiffPatchedCopy = targetVariantDiffPatched.deepCopy();
			VariationDiff<DiffLinesLabel> optimizedDiffCopy = optimizedDiff.deepCopy();
			CutNonEditedSubtrees.genericTransform(targetVariantDiffPatchedCopy);
			CutNonEditedSubtrees.genericTransform(optimizedDiffCopy);
			GameEngine.showAndAwaitAll(Show.diff(optimizedDiffCopy), Show.diff(targetVariantDiffPatchedCopy));
		}
		return targetVariantDiffPatched;
	}

	public static VariationDiff<DiffLinesLabel> parseVariationDiffFromFiles(String file1, String file2)
			throws IOException, DiffParseException {
		Path examplesDir = Path.of("data", "examples");
		return VariationDiff.fromFiles(examplesDir.resolve(file1), examplesDir.resolve(file2),
				DiffAlgorithm.SupportedAlgorithm.MYERS, VariationDiffParseOptions.Default);
	}

	public static VariationDiff<DiffLinesLabel> parseVariationDiffFromFile(String file)
			throws IOException, DiffParseException {
		Path examplesDir = Path.of("data", "examples");
		return VariationDiff.fromFile(examplesDir.resolve(file), VariationDiffParseOptions.Default);
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

	private static boolean compareIgnoreEmptyLines(String s1, String s2) {
		List<String> lines1 = s1.lines().filter(line -> !line.trim().isEmpty()).collect(Collectors.toList());
		List<String> lines2 = s2.lines().filter(line -> !line.trim().isEmpty()).collect(Collectors.toList());
		return lines1.equals(lines2);
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
				Patching.compareIgnoreEmptyLines(sourceVariantAfterRedToCrossVarFeatures.unparse(),
						targetVariantAfterRedToCrossVarFeatures.unparse()),
				Patching.compareIgnoreEmptyLines(patchedTargetVariantRedToUnchanged.unparse(),
						targetVariantBeforeRedToUnchanged.unparse()));

	}

	public static boolean comparePatchedVariantWithExpectedResult(VariationTree<DiffLinesLabel> patchedVariant,
			VariationTree<DiffLinesLabel> expectedResult) {
		return Patching.isSameAs(patchedVariant.toCompletelyUnchangedVariationDiff(),
				expectedResult.toCompletelyUnchangedVariationDiff());
	}

}
