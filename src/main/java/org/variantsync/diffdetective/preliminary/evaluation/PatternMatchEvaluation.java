package org.variantsync.diffdetective.preliminary.evaluation;

import org.prop4j.Node;
import org.variantsync.diffdetective.preliminary.analysis.data.CommitDiffAnalysisResult;
import org.variantsync.diffdetective.preliminary.analysis.data.PatchDiffAnalysisResult;
import org.variantsync.diffdetective.preliminary.analysis.data.PatternMatch;

/**
 * Data class containing a PatternMatch, the possible reverse-engineered FeatureContexts and the complexity of the feature context.
 */
@Deprecated
public class PatternMatchEvaluation {
    private final CommitDiffAnalysisResult commit;
    private final PatchDiffAnalysisResult patch;
    private final PatternMatch patternMatch;
    private final FeatureContext[] featureContexts;
    private final int featureContextComplexity;

    public PatternMatchEvaluation(CommitDiffAnalysisResult commit, PatchDiffAnalysisResult patch,
                                  PatternMatch patternMatch, FeatureContext[] featureContexts) {
        this.commit = commit;
        this.patch = patch;
        this.patternMatch = patternMatch;
        this.featureContexts = featureContexts;
        this.featureContextComplexity = getFeatureContextsComplexity(featureContexts);
    }

    public boolean isFeatureContextUnknown(){
        return featureContexts == null || featureContexts.length == 0;
    }

    public boolean canFeatureContextBeNull(){
        if(featureContexts == null){
            return false;
        }
        for (FeatureContext featureContext : featureContexts) {
            if(featureContext.isNull()){
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the minimal complexity of an array of feature contexts.
     * The complexity is in this case the amount of literals in the feature context
     * @param featureContexts the feature contexts of which to get the complexity
     * @return the minimal complexity of an array of feature contexts.
     */
    private int getFeatureContextsComplexity(FeatureContext[] featureContexts) {
        if (featureContexts == null || featureContexts.length == 0) {
            return -1;
        }

        int minComplexity = Integer.MAX_VALUE;
        for (FeatureContext featureContext : featureContexts) {
            minComplexity = Math.min(minComplexity, getFeatureContextComplexity(featureContext));
        }
        return minComplexity;
    }

    private int getFeatureContextComplexity(FeatureContext featureContext) {
        Node node = featureContext.getNode();
        if (node == null) {
            return 0;
        }
        return node.getLiterals().size();
    }

    public CommitDiffAnalysisResult getCommit() {
        return commit;
    }

    public PatchDiffAnalysisResult getPatch() {
        return patch;
    }

    public PatternMatch getPatternMatch() {
        return patternMatch;
    }

    public FeatureContext[] getFeatureContexts() {
        return featureContexts;
    }

    public FeatureContext getSimplestFeatureContext() {
        if(this.featureContexts == null){
            return null;
        }
        int minComplexity = Integer.MAX_VALUE;
        FeatureContext simplestFeatureContext = featureContexts[0];
        for(FeatureContext fc : featureContexts){
            int complexity = getFeatureContextComplexity(fc);
            if( complexity < minComplexity){
                minComplexity = complexity;
                simplestFeatureContext = fc;
            }
        }
        return simplestFeatureContext;
    }

    public int getFeatureContextComplexity() {
        return featureContextComplexity;
    }
}
