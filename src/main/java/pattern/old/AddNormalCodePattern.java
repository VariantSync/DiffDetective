package pattern.old;

import analysis.data.PatternMatch;
import analysis.data.PatternMatchResult;
import diff.data.LineDiff;
import diff.data.PatchDiff;
import evaluation.FeatureContext;
import pattern.EditPattern;

public class AddNormalCodePattern extends EditPattern {
    public static final String PATTERN_NAME = "AddNormalCode";

    public AddNormalCodePattern() {
        this.name = PATTERN_NAME;
    }

    @Override
    public boolean matches(PatchDiff patchDiff, PatternMatchResult patternMatchResult) {

        // +normal

        int ifCount = 0;

        for(LineDiff lineDiff : patchDiff.getShortDiff()){
            switch(lineDiff.getType()){
                case ADD_IF:
                    ifCount ++;
                    break;

                case ADD_ENDIF:
                    ifCount --;
                    break;

                case ADD_ELSE:
                    break;

                case ADD_NORMAL:
                    if(ifCount == 0){
                        return true;
                    }
                    break;

                case REM_IF:
                    ifCount --;
                    break;

                case REM_ENDIF:
                    ifCount ++;
                    break;

                case REM_ELSE:
                case REM_NORMAL:
                case NORMAL:
                    break;
            }
        }
        return false;

        // NOT CORRECT as it still does not check whether the code addition may be in some wrapped if/else-statement
        // ^(([^+]|\+\s*#\s*endif).*$|$)\s*^\+\s*((?!#\s*if|#\s*elif|#\s*else|#\s*endif).)*$
        /*
        String REGEX = "^(([^+]|\\+\\s*#\\s*endif).*$|$)\\s*^\\+\\s*(" +
                "(?!#\\s*if|#\\s*elif|#\\s*else|#\\s*endif).)*$";
        Pattern regexPattern = Pattern.compile(REGEX, Pattern.MULTILINE);
        Matcher matcher = regexPattern.matcher(fullDiff);
        return matcher.find();

         */
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch patternMatch) {
        return new FeatureContext[0];
    }
}
