package org.variantsync.diffdetective.experiments.thesis_bm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import org.apache.commons.io.input.CharacterFilterReader;
import org.apache.commons.lang3.NotImplementedException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.tinylog.Logger;
import org.variantsync.diffdetective.AnalysisRunner;
import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.analysis.AnalysisResult.ResultKey;
import org.variantsync.diffdetective.analysis.FilterAnalysis;
import org.variantsync.diffdetective.analysis.MetadataKeys;
import org.variantsync.diffdetective.analysis.StatisticsAnalysis;
import org.variantsync.diffdetective.diff.git.GitDiffer;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.editclass.EditClass;
import org.variantsync.diffdetective.editclass.EditClassCatalogue;
import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;
import org.variantsync.diffdetective.metadata.Metadata;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.util.CSV;
import org.variantsync.diffdetective.util.Clock;
import org.variantsync.diffdetective.util.Diagnostics;
import org.variantsync.diffdetective.util.IO;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.Construction;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.Projection;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.filter.VariationDiffFilter;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParser;
import org.variantsync.functjonal.category.InplaceSemigroup;
import org.variantsync.functjonal.map.MergeMap;

import com.github.gumtreediff.matchers.CompositeMatchers;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.Tree;

import static org.variantsync.diffdetective.variation.diff.Time.AFTER;
import static org.variantsync.diffdetective.variation.diff.Time.BEFORE;

/**
 * Validates, evaluates and benchmarks the construction of {@link VariationDiff}s using Gumtree.
 *
 * This experiment computes the variation diff from
 * <ol>
 * <li>a line matching ({@link VariationDiffParser#createVariationDiff Viegener's algorithm}
 * <li>a tree matching computed by Gumtree ({@link Construction#diffUsingMatching}
 * <li>a hybrid matching ({@link Construction#improveMatching})
 * </ol>
 * compares them using some quality metrics and stores timing statistics.
 *
 * @author Benjamin Moosherr
 * @see "Constructing Variation Diffs Using Tree Diffing Algorithms"
 */
public class ConstructionValidation implements Analysis.Hooks {
    public static final ResultKey<Result> RESULT = new ResultKey<>("ConstructionValidation");
    /**
     * Aggregate of the results of the three comparisons.
     */
    public static final class Result implements Metadata<Result> {
        public ComparisonResult[] comparisons = new ComparisonResult[] {
            new ComparisonResult("old vs new"),
            new ComparisonResult("old vs improved"),
            new ComparisonResult("new vs improved")
        };

        public static final InplaceSemigroup<Result> ISEMIGROUP = (a, b) -> {
            for (int i = 0; i < a.comparisons.length; ++i) {
                a.comparisons[i].append(b.comparisons[i]);
            }
        };

        @Override
        public InplaceSemigroup<Result> semigroup() {
            return ISEMIGROUP;
        }

        @Override
        public LinkedHashMap<String, Object> snapshot() {
            LinkedHashMap<String, Object> snap = new LinkedHashMap<>();
            for (int i = 0; i < comparisons.length; ++i) {
                snap.putAll(comparisons[i].snapshot());
            }
            return snap;
        }

        @Override
        public void setFromSnapshot(LinkedHashMap<String, String> snap) {
            throw new NotImplementedException();
        }
    }

    /**
     * Timing of a variation diff construction with a specific matching algorithm and quality results compared to another variation diff.
     */
    public static final class ComparisonResult implements Metadata<ComparisonResult> {
        public String name;
        /** Duration of the matching computation. */
        public long comparisonDuration = 0;
        /** How many variation diffs are equal to the compared variation diff. */
        public int equal = 0;
        /** How many variation diffs are different to the compared variation diff. */
        public int different = 0;
        /** Counts of edit class flows (edit class pair of a projection of the compared
         * variation diffs) */
        public MergeMap<EditClass, MergeMap<EditClass, Integer>> editClassMovements =
            new MergeMap<>(new HashMap<>(), (a, b) -> {
                a.putAll(b);
                return a;
            });

        public ComparisonResult() {
        }

        public ComparisonResult(String name) {
            this.name = name;
        }

        public static final InplaceSemigroup<ComparisonResult> ISEMIGROUP = (a, b) -> {
            a.name = Metadata.mergeEqual(a.name, b.name);
            a.comparisonDuration += b.comparisonDuration;
            a.equal += b.equal;
            a.different += b.different;
            a.editClassMovements.putAll(b.editClassMovements);
        };

