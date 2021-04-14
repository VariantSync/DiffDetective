package pattern.regex;

import analysis.data.PatternMatchResult;
import diff.data.PatchDiff;
import pattern.EditPattern;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddIfdefRegexPattern extends EditPattern {
    public static final String PATTERN_NAME = "AddIfDefRegex";

    public AddIfdefRegexPattern() {
        this.name = PATTERN_NAME;
    }

    @Override
    public boolean matches(PatchDiff patchDiff, PatternMatchResult patternMatchResult) {
        // initial:
        // ^\+\s*#\s*if.*$\s+(^\+\s*((?!#\s*else).)*$\s+)*^\+\s*#\s*endif.*$

        // just #if:
        // ^\+\s*#\s*if

        String REGEX = "^\\+\\s*#\\s*if.*$\\s+(^\\+\\s*((?!#\\s*else).)*$\\s+)*^\\+\\s*#\\s*endif.*$";
        Pattern regexPattern = Pattern.compile(REGEX, Pattern.MULTILINE);
        Matcher matcher = regexPattern.matcher(patchDiff.getFullDiff());

        int count = 0;
        while(matcher.find()){
            count ++;
        }
        return count == 1;
    }
}
