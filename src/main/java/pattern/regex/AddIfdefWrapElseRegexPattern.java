package pattern.regex;

import analysis.data.PatternMatchResult;
import diff.data.PatchDiff;
import pattern.EditPattern;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddIfdefWrapElseRegexPattern extends EditPattern {
    public static final String PATTERN_NAME = "AddIfDefWrapElseRegex";

    public AddIfdefWrapElseRegexPattern() {
        this.name = PATTERN_NAME;
    }

    @Override
    public boolean matches(PatchDiff patchDiff, PatternMatchResult patternMatchResult) {
        // ^\+\s*#\s*if.*$\s+(^\+.*$\s+)*^\+\s*#\s*else.*$\s+(^.*$\s+)*(^[^\+]*$\s+)(^.*$\s+)*^\+\s*#\s*endif.*$
        String REGEX = "^\\+\\s*#\\s*if.*$\\s+(^\\+.*$\\s+)*^\\+\\s*#\\s*else.*$\\s+(^.*$\\s+)*" +
                "(^[^\\+]*$\\s+)(^.*$\\s+)*^\\+\\s*#\\s*endif.*$";
        Pattern regexPattern = Pattern.compile(REGEX, Pattern.MULTILINE);
        Matcher matcher = regexPattern.matcher(patchDiff.getFullDiff());
        return matcher.find();
    }
}
