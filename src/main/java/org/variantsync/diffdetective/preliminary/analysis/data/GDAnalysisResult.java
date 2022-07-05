package org.variantsync.diffdetective.preliminary.analysis.data;

import org.variantsync.diffdetective.preliminary.GitDiff;

import java.util.ArrayList;
import java.util.List;

/**
 * Data class containing the analysis results that an analyzer got from a GitDiff
 *
 * @author Sören Viegener
 */
@Deprecated
public class GDAnalysisResult {
    private final GitDiff gitDiff;
    private final List<CommitDiffAnalysisResult> commitDiffAnalysisResults;

    public GDAnalysisResult(GitDiff gitDiff) {
        this.gitDiff = gitDiff;
        this.commitDiffAnalysisResults = new ArrayList<>();
    }

    public void addCommitDiffAnalysisResult(CommitDiffAnalysisResult commitDiffAnalysisResult) {
        this.commitDiffAnalysisResults.add(commitDiffAnalysisResult);
    }

    public int getPatchAmount() {
        return this.commitDiffAnalysisResults.stream().mapToInt(CommitDiffAnalysisResult::getPatchAmount).sum();
    }

    public GitDiff getGitDiff() {
        return gitDiff;
    }

    public List<CommitDiffAnalysisResult> getCommitDiffAnalysisResults() {
        return commitDiffAnalysisResults;
    }
}
