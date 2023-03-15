package org.variantsync.diffdetective.preliminary.analysis.data;

import org.prop4j.Node;
import org.variantsync.diffdetective.preliminary.pattern.Pattern;
import org.variantsync.diffdetective.preliminary.evaluation.FeatureContext;
import org.variantsync.diffdetective.preliminary.pattern.FeatureContextReverseEngineering;

import java.util.Arrays;

/**
 * Data class containing a single match of a pattern in a patch.
 * Contains information about the feature mapping and the location of the match.
 * There can be multiple PatternMatches per patch.
 *
 * @author Sören Viegener
 */
@Deprecated
public class PatternMatch<E> {
    private final FeatureContextReverseEngineering<E> pattern;
    private final Node[] featureMappings;
    private final int startLineDiff;
    private final int endLineDiff;

    public PatternMatch(FeatureContextReverseEngineering<E> pattern, int startLineDiff, int endLineDiff, Node... featureMappings) {
        this.pattern = pattern;
        this.featureMappings = featureMappings;
        this.startLineDiff = startLineDiff;
        this.endLineDiff = endLineDiff;
    }

    public PatternMatch(FeatureContextReverseEngineering<E> pattern, int startLineDiff, int endLineDiff) {
        this.pattern = pattern;
        this.featureMappings = null;
        this.startLineDiff = startLineDiff;
        this.endLineDiff = endLineDiff;
    }

    public PatternMatch(FeatureContextReverseEngineering<E> pattern) {
        this.pattern = pattern;
        this.featureMappings = null;
        this.startLineDiff = -1;
        this.endLineDiff = -1;
    }

    public FeatureContext[] getFeatureContexts(){
        return pattern.getFeatureContexts(this);
    }

    public String getPatternName() {
        return pattern.getPattern().getName();
    }

    public Pattern<E> getPattern() {
        return pattern.getPattern();
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
