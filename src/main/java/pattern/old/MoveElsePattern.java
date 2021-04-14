package pattern.old;

import analysis.data.PatternMatch;
import analysis.data.PatternMatchResult;
import diff.data.PatchDiff;
import evaluation.FeatureContext;
import org.apache.maven.shared.utils.StringUtils;
import pattern.EditPattern;

public class MoveElsePattern extends EditPattern {
    public static final String PATTERN_NAME = "MoveElse";

    public MoveElsePattern() {
        this.name = PATTERN_NAME;
    }

    @Override
    public boolean matches(PatchDiff patchDiff, PatternMatchResult patternMatchResult) {
        // TODO change to shortDiff match

        String[] lines = patchDiff.getFullDiff().split("(\\r\\n|\\r|\\n)");


        String addIfRegex = "^\\+\\s*#\\s*if.*$";
        String addEndifRegex = "^\\+\\s*#\\s*endif.*$";
        String addElseRegex = "^\\+\\s*#\\s*else.*$";
        String addElifRegex = "^\\+\\s*#\\s*elif.*$";

        String remIfRegex = "^-\\s*#\\s*if.*$";
        String remEndifRegex = "^-\\s*#\\s*endif.*$";
        String remElseRegex = "^-\\s*#\\s*else.*$";
        String remElifRegex = "^-\\s*#\\s*elif.*$";


        int elseFound = 0;
        int elifFound = 0;
        boolean nonEmptyLine = false;
        for(String line : lines){
            if(line.matches(addElseRegex)){
                if(elseFound < 0 && nonEmptyLine){
                    return true;
                }
                elseFound ++;
                nonEmptyLine = false;
            }else if(line.matches(remElseRegex)) {
                if (elseFound > 0 && nonEmptyLine) {
                    return true;
                }
                elseFound--;
                nonEmptyLine = false;
            }else if(line.matches(addElifRegex)){
                if(elifFound < 0 && nonEmptyLine){
                    return true;
                }
                elifFound ++;
                nonEmptyLine = false;
            }else if(line.matches(remElifRegex)){
                if(elifFound > 0 && nonEmptyLine){
                    return true;
                }
                elifFound --;
                nonEmptyLine = false;
            }else if(line.startsWith("+") || line.startsWith("-")){
                elseFound = 0;
                elifFound = 0;
            }else if(!StringUtils.isBlank(line)){
                nonEmptyLine = true;
            }
        }

        return false;
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch patternMatch) {
        return new FeatureContext[0];
    }
}
