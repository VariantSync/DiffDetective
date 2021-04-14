package pattern.old;

import analysis.data.PatternMatch;
import analysis.data.PatternMatchResult;
import diff.data.LineDiff;
import diff.data.PatchDiff;
import evaluation.FeatureContext;
import pattern.EditPattern;

public class RemIfdefPattern extends EditPattern {
    public static final String PATTERN_NAME = "RemIfDef";

    public RemIfdefPattern() {
        this.name = PATTERN_NAME;
    }

    @Override
    public boolean matches(PatchDiff patchDiff, PatternMatchResult patternMatchResult) {
        // -if
        // -normal
        // -endif

        // 495
        boolean valid = false;
        boolean normalRemoved = false;
        for(LineDiff lineDiff : patchDiff.getShortDiff()){
            switch(lineDiff.getType()){
                case ADD_IF:
                case ADD_ENDIF:
                case ADD_ELSE:
                    break;

                case ADD_NORMAL:
                    valid = false;
                    break;

                case REM_IF:
                    valid = true;
                    normalRemoved = false;
                    break;

                case REM_ENDIF:
                    if (valid && normalRemoved) {
                        return true;
                    }
                    valid = false;
                    break;

                case REM_ELSE:
                    break;

                case REM_NORMAL:
                    normalRemoved = true;
                    break;

                case NORMAL:
                    break;
            }
        }
        return false;
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch patternMatch) {
        return new FeatureContext[0];
    }
}
