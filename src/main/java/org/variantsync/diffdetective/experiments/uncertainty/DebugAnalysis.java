package org.variantsync.diffdetective.experiments.uncertainty;

import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.editclass.EditClass;
import org.variantsync.diffdetective.util.fide.FixTrueFalse;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.transform.NaiveMovedArtifactDetection;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses.*;

public class DebugAnalysis implements Analysis.Hooks {

    private boolean isInterestingCommit = false;
    private boolean isInterestingClassification = false;
    private int uninterestingPatches = 0;
    private int interestingPatches = 0;
    private List<String> interestingFilesList;
    private static final Pattern bugStringPattern = Pattern.compile("(fix)|(problem)|(issue)|(solve)|(error)|((?<!e)bug(?!=b:))");

    public static boolean isIfFalse(DiffNode<?> d) {
        // There might be other edge cases as well.
        return (d.isIf() || d.isElif()) && FixTrueFalse.isFalse(d.getFormula());
    }

    @Override
    public void initializeResults(Analysis analysis) {
        Analysis.Hooks.super.initializeResults(analysis);
    }

    @Override
    public void beginBatch(Analysis analysis) throws Exception {
        Analysis.Hooks.super.beginBatch(analysis);
    }

    @Override
    public boolean beginCommit(Analysis analysis) throws Exception {
        if (bugStringPattern.matcher(analysis.getCurrentCommit().getFullMessage().toLowerCase()).find()) { // TODO complete list of keywords
            isInterestingCommit = true;
            interestingFilesList = new ArrayList<>();
            return true;
        }
        return false;
    }

    @Override
    public void onFailedCommit(Analysis analysis) throws Exception {
        Analysis.Hooks.super.onFailedCommit(analysis);
    }

    @Override
    public void onFailedParse(Analysis analysis) throws Exception {
        Analysis.Hooks.super.onFailedParse(analysis);
    }

    @Override
    public boolean onParsedCommit(Analysis analysis) throws Exception {
        return Analysis.Hooks.super.onParsedCommit(analysis);
    }

    @Override
    public boolean beginPatch(Analysis analysis) throws Exception {
        return Analysis.Hooks.super.beginPatch(analysis);
    }

    @Override
    public boolean analyzeVariationDiff(Analysis analysis) throws Exception {
        if(!isInterestingCommit) {
            return false;
        }
        NaiveMovedArtifactDetection<DiffLinesLabel> detectTwins = new NaiveMovedArtifactDetection<>(); //TODO investigate changed trees
        detectTwins.transform(analysis.getCurrentVariationDiff());
        int newInterestingLines = analysis.getCurrentVariationDiff().count(node -> (node.isArtifact() && isInterestingClassification(node) && !isCommented(node)));
        interestingPatches += newInterestingLines; //TODO 1 Patch = 1 ganzer Variation diff. rename (interestingNode)
        uninterestingPatches += analysis.getCurrentVariationDiff().count(node -> (node.isArtifact() && (!isInterestingClassification(node) || isCommented(node)))); //TODO wollen wir hier überhaupt die isArtifact Überprüfung?

        if(newInterestingLines > 0){
            isInterestingClassification = true;
            interestingFilesList.add(analysis.getCurrentPatch().getFileName(Time.BEFORE));
            return true;
        }
        return false;
    }

    private <T extends Label> boolean isCommented(DiffNode<T> diffNode) {
        DiffNode<T> childOfChangedNode = getChangedNode(diffNode);
        if(childOfChangedNode == null) {
            return false;
        }
        DiffNode<T> parentBefore = childOfChangedNode.getParent(Time.BEFORE);
        DiffNode<T> parentAfter = childOfChangedNode.getParent(Time.AFTER);
        boolean beforeIsFalse = parentBefore != null && isIfFalse(parentBefore);
        boolean afterIsFalse = parentAfter != null && isIfFalse(parentAfter);

        return beforeIsFalse || afterIsFalse;
//        return false;
    }

    /**
     *
     * @param diffNode
     * @return the first parent node (in depth) that was changed of a given node or null if no parent was changed up to the root
     */
    private <T extends Label> DiffNode<T> getChangedNode(DiffNode<T> diffNode) { //TODO change naming of method
        if (diffNode.isRoot()) {
            return null;
        }
        DiffNode<T> parentBefore = diffNode.getParent(Time.BEFORE);
        DiffNode<T> parentAfter = diffNode.getParent(Time.AFTER);

        if (parentBefore == null || parentAfter == null) {
            return diffNode;
        }
        if (!parentBefore.equals(parentAfter)) {
            return diffNode;
        } else {
            return getChangedNode(parentAfter);
        }
    }

    private boolean isInterestingClassification(DiffNode<DiffLinesLabel> diffNode) {
        EditClass match = Instance.match(diffNode);
        return match.equals(Specialization) ||
            match.equals(Generalization) ||
            match.equals(Reconfiguration);
    }

    @Override
    public void endPatch(Analysis analysis) throws Exception {
        Analysis.Hooks.super.endPatch(analysis);
    }

    @Override
    public void endCommit(Analysis analysis) throws Exception {
        Analysis.Hooks.super.endCommit(analysis);
        if(isInterestingClassification) {
            interestingFilesList.add("Number uninteresting Patches: " + uninterestingPatches);
            interestingFilesList.add("Number interesting Patches: " + interestingPatches);
            interestingFilesList.add("Year: " + analysis.getCurrentCommit().getAuthorIdent().getWhenAsInstant().atZone(ZoneId.systemDefault()).getYear());
            InterestingCommit c = new InterestingCommit(analysis.getRepository().getRemoteURI().toString() + "/commit/" +analysis.getCurrentCommit().getName(), interestingFilesList);
            analysis.append(c.getKey(), c);
            isInterestingCommit = false;
            isInterestingClassification = false;
        }
        uninterestingPatches = 0;
        interestingPatches = 0;
    }

    @Override
    public void endBatch(Analysis analysis) throws Exception {
        Analysis.Hooks.super.endBatch(analysis);
    }
}
