package analysis.old;

import analysis.data.PatternMatch;
import org.antlr.v4.runtime.tree.ParseTree;
import pattern.antlr.AddIfdefAntlrPattern;
import patterns.PatternParser;
import patterns.PatternParserBaseVisitor;

import java.util.List;

public class PatternVisitor extends PatternParserBaseVisitor<List<PatternMatch>> {
    AddIfdefAntlrPattern addIfdefAntlrPattern = new AddIfdefAntlrPattern();

    @Override
    public List<PatternMatch> visitPatch(PatternParser.PatchContext ctx) {
        traverseTree(ctx);
        return super.visitPatch(ctx);
    }

    private void traverseTree(ParseTree parseTree){

        int n = parseTree.getChildCount();

        for(int i = 0; i < n; ++i) {
            ParseTree child = parseTree.getChild(i);
            traverseTree(child);
        }
    }

    @Override
    public List<PatternMatch> visitAddIfdef(PatternParser.AddIfdefContext ctx) {
        for(PatternParser.AnyNormalContext anyNormalContext : ctx.patternContent().anyNormal()){
            System.out.println(anyNormalContext.ADD_NORMAL());
        }

        return super.visitAddIfdef(ctx);
    }

    @Override
    protected List<PatternMatch> aggregateResult(List<PatternMatch> aggregate,
                                                 List<PatternMatch> nextResult) {
        if(aggregate == null){
            return nextResult;
        }
        if(nextResult == null){
            return aggregate;
        }
        aggregate.addAll(nextResult);
        return aggregate;
    }
}
