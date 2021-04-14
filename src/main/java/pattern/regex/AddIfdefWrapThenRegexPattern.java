package pattern.regex;

import analysis.data.PatternMatchResult;
import diff.data.PatchDiff;
import pattern.EditPattern;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddIfdefWrapThenRegexPattern extends EditPattern {
    public static final String PATTERN_NAME = "AddIfDefWrapThenRegex";

    public AddIfdefWrapThenRegexPattern() {
        this.name = PATTERN_NAME;
    }

    @Override
    public boolean matches(PatchDiff patchDiff, PatternMatchResult patternMatchResult) {
        // ^\+\s*#\s*if.*$\s+(^.*$\s+)*(^[^\+]*$\s+)(^.*$\s+)*^\+\s*#\s*else.*$\s+(^\+.*$\s+)*^\+\s*#\s*endif.*$
        String REGEX = "^\\+\\s*#\\s*if.*$\\s+(^.*$\\s+)*(^[^\\+]*$\\s+)(^.*$\\s+)" +
                "*^\\+\\s*#\\s*else.*$\\s+(^\\+.*$\\s+)*^\\+\\s*#\\s*endif.*$";
        Pattern regexPattern = Pattern.compile(REGEX, Pattern.MULTILINE);
        Matcher matcher = regexPattern.matcher(patchDiff.getFullDiff());
        return matcher.find();
    }
}
