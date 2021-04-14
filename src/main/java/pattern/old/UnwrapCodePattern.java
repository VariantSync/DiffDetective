package pattern.old;

import analysis.data.PatternMatch;
import analysis.data.PatternMatchResult;
import diff.data.LineDiff;
import diff.data.PatchDiff;
import evaluation.FeatureContext;
import pattern.EditPattern;

public class UnwrapCodePattern extends EditPattern {
    public static final String PATTERN_NAME = "UnwrapCode";

    public UnwrapCodePattern() {
        this.name = PATTERN_NAME;
    }

    @Override
    public boolean matches(PatchDiff patchDiff, PatternMatchResult patternMatchResult) {

        // -if
        // .
        // -endif

        boolean valid = false;
        boolean codeUnwrapped = false;
        for(LineDiff lineDiff : patchDiff.getShortDiff()){
            switch(lineDiff.getType()){
                case ADD_IF:
                case ADD_ENDIF:
                case ADD_ELSE:
                case ADD_NORMAL:
                    valid = false;
                    break;

                case REM_IF:
                    valid = true;
                    codeUnwrapped = false;
                    break;

                case REM_ENDIF:
                    if (valid && codeUnwrapped) {
                        return true;
                    }
                    valid = false;
                    break;

                case REM_ELSE:
                    valid = false;
                    break;

                case REM_NORMAL:
                    break;

                case NORMAL:
                    codeUnwrapped = true;
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