        @Override
        public InplaceSemigroup<ComparisonResult> semigroup() {
            return ISEMIGROUP;
        }

        @Override
        public LinkedHashMap<String, Object> snapshot() {
            LinkedHashMap<String, Object> snap = new LinkedHashMap<>();
            snap.put(name + " comparisonDuration", comparisonDuration);
            snap.put(name + " equal", equal);
            snap.put(name + " different", different);
            editClassMovements.forEach((oldEditClass, newEditClasses) -> {
                newEditClasses.forEach((newEditClass, count) -> {
                    snap.put(
                        String.format(
                            "%s: %s from %s to %s",
                            name,
                            MetadataKeys.EDIT_CLASS_MOVEMENT,
                            oldEditClass,
                            newEditClass
                        ),
                        count
                    );
                });
            });
            return snap;
        }

        @Override
        public void setFromSnapshot(LinkedHashMap<String, String> snap) {
            throw new NotImplementedException();
        }
    }

    /**
     * Main method to start the validation.
     * @param args Command-line options.
     * @throws IOException When copying the log file fails.
     */
    public static void main(String[] args) throws IOException {
        AnalysisRunner.run(AnalysisRunner.Options.DEFAULT(args), (repo, repoOutputDir) ->
            Analysis.forEachCommit(() ->
                new Analysis(
                    "ConstructionValidation",
                    List.of(
                        new FilterAnalysis(VariationDiffFilter.notEmpty()),
                        new ConstructionValidation(
                            // new CompositeMatchers.XyMatcher(),
                            new CompositeMatchers.ClassicGumtree(),
                            // new CompositeMatchers.SimpleGumtree(),
                            // new CompositeMatchers.HybridGumtree(),
                            // new CompositeMatchers.Theta(),
                            // new CompositeMatchers.ClassicGumtreeTheta(), // buggy
                            ProposedEditClasses.Instance),
                        new StatisticsAnalysis()
                    ),
                    repo,
                    repoOutputDir
                ),
                100,
                Diagnostics.INSTANCE.run().getNumberOfAvailableProcessors()
            )
        );
    }

    private class Statistics {
        public long variationTreeParseDuration = 0;
        public boolean oldAndHybridAreEqual = false;
        public VariationDiffStatistics[] variationDiff = new VariationDiffStatistics[3];

        public Statistics() {
            for (int i = 0; i < variationDiff.length; ++i) {
                variationDiff[i] = new VariationDiffStatistics();
            }
        }

        public void writeCsvCells(Writer destination) throws IOException {
            writeCsvCell(destination, String.valueOf(variationTreeParseDuration));
            destination.write(String.valueOf(oldAndHybridAreEqual));
            for (int i = 0; i < variationDiff.length; ++i) {
                destination.write(CSV.DEFAULT_CSV_DELIMITER);
                variationDiff[i].writeCsvCells(destination);
            }
        }
    }

    private class VariationDiffStatistics {
        public long matchingDuration = 0;
        public long constructionDuration = 0;
        public long metricDuration = 0;
        public int artifactCount = 0;
        public int annotationCount = 0;
        public int nodeCount = 0;
        public int matchingSize = 0;
        public int edgeCount = 0;
        public int maxEdgeCount = 0;

        public void writeCsvCells(Writer destination) throws IOException {
            writeCsvCell(destination, matchingDuration);
            writeCsvCell(destination, constructionDuration);
            writeCsvCell(destination, metricDuration);
            writeCsvCell(destination, artifactCount);
            writeCsvCell(destination, annotationCount);
            writeCsvCell(destination, nodeCount);
            writeCsvCell(destination, matchingSize);
            writeCsvCell(destination, edgeCount);
            destination.write(String.valueOf(maxEdgeCount));
        }
    }

    private void writeCsvCell(Writer destination, Object o) throws IOException {
        destination.write(o.toString());
        destination.write(CSV.DEFAULT_CSV_DELIMITER);
    }

    private final Matcher matcher;
    private final EditClassCatalogue editClasses;
    private BufferedWriter destination;

    public ConstructionValidation(Matcher matcher, EditClassCatalogue editClasses) {
        this.matcher = matcher;
        this.editClasses = editClasses;
    }

    @Override
    public void initializeResults(Analysis analysis) {
        analysis.append(RESULT, new Result());
    }

