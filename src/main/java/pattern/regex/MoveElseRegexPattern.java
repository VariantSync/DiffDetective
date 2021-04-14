package pattern.regex;

import analysis.data.PatternMatchResult;
import diff.data.PatchDiff;
import pattern.EditPattern;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MoveElseRegexPattern extends EditPattern {
    public static final String PATTERN_NAME = "MoveElseRegex";

    public MoveElseRegexPattern() {
        this.name = PATTERN_NAME;
    }

    @Override
    public boolean matches(PatchDiff patchDiff, PatternMatchResult patternMatchResult) {

        String shortDiff = patchDiff.getShortDiffString(true);

        // ^-else\s+^\.\s+^\+else
        String REGEX = "^-else\\s+^\\.\\s+^\\+else";
        Pattern regexPattern = Pattern.compile(REGEX, Pattern.MULTILINE);
        Matcher matcher = regexPattern.matcher(shortDiff);

        // ^\+else\s+^\.\s+^-else
        REGEX = "^\\+else\\s+^\\.\\s+^-else";
        regexPattern = Pattern.compile(REGEX, Pattern.MULTILINE);
        Matcher matcher2 = regexPattern.matcher(shortDiff);
        return matcher.find() || matcher2.find();


        /*
        // ^-\s*#\s*(else|elif).*$\s+(^[^+-].*\S.*$\s+)+^\+\s*#\s*\1.*$
        String REGEX = "^-\\s*#\\s*(else|elif).*$\\s+(^[^+-].*\\S.*$\\s+)+^\\+\\s*#\\s*\\1.*$";
        Pattern regexPattern = Pattern.compile(REGEX, Pattern.MULTILINE);
        Matcher matcher = regexPattern.matcher(patchDiff.getFullDiff());

        // ^\+\s*#\s*(else|elif).*$\s+(^[^+-].*\S.*$\s+)+^-\s*#\s*\1.*$
        REGEX = "^\\+\\s*#\\s*(else|elif).*$\\s+(^[^+-].*\\S.*$\\s+)+^-\\s*#\\s*\\1.*$";
        regexPattern = Pattern.compile(REGEX, Pattern.MULTILINE);
        Matcher matcher2 = regexPattern.matcher(patchDiff.getFullDiff());
        return matcher.find() || matcher2.find();

         */
    }
}
