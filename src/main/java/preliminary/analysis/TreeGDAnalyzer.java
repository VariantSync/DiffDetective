package preliminary.analysis;

import diff.GitDiff;
import diff.PatchDiff;
import diff.difftree.DiffNode;
import diff.difftree.DiffTree;
import pattern.atomic.AtomicPattern;
import preliminary.analysis.data.PatchDiffAnalysisResult;
import preliminary.analysis.data.PatternMatch;
import preliminary.pattern.FeatureContextReverseEngineering;
import preliminary.pattern.atomic.*;
import preliminary.pattern.semantic.SemanticPattern;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the GDAnalyzer using the diff tree.
 *
 * Matches atomic patterns on the code nodes and semantic patterns on the annotation nodes.
 */
@Deprecated
public class TreeGDAnalyzer extends GDAnalyzer<DiffNode> {
    private static List<FeatureContextReverseEngineering<DiffNode>> getPatterns(boolean atomic, boolean semantic) {
        final List<FeatureContextReverseEngineering<DiffNode>> patterns = new ArrayList<>();
        if (atomic) {
            patterns.addAll(List.of(
                    new FeatureContextOfAddToPC(),
                    new FeatureContextOfAddWithMapping(),
                    new FeatureContextOfRemFromPC(),
                    new FeatureContextOfRemWithMapping(),
                    new FeatureContextOfGeneralization(),
                    new FeatureContextOfSpecialization(),
                    new FeatureContextOfReconfiguration(),
                    new FeatureContextOfRefactoring(),
                    new FeatureContextOfUntouched()
            ));
        }
        if (semantic) {
            patterns.addAll(SemanticPattern.All);
        }
        return patterns;
    }

    public TreeGDAnalyzer(GitDiff gitDiff, List<FeatureContextReverseEngineering<DiffNode>> patterns) {
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
                for (FeatureContextReverseEngineering<DiffNode> pattern : patterns) {
                    if (pattern.getPattern() instanceof AtomicPattern) {
                        results.add(pattern.createMatch(diffNode));
//                        pattern.match(diffNode).ifPresent(results::add);
                    }
                }
            }

            // match semantic patterns
            for (DiffNode diffNode : diffTree.computeAnnotationNodes()) {
                for (FeatureContextReverseEngineering<DiffNode> pattern : patterns) {
                    if (pattern.getPattern() instanceof SemanticPattern s) {
                        s.match(diffNode).ifPresent(results::add);
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