    @Override
    public void beginBatch(Analysis analysis) throws IOException {
        destination = new BufferedWriter(new OutputStreamWriter(IO.newBufferedOutputStream(analysis.getOutputFile())));
    }


    @Override
    public boolean analyzeVariationDiff(Analysis analysis) throws Exception, DiffParseException {
        Logger.info("current patch: {} {} (by thread {})", analysis.getCurrentPatch().getFileName(), analysis.getCurrentPatch().getCommitHash(), Thread.currentThread().getId());
        Statistics statistics = new Statistics();
        try {
            statistics.variationDiff[0].matchingDuration = 0;
            statistics.variationDiff[0].constructionDuration = 0;

            Clock clock = new Clock();
            final VariationDiff<DiffLinesLabel> beforeVariationTree = parseVariationTree(analysis, analysis.getCurrentCommit().getParent(0));
            final VariationDiff<DiffLinesLabel> afterVariationTree = parseVariationTree(analysis, analysis.getCurrentCommit());
            statistics.variationTreeParseDuration += clock.getPassedMilliseconds();

            beforeVariationTree.assertConsistency();
            afterVariationTree.assertConsistency();

            clock.start();
            final DiffNode<DiffLinesLabel> newVariationDiffRoot = Construction.diffUsingMatching(
                beforeVariationTree.getRoot().projection(BEFORE),
                afterVariationTree.getRoot().projection(AFTER),
                augmentedMatcher(statistics.variationDiff[1])
            );
            final var newVariationDiff = new VariationDiff<DiffLinesLabel>(newVariationDiffRoot);
            statistics.variationDiff[1].constructionDuration += clock.getPassedMilliseconds() - statistics.variationDiff[1].matchingDuration;

            newVariationDiff.assertConsistency();

            final VariationDiff<DiffLinesLabel> improvedVariationDiff = analysis.getCurrentVariationDiff().deepCopy();
            improvedVariationDiff.assertConsistency();
            clock.start();
            Construction.improveMatching(improvedVariationDiff.getRoot(), augmentedMatcher(statistics.variationDiff[2]));
            statistics.variationDiff[2].constructionDuration += clock.getPassedMilliseconds() - statistics.variationDiff[2].matchingDuration;
            improvedVariationDiff.assertConsistency();

            counts(analysis.getCurrentVariationDiff(), statistics.variationDiff[0]);
            counts(newVariationDiff, statistics.variationDiff[1]);
            counts(improvedVariationDiff, statistics.variationDiff[2]);

            var compareResults0 = compare(analysis.getCurrentVariationDiff(), newVariationDiff);
            var compareResults1 = compare(analysis.getCurrentVariationDiff(), improvedVariationDiff);
            var compareResults2 = compare(newVariationDiff, improvedVariationDiff);

            var result = analysis.get(RESULT);
            result.comparisons[0].append(compareResults0);
            result.comparisons[1].append(compareResults1);
            result.comparisons[2].append(compareResults2);

            statistics.oldAndHybridAreEqual = compareResults2.different > 0;
            statistics.writeCsvCells(destination);
            destination.newLine();
        } catch (Throwable t) {
            Logger.error(t, "{} {}", analysis.getCurrentPatch().getFileName(), analysis.getCurrentPatch().getCommitHash());
        }
        return true;
    }

    @Override
    public void endBatch(Analysis analysis) throws IOException {
        destination.newLine();
        destination.append(Metadata.show(analysis.getResult().snapshot()));
        destination.close();
    }

    private void counts(VariationDiff<DiffLinesLabel> tree, VariationDiffStatistics statistics) {
        Clock clock = new Clock();
        tree.forAll(node -> {
            ++statistics.nodeCount;

            if (!node.isAdd()) {
                if (node.isArtifact()) {
                    ++statistics.artifactCount;
                } else {
                    ++statistics.annotationCount;
                }
            }

            if (node.isNon()) {
                ++statistics.matchingSize;
            }

            if (!node.isRoot()) {
                if (node.getParent(BEFORE) == node.getParent(AFTER)) {
                    ++statistics.edgeCount;
                } else {
                    Time.forAll(time -> {
                        if (node.getParent(time) != null) {
                            ++statistics.edgeCount;
                        }
                    });
                }
                Time.forAll(time -> {
                    if (node.getParent(time) != null) {
                        ++statistics.maxEdgeCount;
                    }
                });
            }
        });
        statistics.metricDuration += clock.getPassedMilliseconds();
    }

