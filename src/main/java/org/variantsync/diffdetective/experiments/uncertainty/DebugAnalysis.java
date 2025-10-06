package org.variantsync.diffdetective.experiments.uncertainty;

import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.editclass.EditClass;
import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;
import org.variantsync.diffdetective.metadata.EditClassCount;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.transform.NaiveMovedArtifactDetection;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses.*;

public class DebugAnalysis implements Analysis.Hooks {

    private boolean isInterestingCommit = false;
    private boolean isInterestingClassification = false;
    private boolean commitHadInterestingClassification = false;
    private List<String> interestingFilesList;

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
        String commitMessage = analysis.getCurrentCommit().getFullMessage();
        if (commitMessage.contains("fix") || commitMessage.contains("problem") || commitMessage.contains("issue") || commitMessage.contains("solve") || commitMessage.contains("bug") || commitMessage.contains("error")) { // TODO complete list of keywords
            isInterestingCommit = true;
            interestingFilesList = new ArrayList<>();
            return true;
        }
        return Analysis.Hooks.super.beginCommit(analysis);
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
        if(analysis.getCurrentVariationDiff().anyMatch(node -> {
            if (node.isArtifact()) {
                EditClass editClass = ProposedEditClasses.Instance.match(node);
                if(editClass.equals(Specialization) || editClass.equals(Generalization) || editClass.equals(Reconfiguration)) { // TODO think about classifications
                    return true;
                }
            }
            return false;
        })) {
            interestingFilesList.add(analysis.getCurrentPatch().getFileName(Time.BEFORE));
            return true;
        }
        return false;
    }

    @Override
    public void endPatch(Analysis analysis) throws Exception {
        Analysis.Hooks.super.endPatch(analysis);
    }

    @Override
    public void endCommit(Analysis analysis) throws Exception {
        Analysis.Hooks.super.endCommit(analysis);
        if(isInterestingCommit) {
            InterestingCommit c = new InterestingCommit(analysis.getRepository().getRemoteURI().toString() + "/commit/" +analysis.getCurrentCommit().getName(), interestingFilesList);
            analysis.append(c.getKey(), c);
        }
        isInterestingCommit = false;
    }

    @Override
    public void endBatch(Analysis analysis) throws Exception {
        Analysis.Hooks.super.endBatch(analysis);
    }
}
