package pattern.regex;

import analysis.data.PatternMatch;
import analysis.data.PatternMatchResult;
import diff.data.PatchDiff;
import pattern.EditPattern;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class RegexPattern extends EditPattern {
    String[] fullRegex;
    String[] shortRegex;

    public RegexPattern() {
        this.fullRegex = new String[0];
        this.shortRegex = new String[0];
    }

    @Override
    public boolean matches(PatchDiff patchDiff, PatternMatchResult patternMatchResult) {
        for(String regex : fullRegex){
            Pattern regexPattern = Pattern.compile(regex, Pattern.MULTILINE);
            Matcher matcher = regexPattern.matcher(patchDiff.getFullDiff());

            while(matcher.find()){
                // this will record neither the location of the match nor the presence condition
                patternMatchResult.addPatternMatch(new PatternMatch(this));
            }
        }
        for(String regex : shortRegex){
            Pattern regexPattern = Pattern.compile(regex);
            Matcher matcher = regexPattern.matcher(patchDiff.getOneLineShortDiff());

            while(matcher.find()){
                // this will record neither the location of the match nor the presence condition
                patternMatchResult.addPatternMatch(new PatternMatch(this));
            }
        }
        return patternMatchResult.hasMatch();
    }
}
