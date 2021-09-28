package analysis;

import analysis.data.CommitDiffAnalysisResult;
import analysis.data.GDAnalysisResult;
import analysis.data.PatchDiffAnalysisResult;
import diff.CommitDiff;
import diff.GitDiff;
import diff.PatchDiff;
import diff.difftree.DiffNode;
import pattern.EditPattern;
import pattern.InvalidPatchPattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * (Abstract) class for analyzing a GitDiff.
 *
 * Gets a GitDiff which is analyzed using the given edit patterns.
 */
public abstract class GDAnalyzer<E> {

    final GitDiff gitDiff;
    final EditPattern<E>[] patterns;

    @SuppressWarnings("unchecked")
    public GDAnalyzer(GitDiff gitDiff, EditPattern<DiffNode>[] patterns) {
        this.gitDiff = gitDiff;
        List<EditPattern<DiffNode>> patternList = new ArrayList<>(Arrays.asList(patterns));
        patternList.add(0, new InvalidPatchPattern<>());
        this.patterns = patternList.toArray(new EditPattern[0]);
    }

    public EditPattern<E>[] getPatterns() {
        return patterns;
    }

    /**
     * Analyzes the GitDiff by analyzing each patch in each commit
     * @return The result of the analysis
     */
    public GDAnalysisResult analyze(){
        GDAnalysisResult analysisResult = new GDAnalysisResult(gitDiff);

        for (CommitDiff commitDiff : gitDiff.getCommitDiffs()) {

            CommitDiffAnalysisResult commitResult = new CommitDiffAnalysisResult(commitDiff);

            for (PatchDiff patchDiff : commitDiff.getPatchDiffs()) {

                commitResult.addPatchDiffAnalysisResult(analyzePatch(patchDiff));
            }

            analysisResult.addCommitDiffAnalysisResult(commitResult);
        }

        return analysisResult;
    }

    /**
     * Analyzes a single PatchDiff
     * @param patchDiff The PatchDiff to be analyzed
     * @return The result of the analysis of the PatchDiff
     */
    protected abstract PatchDiffAnalysisResult analyzePatch(PatchDiff patchDiff);
}