    private VariationDiff<DiffLinesLabel> parseVariationTree(Analysis analysis, RevCommit commit) throws IOException, DiffParseException {
        try (BufferedReader afterFile =
            new BufferedReader(
                /*
                 * JGit may insert a BOM (byte order mask, a Unicode feature) at unfortunate places
                 * (e.g. at the start of a diff, right before a {@code +}, {@code -} or space.
                 * Hence, BOMs need to be removed. A similar heuristic is implemented in {@link GitDiffer#getFullDiff()}.
                 */
                new CharacterFilterReader(
                    GitDiffer.getBeforeFullFile(
                        analysis.getRepository().getGitRepo().run(),
                        commit,
                        analysis.getCurrentPatch().getFileName()),
                    0xfeff)) // BOM, same as GitDiffer.BOM_PATTERN
        ) {
            return VariationDiffParser.createVariationTree(afterFile, analysis.getRepository().getParseOptions().variationDiffParseOptions());
        }
    }

    private <L extends Label> ComparisonResult compare(VariationDiff<L> a, VariationDiff<L> b) {
        ComparisonResult comparisonResult = new ComparisonResult();
        boolean[] equal = new boolean[] { true };
        Clock clock = new Clock();
        parallelPreOrderWalk(a, b,
            (oldNode, newNode) -> {
                if (oldNode.getDiffType() != newNode.getDiffType()) {
                    equal[0] = false;
                }

                if (oldNode.isArtifact()) {
                    EditClass oldEditClass = editClasses.match(oldNode);
                    EditClass newEditClass = editClasses.match(newNode);

                    comparisonResult
                        .editClassMovements
                        .computeIfAbsent(oldEditClass, (key) -> new MergeMap<>(new HashMap<>(), Integer::sum))
                        .put(newEditClass, 1);
                }
            }
        );
        comparisonResult.comparisonDuration += clock.getPassedMilliseconds();
        if (equal[0]) {
            ++comparisonResult.equal;
        } else {
            ++comparisonResult.different;
        }
        return comparisonResult;
    }

    private <L extends Label> void parallelPreOrderWalk(VariationDiff<L> nodeA, VariationDiff<L> nodeB, BiConsumer<DiffNode<L>, DiffNode<L>> consumer) {
        Time.forAll(time -> {
            parallelPreOrderWalk(
                nodeA.getRoot().projection(time),
                nodeB.getRoot().projection(time),
                consumer,
                new HashSet<>()
            );
        });
    }

    private <L extends Label> void parallelPreOrderWalk(Projection<L> nodeA, Projection<L> nodeB, BiConsumer<DiffNode<L>, DiffNode<L>> consumer, Set<DiffNode<L>> visited) {
        if (!visited.add(nodeA.getBackingNode())) {
            return;
        }

        try {
            Assert.assertEquals(nodeA.getNodeType(), nodeB.getNodeType());
            Assert.assertEquals(nodeA.getFormula(), nodeB.getFormula());
            // The label of annotations may be different as only their semantic has to be
            // equivalent.
            if (nodeA.isArtifact()) {
                Assert.assertEquals(nodeA.getLabel(), nodeB.getLabel());
            }

            consumer.accept(nodeA.getBackingNode(), nodeB.getBackingNode());
        } catch (AssertionError e) {
            var a = nodeA.getBackingNode();
            var b = nodeB.getBackingNode();
            Logger.error("Nodes {} and {} with labels {} and {}, formulas {} and {}", a, b, a.getLabel(), b.getLabel(), a.getFormula(), b.getFormula());
            throw e;
        }

        var childrenA = nodeA.getChildren().iterator();
        var childrenB = nodeB.getChildren().iterator();
        while (childrenA.hasNext() && childrenB.hasNext()) {
            var childA = childrenA.next();
            var childB = childrenB.next();

            parallelPreOrderWalk(childA, childB, consumer, visited);
        }

        Assert.assertFalse(childrenA.hasNext());
        Assert.assertFalse(childrenB.hasNext());
    }

    private Matcher augmentedMatcher(VariationDiffStatistics statistics) {
        return new Matcher() {
            @Override
            public MappingStore match(Tree src, Tree dst, MappingStore mappings) {
                Clock matchingClock = new Clock();
                MappingStore result = matcher.match(src, dst, mappings);
                statistics.matchingDuration += matchingClock.getPassedMilliseconds();
                return result;
            }
        };
    }
}
