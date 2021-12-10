package analysis;

import analysis.data.PatchDiffAnalysisResult;
import analysis.data.PatternMatch;
import diff.GitDiff;
import diff.PatchDiff;
import diff.difftree.DiffNode;
import diff.difftree.DiffTree;
import pattern.EditPattern;
import pattern.atomic.AtomicPattern;
import pattern.semantic.SemanticPattern;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the GDAnalyzer using the diff tree.
 *
 * Matches atomic patterns on the code nodes and semantic patterns on the annotation nodes.
 */
public class TreeGDAnalyzer extends GDAnalyzer<DiffNode> {
    private static List<EditPattern<DiffNode>> getPatterns(boolean atomic, boolean semantic) {
        final List<EditPattern<DiffNode>> patterns = new ArrayList<>();
        if (atomic) {
            patterns.addAll(AtomicPattern.All);
        }
        if (semantic) {
            patterns.addAll(SemanticPattern.All);
        }
        return patterns;
    }

    public TreeGDAnalyzer(GitDiff gitDiff, List<EditPattern<DiffNode>> patterns) {
        super(gitDiff, patterns);
    }

    public TreeGDAnalyzer(GitDiff gitDiff, boolean atomic, boolean semantic) {
        this(gitDiff, getPatterns(atomic, semantic));
    }

    public TreeGDAnalyzer(GitDiff gitDiff) {
        this(gitDiff, true, true);
    }

    /**
     * Analyzes a patch using the given patterns.
     * Atomic patterns are matched on the code nodes. Semantic patterns are matched on the annotation nodes
     * @param patchDiff The PatchDiff that is analyzed
     * @return The result of the analysis
     */
    @Override
    protected PatchDiffAnalysisResult analyzePatch(PatchDiff patchDiff) {
        List<PatternMatch<DiffNode>> results = new ArrayList<>();

        DiffTree diffTree = patchDiff.getDiffTree();
        if(diffTree != null) {
            // match atomic patterns
            for (DiffNode diffNode : diffTree.computeCodeNodes()) {
                for (EditPattern<DiffNode> pattern : patterns) {
                    if(pattern instanceof AtomicPattern) {
                        pattern.match(diffNode).ifPresent(results::add);
                    }
                }
            }

            // match semantic patterns
            for (DiffNode diffNode : diffTree.computeAnnotationNodes()) {
                for (EditPattern<DiffNode> pattern : patterns) {
                    if(pattern instanceof SemanticPattern) {
                        pattern.match(diffNode).ifPresent(results::add);
                    }
                }
            }
        }else{
            results.add(new PatternMatch<>(patterns.get(0)));
        }
        PatchDiffAnalysisResult patchResult = new PatchDiffAnalysisResult(patchDiff);
        patchResult.addPatternMatches(results);
        return patchResult;
    }
}
