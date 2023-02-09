package org.variantsync.diffdetective.validation;

import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.analysis.CommitHistoryAnalysisResult;
import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;

/**
 * Task for performing the ESEC/FSE'22 validation on a set of commits from a given repository.
 * @author Paul Bittner
 */
public class EditClassValidationAnalysis implements Analysis.Hooks<CommitHistoryAnalysisResult> {
    @Override
    public boolean analyzeDiffTree(Analysis<CommitHistoryAnalysisResult> analysis) throws Exception {
        analysis.getDiffTree().forAll(node -> {
            if (node.isArtifact()) {
                analysis.getResult().editClassCounts.reportOccurrenceFor(
                    ProposedEditClasses.Instance.match(node),
                    analysis.getCommitDiff()
                );
            }
        });

        return true;
    }
}
