package org.variantsync.diffdetective.variation.tree.view.relevance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.tree.VariationNode;

import com.github.gumtreediff.actions.Diff;

public record Unchanged(VariationDiff<DiffLinesLabel> diff, Time time) implements Relevance {

	@Override
	public boolean test(VariationNode<?, ?> t) {
		List<DiffNode<DiffLinesLabel>> tInDiffMatches = diff.computeAllNodesThat(diffNode -> isSameNode(t, diffNode)
				&& (time == Time.AFTER ? !diffNode.isRem() : !diffNode.isAdd()));
		if (tInDiffMatches.isEmpty()) {
			return true;
		}
		DiffNode<DiffLinesLabel> tInDiff = tInDiffMatches.get(0);
		if (tInDiffMatches.size() > 1) {
			Map<DiffNode<DiffLinesLabel>, Integer> map = new HashMap<>();
			for (DiffNode<DiffLinesLabel> diffNode : tInDiffMatches) {
				Integer lineNumberDiffNode = diffNode.getLinesAtTime(time).fromInclusive();
				map.put(diffNode, Math.abs(t.getLineRange().fromInclusive() - lineNumberDiffNode));
			}
			List<Entry<DiffNode<DiffLinesLabel>, Integer>> list = new ArrayList<>(map.entrySet());
			list.sort(Entry.comparingByValue());
			tInDiff = list.get(0).getKey();
		}
		return tInDiff.isNon() && tInDiff.beforePathEqualsAfterPath();
	}

	@Override
	public String getFunctionName() {
		// TODO Auto-generated method stub
		return "unchanged";
	}

	@Override
	public String parametersToString() {
		// TODO Auto-generated method stub
		return diff.toString();
	}

	public static boolean isSameNode(VariationNode<?, ?> a, DiffNode<?> b) {
		return a.getNodeType().equals(b.getNodeType()) && Objects.equals(a.getFormula(), b.getFormula())
				&& Label.observablyEqual(a.getLabel(), b.getLabel());
	}
}
