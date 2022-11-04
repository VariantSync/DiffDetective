package org.variantsync.diffdetective.preliminary.analysis;

import org.variantsync.diffdetective.diff.PatchDiff;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.editclass.EditClass;
import org.variantsync.diffdetective.preliminary.GitDiff;
import org.variantsync.diffdetective.preliminary.analysis.data.PatchDiffAnalysisResult;
import org.variantsync.diffdetective.preliminary.analysis.data.PatternMatch;
import org.variantsync.diffdetective.preliminary.pattern.FeatureContextReverseEngineering;
import org.variantsync.diffdetective.preliminary.pattern.elementary.*;
import org.variantsync.diffdetective.preliminary.pattern.semantic.SemanticPattern;

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
            for (DiffNode diffNode : diffTree.computeArtifactNodes()) {
                for (FeatureContextReverseEngineering<DiffNode> pattern : patterns) {
                    if (pattern.getPattern() instanceof EditClass) {
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
