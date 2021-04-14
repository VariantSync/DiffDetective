package pattern.antlr;

import analysis.data.PatternMatch;
import evaluation.FeatureContext;
import org.antlr.v4.runtime.tree.ParseTree;
import patterns.PatternParser;

public class AddIfdefAntlrPattern extends AntlrPattern{

    public static final String PATTERN_NAME = "AddIfDefAntlr";

    public AddIfdefAntlrPattern() {
        this.name = PATTERN_NAME;
    }

    @Override
    public PatternMatch getMatch(ParseTree parseTree) {
        if(parseTree instanceof PatternParser.AddIfdefContext){
            PatternParser.AddIfdefContext ctx = (PatternParser.AddIfdefContext) parseTree;
            for(PatternParser.AnyNormalContext anyNormal : ctx.patternContent().anyNormal()){
                if(anyNormal.ADD_NORMAL() != null){
                    return new PatternMatch(this);
                }
            }
        }
        return null;
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch patternMatch) {
        // TODO implement
        return new FeatureContext[0];
    }
}
