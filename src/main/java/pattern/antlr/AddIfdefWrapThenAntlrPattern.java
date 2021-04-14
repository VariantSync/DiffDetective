package pattern.antlr;

import analysis.data.PatternMatch;
import evaluation.FeatureContext;
import org.antlr.v4.runtime.tree.ParseTree;
import patterns.PatternParser;

public class AddIfdefWrapThenAntlrPattern extends AntlrPattern{

    public static final String PATTERN_NAME = "AddIfDefWrapThenAntlr";

    public AddIfdefWrapThenAntlrPattern() {
        this.name = PATTERN_NAME;
    }

    @Override
    public PatternMatch getMatch(ParseTree parseTree) {
        if(parseTree instanceof PatternParser.AddIfdefElseContext){
            PatternParser.AddIfdefElseContext ctx = (PatternParser.AddIfdefElseContext) parseTree;

            boolean thenNormal = false;
            boolean elseAddNormal = false;
            for(PatternParser.AnyNormalContext anyNormal : ctx.thenContent.anyNormal()){
                if(anyNormal.NORMAL() != null){
                    thenNormal = true;
                    break;
                }
            }

            for(PatternParser.AnyNormalContext anyNormal : ctx.elseContent.anyNormal()){
                if(anyNormal.ADD_NORMAL() != null){
                    elseAddNormal = true;
                    break;
                }
            }

            if(thenNormal && elseAddNormal){
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
