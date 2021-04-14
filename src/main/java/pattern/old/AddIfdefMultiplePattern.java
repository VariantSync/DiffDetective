package pattern.old;

import analysis.data.PatternMatch;
import analysis.data.PatternMatchResult;
import diff.data.LineDiff;
import diff.data.PatchDiff;
import evaluation.FeatureContext;
import pattern.EditPattern;

public class AddIfdefMultiplePattern extends EditPattern {
    public static final String PATTERN_NAME = "AddIfDefMultiple";

    public AddIfdefMultiplePattern() {
        this.name = PATTERN_NAME;
    }

    @Override
    public boolean matches(PatchDiff patchDiff, PatternMatchResult patternMatchResult) {
        // 2+ times:
        // +if
        // +normal
        // +endif

        int ifCount = 0;
        boolean valid = false;
        boolean normalAdded = false;
        for(LineDiff lineDiff : patchDiff.getShortDiff()){
            switch(lineDiff.getType()){
                case ADD_IF:
                    valid = true;
                    normalAdded = false;
                    break;

                case ADD_ENDIF:
                    if (valid && normalAdded) {
                        ifCount ++;
                    }
                    valid = false;
                    break;

                case ADD_ELSE:
                    valid = false;
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

        return ifCount > 1;
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch patternMatch) {
        return new FeatureContext[0];
    }
}
