package pattern.old;

import analysis.data.PatternMatch;
import analysis.data.PatternMatchResult;
import diff.data.LineDiff;
import diff.data.PatchDiff;
import evaluation.FeatureContext;
import pattern.EditPattern;

public class AddIfdefElsePattern extends EditPattern {
    public static final String PATTERN_NAME = "AddIfDefElse";

    public AddIfdefElsePattern() {
        this.name = PATTERN_NAME;
    }

    @Override
    public boolean matches(PatchDiff patchDiff, PatternMatchResult patternMatchResult) {

        // +if
        // +normal
        // +else
        // +normal
        // +endif

        boolean elseAdded = false;
        boolean normalAdded = false;
        boolean valid = false;

        for(LineDiff lineDiff : patchDiff.getShortDiff()){
            switch(lineDiff.getType()){
                case ADD_IF:
                    valid = true;
                    elseAdded = false;
                    normalAdded = false;
                    break;

                case ADD_ENDIF:
                    if (valid && elseAdded && normalAdded) {
                        return true;
                    }
                    valid = false;
                    break;

                case ADD_ELSE:
                    if(normalAdded) {
                        elseAdded = true;
                        normalAdded = false;
                    }
                    break;

                case ADD_NORMAL:
                    normalAdded = true;
                    break;

                case REM_IF:
                case REM_ENDIF:
                case REM_ELSE:
                case REM_NORMAL:
                    valid = false;
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
