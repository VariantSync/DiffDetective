package analysis.data;

import diff.data.CommitDiff;

import java.util.ArrayList;
import java.util.List;

/**
 * Data class containing the analysis results for a single CommitDiff
 *
 * @author Sören Viegener
 */
public class CommitDiffAnalysisResult {
    private final CommitDiff commitDiff;
    private final List<PatchDiffAnalysisResult> patchDiffAnalysisResults;

    public CommitDiffAnalysisResult(CommitDiff commitDiff) {
        this.commitDiff = commitDiff;
        patchDiffAnalysisResults = new ArrayList<>();
    }

    public void addPatchDiffAnalysisResult(PatchDiffAnalysisResult patchDiffAnalysisResult){
        this.patchDiffAnalysisResults.add(patchDiffAnalysisResult);
    }

    public List<PatchDiffAnalysisResult> getPatchDiffAnalysisResults() {
        return patchDiffAnalysisResults;
    }

    public CommitDiff getCommitDiff(){
        return this.commitDiff;
    }

    public int getPatchAmount(){
        return patchDiffAnalysisResults.size();
    }
}
