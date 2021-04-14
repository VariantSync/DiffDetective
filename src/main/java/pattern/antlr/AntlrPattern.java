package pattern.antlr;

import analysis.data.PatternMatch;
import analysis.data.PatternMatchResult;
import diff.data.PatchDiff;
import evaluation.FeatureContext;
import org.antlr.v4.runtime.tree.ParseTree;
import pattern.EditPattern;
import patterns.PatternParser;

public abstract class AntlrPattern extends EditPattern {

    @Override
    public boolean matches(PatchDiff patchDiff, PatternMatchResult patternMatchResult) {
        // TODO does this make sense? I don't think so
        throw new UnsupportedOperationException();
    }

    public abstract PatternMatch getMatch(ParseTree parseTree);
}
