package analysis.data;

import evaluation.FeatureContext;
import org.prop4j.Node;
import pattern.EditPattern;

import java.util.Arrays;

/**
 * Data class containing a single match of a pattern in a patch.
 * Contains information about the feature mapping and the location of the match.
 * There can be multiple PatternMatches per patch.
 *
 * @author Sören Viegener
 */
public class PatternMatch {
    private final EditPattern pattern;
    private final Node[] featureMappings;
    private final int startLineDiff;
    private final int endLineDiff;

    public PatternMatch(EditPattern pattern, int startLineDiff, int endLineDiff, Node... featureMappings) {
        this.pattern = pattern;
        this.featureMappings = featureMappings;
        this.startLineDiff = startLineDiff;
        this.endLineDiff = endLineDiff;
    }

    public PatternMatch(EditPattern pattern, int startLineDiff, int endLineDiff) {
        this.pattern = pattern;
        this.featureMappings = null;
        this.startLineDiff = startLineDiff;
        this.endLineDiff = endLineDiff;
    }

    public PatternMatch(EditPattern pattern) {
        this.pattern = pattern;
        this.featureMappings = null;
        this.startLineDiff = -1;
        this.endLineDiff = -1;
    }

    public FeatureContext[] getFeatureContexts(){
        return pattern.getFeatureContexts(this);
    }

    public String getPatternName() {
        return pattern.getName();
    }

    public EditPattern getPattern() {
        return pattern;
    }

    public Node[] getFeatureMappings() {
        return featureMappings;
    }

    public int getStartLineDiff() {
        return startLineDiff;
    }

    public int getEndLineDiff() {
        return endLineDiff;
    }

    public boolean hasFeatureMappings(){
        return featureMappings != null;
    }

    public boolean hasDiffLocation(){
        return startLineDiff != -1 && endLineDiff != -1;
    }

    @Override
    public String toString() {
        String s = getPatternName();
        if(hasDiffLocation()){
            s += " from " + startLineDiff +  " to " + endLineDiff;
        }
        if(hasFeatureMappings()){
            s += " (Mappings: " + Arrays.toString(featureMappings) + ")";
        }
        return s;
    }
}
