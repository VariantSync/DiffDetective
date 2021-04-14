package pattern.antlr;

import analysis.data.PatternMatch;
import evaluation.FeatureContext;
import org.antlr.v4.runtime.tree.ParseTree;
import patterns.PatternParser;

public class AddIfdefElseAntlrPattern extends AntlrPattern{

    public static final String PATTERN_NAME = "AddIfDefElseAntlr";

    public AddIfdefElseAntlrPattern() {
        this.name = PATTERN_NAME;
    }

    @Override
    public PatternMatch getMatch(ParseTree parseTree) {
        if(parseTree instanceof PatternParser.AddIfdefElseContext){
            PatternParser.AddIfdefElseContext ctx = (PatternParser.AddIfdefElseContext) parseTree;

            boolean thenAddNormal = false;
            boolean elseAddNormal = false;
            for(PatternParser.AnyNormalContext anyNormal : ctx.thenContent.anyNormal()){
                if(anyNormal.ADD_NORMAL() != null){
                    thenAddNormal = true;
                    break;
                }
            }

            for(PatternParser.AnyNormalContext anyNormal : ctx.elseContent.anyNormal()){
                if(anyNormal.ADD_NORMAL() != null){
                    elseAddNormal = true;
                    break;
                }
            }

            if(thenAddNormal && elseAddNormal){
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
