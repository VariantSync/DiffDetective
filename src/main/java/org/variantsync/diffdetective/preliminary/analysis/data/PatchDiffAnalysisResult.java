package org.variantsync.diffdetective.preliminary.analysis.data;

import org.variantsync.diffdetective.diff.PatchDiff;
import org.variantsync.diffdetective.diff.difftree.DiffNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Data class containing the analysis results for a single PatchDiff
 *
 * @author Sören Viegener
 */
@Deprecated
public class PatchDiffAnalysisResult {
    private final PatchDiff patchDiff;
    private final List<PatternMatch<DiffNode>> patternMatches;

    public PatchDiffAnalysisResult(PatchDiff patchDiff) {
        this.patchDiff = patchDiff;
        this.patternMatches = new ArrayList<>();
    }

    public void addPatternMatches(List<PatternMatch<DiffNode>> patternMatches){
        this.patternMatches.addAll(patternMatches);
    }

    public List<PatternMatch<DiffNode>> getPatternMatches() {
        return patternMatches;
    }

    public PatchDiff getPatchDiff() {
        return this.patchDiff;
    }
}
