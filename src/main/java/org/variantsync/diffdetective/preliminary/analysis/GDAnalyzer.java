package org.variantsync.diffdetective.preliminary.analysis;

import org.variantsync.diffdetective.diff.git.CommitDiff;
import org.variantsync.diffdetective.diff.git.PatchDiff;
import org.variantsync.diffdetective.preliminary.pattern.Pattern;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.preliminary.GitDiff;
import org.variantsync.diffdetective.preliminary.analysis.data.CommitDiffAnalysisResult;
import org.variantsync.diffdetective.preliminary.analysis.data.GDAnalysisResult;
import org.variantsync.diffdetective.preliminary.analysis.data.PatchDiffAnalysisResult;
import org.variantsync.diffdetective.preliminary.pattern.FeatureContextReverseEngineering;

import java.util.List;
import java.util.stream.Collectors;

/**
 * (Abstract) class for analyzing a GitDiff.
 *
 * Gets a GitDiff which is analyzed using the given edit patterns.
 */
@Deprecated
public abstract class GDAnalyzer {

    final GitDiff gitDiff;
    final List<FeatureContextReverseEngineering<DiffNode<?>>> patterns;

    public GDAnalyzer(GitDiff gitDiff, List<FeatureContextReverseEngineering<DiffNode<?>>> patterns) {
        this.gitDiff = gitDiff;
//        List<Pattern<DiffNode>> patternList = new ArrayList<>(Arrays.asList(patterns));
//        patternList.add(0, new InvalidPatchPattern<>());
        this.patterns = patterns;
    }

    public List<FeatureContextReverseEngineering<DiffNode<?>>> getReverseEngineerings() {
        return patterns;
    }

    public List<Pattern<DiffNode<?>>> getPatterns() {
        return patterns.stream().map(FeatureContextReverseEngineering::getPattern).collect(Collectors.toList());
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
