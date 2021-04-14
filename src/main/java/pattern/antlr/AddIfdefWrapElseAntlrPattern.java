package pattern.antlr;

import analysis.data.PatternMatch;
import evaluation.FeatureContext;
import org.antlr.v4.runtime.tree.ParseTree;
import patterns.PatternParser;

public class AddIfdefWrapElseAntlrPattern extends AntlrPattern{

    public static final String PATTERN_NAME = "AddIfDefWrapElseAntlr";

    public AddIfdefWrapElseAntlrPattern() {
        this.name = PATTERN_NAME;
    }

    @Override
    public PatternMatch getMatch(ParseTree parseTree) {
        if(parseTree instanceof PatternParser.AddIfdefElseContext){
            PatternParser.AddIfdefElseContext ctx = (PatternParser.AddIfdefElseContext) parseTree;

            boolean thenAddNormal = false;
            boolean elseNormal = false;
            for(PatternParser.AnyNormalContext anyNormal : ctx.thenContent.anyNormal()){
                if(anyNormal.ADD_NORMAL() != null){
                    thenAddNormal = true;
                    break;
                }
            }

            for(PatternParser.AnyNormalContext anyNormal : ctx.elseContent.anyNormal()){
                if(anyNormal.NORMAL() != null){
                    elseNormal = true;
                    break;
                }
            }

            if(thenAddNormal && elseNormal){
                return new PatternMatch(this);
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
