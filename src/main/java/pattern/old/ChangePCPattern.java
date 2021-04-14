package pattern.old;

import analysis.data.PatternMatch;
import analysis.data.PatternMatchResult;
import diff.data.PatchDiff;
import evaluation.FeatureContext;
import pattern.EditPattern;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangePCPattern extends EditPattern {
    public static final String PATTERN_NAME = "ChangePC";

    public ChangePCPattern() {
        this.name = PATTERN_NAME;
    }

    @Override
    public boolean matches(PatchDiff patchDiff, PatternMatchResult patternMatchResult) {
        // TODO: something matches that should probably not match. Idk what tho

        // -if
        // +if

        // ^\-if$\s+^\+if$
        String REGEX = "^\\-if$\\s+^\\+if$";
        Pattern regexPattern = Pattern.compile(REGEX, Pattern.MULTILINE);
        Matcher matcher = regexPattern.matcher(patchDiff.getShortDiffString(true));
        return matcher.find();

    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch patternMatch) {
        return new FeatureContext[0];
    }
}
