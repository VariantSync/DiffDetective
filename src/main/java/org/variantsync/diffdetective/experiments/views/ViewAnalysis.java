package org.variantsync.diffdetective.experiments.views;

import org.prop4j.Node;
import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.analysis.logic.SAT;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;
import org.variantsync.diffdetective.experiments.views.result.ViewEvaluation;
import org.variantsync.diffdetective.util.*;
import org.variantsync.diffdetective.util.fide.FixTrueFalse;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.Projection;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.view.DiffView;
import org.variantsync.diffdetective.variation.tree.view.query.ArtifactQuery;
import org.variantsync.diffdetective.variation.tree.view.query.FeatureQuery;
import org.variantsync.diffdetective.variation.tree.view.query.Query;
import org.variantsync.diffdetective.variation.tree.view.query.VariantQuery;

import java.io.IOException;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;

import static org.variantsync.diffdetective.util.fide.FormulaUtils.negate;

public class ViewAnalysis implements Analysis.Hooks {
    // Result data
    public static final String EDIT_COMPLEXITIES_EXTENSION = ".views.csv";
    private StringBuilder csv;
    private Random random;

    @Override
    public void initializeResults(Analysis analysis) {
        Analysis.Hooks.super.initializeResults(analysis);

        random = new Random();

        csv = new StringBuilder();
        csv.append(ViewEvaluation.makeHeader(CSV.DEFAULT_CSV_DELIMITER)).append(StringUtils.LINEBREAK);
    }

    private void runQueryExperiment(Analysis analysis, final DiffTree d, final Query q) {
        final long preprocessingTime, naiveTime, optimizedTime;

        final Clock c = new Clock();

        final BiPredicate<Time, Projection> inV = DiffView.computeWhenNodesAreRelevant(d, q);

        preprocessingTime = c.getPassedMilliseconds();

        // measure naive view generation
        try {
            c.start();
            DiffView.naive(d, q, inV);
            naiveTime = c.getPassedMilliseconds();
        } catch (IOException | DiffParseException e) {
            throw new RuntimeException(e);
        }

        // measure optimized view generation
        c.start();
        final DiffTree view = DiffView.optimized(d, q, inV);
        optimizedTime = c.getPassedMilliseconds();

        // export results
        final ViewEvaluation e = new ViewEvaluation(
                analysis.getCurrentCommitDiff().getAbbreviatedCommitHash(),
                analysis.getCurrentPatch().getFileName(),
                q,
                preprocessingTime + naiveTime,
                preprocessingTime + optimizedTime,
                ViewEvaluation.DiffStatistics.of(d),
                ViewEvaluation.DiffStatistics.of(view)
        );
        csv.append(e.toCSV()).append(StringUtils.LINEBREAK);
    }

    @Override
    public boolean analyzeDiffTree(Analysis analysis) throws Exception {
        final DiffTree d                = analysis.getCurrentDiffTree();
        final Collection<Query> queries = generateRandomQueries(d);

        for (final Query q : queries) {
            runQueryExperiment(analysis, d, q);
        }

        return Analysis.Hooks.super.analyzeDiffTree(analysis);
    }

    private List<Query> generateRandomQueries(final DiffTree d) {
        final List<Node> deselectedPCs = new ArrayList<>();
        final Set<String> features = new HashSet<>();
        final Set<String> artifacts = new HashSet<>();

        d.forAll(a -> {
            if (a.isArtifact()) {
                // Collect all PCs negated
                if (!ProposedEditClasses.Untouched.matches(a)) {
                    a.getDiffType().forAllTimesOfExistence(t -> deselectedPCs.add(negate(a.getPresenceCondition(t))));
                }

                // Collect all artifact names.
                artifacts.addAll(a.getLabelLines());
            }

            // Collect all features
            else if (a.isConditionalAnnotation()) {
                features.addAll(a.getFormula().getUniqueContainedFeatures());
            }
        });

        features.remove(FixTrueFalse.True.var.toString());
        features.remove(FixTrueFalse.False.var.toString());

        final List<Query> queries = new ArrayList<>(3);
        addRandomQuery(deselectedPCs, this::randomVariantQuery,  queries);
        addRandomQuery(features,      this::randomFeatureQuery,  queries);
        addRandomQuery(artifacts,     this::randomArtifactQuery, queries);
        return queries;
    }

    private static <QueryData, QueryCandidates extends Collection<QueryData>> void addRandomQuery(
            QueryCandidates source,
            Function<QueryCandidates, Query> pick,
            Collection<Query> target
    ) {
        if (!source.isEmpty()) {
            final Query e = pick.apply(source);
            if (e != null) {
                target.add(e);
            }
        }
    }

    private Query randomVariantQuery(final List<Node> deselectedPCs) {
        /*
        Do we need this?
        I think for our feasibility study, we can ignore this.
        Without semantic duplicates, we sample uniform randomly over all PCs of edited artifacts.
        Otherwise, we would sample uniform randomly over all _unique_ PCs of edited artifacts.
         */
        // remove semantic duplicates
//        final List<Node> deselectedPCsList = new ArrayList<>(deselectedPCs);
//        removeSemanticDuplicates(deselectedPCsList);

        /*
         * Select a random satisfiable configuration (i.e., a non-false config).
         * Unsatisfiable configs cause empty views which
         * (1) we suspect to be rather useless and thus unused in practice
         * (2) cause a crash in view generation because everything is removed, even the mandatory root.
         */
        Node winner = null;
        while (winner == null && !deselectedPCs.isEmpty()) {
            final Node candidate = deselectedPCs.get(random.nextInt(deselectedPCs.size()));
            FixTrueFalse.EliminateTrueAndFalseInplace(candidate);
            if (SAT.isSatisfiableAlreadyEliminatedTrueAndFalse(candidate)) {
                winner = candidate;
            } else {
                deselectedPCs.remove(candidate);
            }
        }

        if (winner == null) {
            return null;
        }

        return VariantQuery.fromConfigurationWithoutTrueAndFalseLiterals(winner);
    }

    private Query randomFeatureQuery(final Set<String> features) {
        /*
        Pick a random feature for our query.
        Since we actually just need a single random value, we could also just pick the first element
        but I don't know if there is any hidden sorting within the set that would bias this choice.
         */
        return new FeatureQuery(CollectionUtils.getRandomElement(random, features));
    }

    private Query randomArtifactQuery(final Set<String> artifacts) {
        /*
        Pick a random artifact for our query.
        Since we actually just need a single random value, we could also just pick the first element
        but I don't know if there is any hidden sorting within the set that would bias this choice.
         */
        return new ArtifactQuery(CollectionUtils.getRandomElement(random, artifacts));
    }

    @Override
    public void endBatch(Analysis analysis) throws IOException {
        IO.write(
                FileUtils.addExtension(analysis.getOutputFile(), EDIT_COMPLEXITIES_EXTENSION),
                csv.toString()
        );
    }
}
