package org.variantsync.diffdetective.experiments.uncertainty;

import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.editclass.EditClass;
import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;
import org.variantsync.diffdetective.metadata.EditClassCount;
import org.variantsync.diffdetective.show.Show;
import org.variantsync.diffdetective.show.engine.GameEngine;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.transform.NaiveMovedArtifactDetection;
import org.variantsync.diffdetective.variation.tree.VariationTree;

import static org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses.*;

public class DebugAnalysis implements Analysis.Hooks {

    @Override
    public void initializeResults(Analysis analysis) {
        analysis.append(EditClassCount.KEY, new EditClassCount(ProposedEditClasses.Instance));
        Analysis.Hooks.super.initializeResults(analysis);
    }

    @Override
    public void beginBatch(Analysis analysis) throws Exception {
        Analysis.Hooks.super.beginBatch(analysis);
    }

    @Override
    public boolean beginCommit(Analysis analysis) throws Exception {
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
//        Show.diff(analysis.getCurrentVariationDiff(), analysis.getCurrentCommit().getShortMessage());
//        VariationDiff<DiffLinesLabel> d = analysis.getCurrentVariationDiff();
//        VariationTree<DiffLinesLabel> b = d.project(Time.BEFORE);
//        VariationTree<DiffLinesLabel> a = d.project(Time.AFTER);
//        GameEngine.showAndAwaitAll(Show.diff(d), Show.tree(b), Show.tree(a));
        NaiveMovedArtifactDetection<DiffLinesLabel> detectTwins = new NaiveMovedArtifactDetection<>(); //TODO Bäume die geändert wurden angucken
        detectTwins.transform(analysis.getCurrentVariationDiff());
        if(analysis.getCurrentVariationDiff().anyMatch(node -> {
            if (node.isArtifact()) {
                EditClass editClass = ProposedEditClasses.Instance.match(node);
                if(editClass.equals(Specialization) || editClass.equals(Generalization) || editClass.equals(Reconfiguration)) {
                    return true;
                }
            }
            return false;
        })) {
            if (analysis.getCurrentCommit().getFullMessage().contains("fix")) {
                System.out.println(analysis.getRepository().getRemoteURI() + "/commit/" + analysis.getCurrentCommit().getName());
            }
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
//        Logger.info(analysis.getCurrentCommit().getShortMessage());
    }

    @Override
    public void endBatch(Analysis analysis) throws Exception {
        Analysis.Hooks.super.endBatch(analysis);
    }
}
