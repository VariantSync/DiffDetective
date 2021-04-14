package analysis.data;

import diff.data.PatchDiff;

import java.util.ArrayList;
import java.util.List;

/**
 * Data class containing the analysis results for a single PatchDiff
 *
 * @author Sören Viegener
 */
public class PatchDiffAnalysisResult {
    private final PatchDiff patchDiff;
    private final List<PatternMatch> patternMatches;

    public PatchDiffAnalysisResult(PatchDiff patchDiff) {
        this.patchDiff = patchDiff;
        this.patternMatches = new ArrayList<>();
    }

    public void addPatternMatches(List<PatternMatch> patternMatches){
        this.patternMatches.addAll(patternMatches);
    }

    public List<PatternMatch> getPatternMatches() {
        return patternMatches;
    }

    public PatchDiff getPatchDiff() {
        return this.patchDiff;
    }
}
