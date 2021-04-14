package analysis.old;

import analysis.GDAnalyzer;
import analysis.data.*;
import diff.data.GitDiff;
import diff.data.PatchDiff;
import org.pmw.tinylog.Logger;
import pattern.*;
import pattern.old.*;

/**
 * Class for analyzing a GitDiff.
 * <p>
 * This consists of filtering the PatchDiffs using the given DiffFilter and then matching
 * different EditPatterns.
 *
 * @author Sören Viegener
 */
public class BasicGDAnalyzer extends GDAnalyzer {
    public static final EditPattern[] DEFAULT_PATTERNS = new EditPattern[]{
            new AddIfdefPattern(),
            new AddIfdefMultiplePattern(),
            new AddIfdefElsePattern(),
            new AddIfdefWrapElsePattern(),
            new AddIfdefWrapThenPattern(),
            new AddNormalCodePattern(),
            new AddAnnotationPattern(),
            new RemNormalCodePattern(),
            new RemIfdefPattern(),
            new RemAnnotationPattern(),
            new WrapCodePattern(),
            new UnwrapCodePattern(),
            new ChangePCPattern(),
            new MoveElsePattern(),
    };

    public BasicGDAnalyzer(GitDiff gitDiff) {
        this(gitDiff, DEFAULT_PATTERNS);
    }

    public BasicGDAnalyzer(GitDiff gitDiff, EditPattern[] patterns) {
        super(gitDiff, patterns);
    }

    @Override
    protected PatchDiffAnalysisResult analyzePatch(PatchDiff patchDiff) {
        PatchDiffAnalysisResult patchResult = new PatchDiffAnalysisResult(patchDiff);

        for (EditPattern pattern : patterns) {
            try {
                PatternMatchResult patternMatchResult = new PatternMatchResult();
                if (pattern.matches(patchDiff, patternMatchResult)) {
                    if (!patternMatchResult.hasMatch()) {
                        patternMatchResult.addPatternMatch(new PatternMatch(pattern));
                    }

                    patchResult.addPatternMatches(patternMatchResult.getMatches());
                }
            } catch (StackOverflowError e) {
                Logger.warn("Matching for pattern {} caused StackOverflow in ({}, {})",
                        pattern.getName(),
                        patchDiff.getCommitDiff().getAbbreviatedCommitHash(),
                        patchDiff.getFileName());
            }
        }
        return patchResult;
    }
}
