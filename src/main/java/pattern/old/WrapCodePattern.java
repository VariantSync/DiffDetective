package pattern.old;

import analysis.data.PatternMatch;
import analysis.data.PatternMatchResult;
import diff.data.LineDiff;
import diff.data.PatchDiff;
import evaluation.FeatureContext;
import pattern.EditPattern;

public class WrapCodePattern extends EditPattern {
    public static final String PATTERN_NAME = "WrapCode";

    public WrapCodePattern() {
        this.name = PATTERN_NAME;
    }

    @Override
    public boolean matches(PatchDiff patchDiff, PatternMatchResult patternMatchResult) {

        // +if
        // .
        // +endif

        boolean valid = false;
        boolean codeWrapped = false;
        for(LineDiff lineDiff : patchDiff.getShortDiff()){
            switch(lineDiff.getType()){
                case ADD_IF:
                    valid = true;
                    codeWrapped = false;
                    break;

                case ADD_ENDIF:
                    if (valid && codeWrapped) {
                        return true;
                    }
                    valid = false;
                    break;

                case ADD_ELSE:
                    valid = false;
                    break;

                case ADD_NORMAL:
                    break;

                case REM_IF:
                case REM_ENDIF:
                case REM_ELSE:
                case REM_NORMAL:
                    valid = false;
                    break;

                case NORMAL:
                    codeWrapped = true;
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
