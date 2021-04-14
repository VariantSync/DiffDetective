package pattern.old;

import analysis.data.PatternMatch;
import analysis.data.PatternMatchResult;
import diff.data.LineDiff;
import diff.data.PatchDiff;
import evaluation.FeatureContext;
import org.prop4j.Node;
import pattern.EditPattern;

public class AddIfdefPattern extends EditPattern {
    public static final String PATTERN_NAME = "AddIfDef";

    public AddIfdefPattern() {
        this.name = PATTERN_NAME;
    }

    @Override
    public boolean matches(PatchDiff patchDiff, PatternMatchResult patternMatchResult) {
        // +if
        // +normal
        // +endif


        boolean valid = false;
        int patternStart = -1;
        boolean normalAdded = false;
        int currentLine = 0;
        Node presenceCondition = null;
        for(LineDiff lineDiff : patchDiff.getShortDiff()){
            switch(lineDiff.getType()){
                case ADD_IF:
                    valid = true;
                    patternStart = currentLine;
                    //TODO
                    presenceCondition = lineDiff.getPresenceCondition();
                    normalAdded = false;
                    break;

                case ADD_ENDIF:
                    if (valid && normalAdded) {
                        patternMatchResult.addPatternMatch(new PatternMatch(this, patternStart, currentLine, presenceCondition));
                    }
                    valid = false;
                    break;

                case ADD_NORMAL:
                    normalAdded = true;
                    break;

                case ADD_ELSE:
                case ADD_ELIF:
                case REM_IF:
                case REM_ENDIF:
                case REM_ELSE:
                case REM_NORMAL:
                    valid = false;
                    break;

                case NORMAL:
                    break;
            }
            currentLine ++;
        }
        return patternMatchResult.hasMatch();
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch patternMatch) {
        return new FeatureContext[0];
    }
}
