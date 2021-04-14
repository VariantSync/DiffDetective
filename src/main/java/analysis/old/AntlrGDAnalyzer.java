package analysis.old;

import analysis.GDAnalyzer;
import analysis.data.PatchDiffAnalysisResult;
import analysis.data.PatternMatch;
import diff.data.GitDiff;
import diff.data.PatchDiff;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import pattern.EditPattern;
import pattern.antlr.*;
import patterns.PatternLexer;
import patterns.PatternParser;

import java.util.ArrayList;
import java.util.List;

public class AntlrGDAnalyzer extends GDAnalyzer {
    public static final AntlrPattern[] DEFAULT_PATTERNS = new AntlrPattern[]{
            new AddIfdefAntlrPattern(),
            new AddIfdefElseAntlrPattern(),
            new AddIfdefWrapElseAntlrPattern(),
            new AddIfdefWrapThenAntlrPattern(),
    };


    public AntlrGDAnalyzer(GitDiff gitDiff) {
        this(gitDiff, DEFAULT_PATTERNS);
    }

    public AntlrGDAnalyzer(GitDiff gitDiff, AntlrPattern[] patterns) {
        super(gitDiff, patterns);
    }

    @Override
    protected PatchDiffAnalysisResult analyzePatch(PatchDiff patchDiff) {
        //System.out.printf("parsing patch %s, %s (len: %d)%n", patchDiff.getCommitDiff().getAbbreviatedCommitHash(), patchDiff.getFileName(), patchDiff.getShortDiff().length);
        String oneLineShortDiff = patchDiff.getOneLineShortDiff();
        PatternLexer patternLexer = new PatternLexer(CharStreams.fromString(oneLineShortDiff));
        PatternParser patternParser = new PatternParser(new CommonTokenStream(patternLexer));
        patternParser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                System.out.printf("Could not parse %s, %s:%n", patchDiff.getCommitDiff().getAbbreviatedCommitHash(), patchDiff.getFileName());
                System.out.println(oneLineShortDiff);
                System.out.println(patchDiff.getShortDiffString(true));
                System.out.println(patchDiff.getFullDiff());
                //throw new IllegalStateException("failed to parse at line " + line + " due to " + msg, e);
            }
        });
        PatchDiffAnalysisResult patchResult = new PatchDiffAnalysisResult(patchDiff);
        patchResult.addPatternMatches(traverseTree(patternParser.patch()));
        return patchResult;
    }

    private List<PatternMatch> traverseTree(ParseTree parseTree){
        List<PatternMatch> result = new ArrayList<>();

        for(EditPattern pattern : patterns){
            PatternMatch patternMatch = ((AntlrPattern) pattern).getMatch(parseTree);
            if(patternMatch != null){
                result.add(patternMatch);
            }
        }

        int n = parseTree.getChildCount();
        for(int i = 0; i < n; ++i) {
            ParseTree child = parseTree.getChild(i);
            result.addAll(traverseTree(child));
        }
        return result;
    }
}
