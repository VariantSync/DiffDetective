package pattern.old;

import analysis.data.PatternMatch;
import analysis.data.PatternMatchResult;
import diff.data.LineDiff;
import diff.data.PatchDiff;
import evaluation.FeatureContext;
import pattern.EditPattern;

public class AddAnnotationPattern extends EditPattern {
    public static final String PATTERN_NAME = "AddAnnotation";

    public AddAnnotationPattern() {
        this.name = PATTERN_NAME;
    }

    @Override
    public boolean matches(PatchDiff patchDiff, PatternMatchResult patternMatchResult) {

        // TODO

        // +if
        // +normal
        // +endif

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
                        return true;
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
        return false;

        /*
        String[] lines = patchDiff.getFullDiff().split("(\\r\\n|\\r|\\n)");


        String addIfRegex = "^\\+\\s*#\\s*if.*$";
        String addEndifRegex = "^\\+\\s*#\\s*endif.*$";
        String addElseRegex = "^\\+\\s*#\\s*(else|elif).*$";

        String remIfRegex = "^-\\s*#\\s*if.*$";
        String remEndifRegex = "^-\\s*#\\s*endif.*$";
        String remElseRegex = "^-\\s*#\\s*(else|elif).*$";


        int ifCount = 0;
        int addRem = 0;
        for(String line : lines){
            if(line.matches(addIfRegex)){
                ifCount ++;
                addRem ++;
            }else if(line.matches(addEndifRegex)) {
                ifCount--;
                addRem ++;
            }else if(line.matches(remIfRegex)) {
                ifCount--;
                addRem --;
            }else if(line.matches(remEndifRegex)) {
                ifCount ++;
                addRem --;
            }
        }

        return ifCount != 0 && addRem > 0;

         */
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch patternMatch) {
        return new FeatureContext[0];
    }
}
