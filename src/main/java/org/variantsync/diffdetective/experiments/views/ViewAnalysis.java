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
import org.variantsync.diffdetective.variation.tree.view.relevance.Search;
import org.variantsync.diffdetective.variation.tree.view.relevance.Trace;
import org.variantsync.diffdetective.variation.tree.view.relevance.Relevance;
import org.variantsync.diffdetective.variation.tree.view.relevance.Configure;

import java.io.IOException;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;

import static org.variantsync.diffdetective.util.fide.FormulaUtils.negate;

public class ViewAnalysis implements Analysis.Hooks {
    // Result data
    public static final String VIEW_CSV_EXTENSION = ".views.csv";
    private StringBuilder csv;
    private Random random;

    @Override
    public void initializeResults(Analysis analysis) {
        Analysis.Hooks.super.initializeResults(analysis);

        random = new Random();

        csv = new StringBuilder();
        csv.append(ViewEvaluation.makeHeader(CSV.DEFAULT_CSV_DELIMITER)).append(StringUtils.LINEBREAK);
    }

    private void runRelevanceExperiment(Analysis analysis, final DiffTree d, final Relevance rho) {
        final long preprocessingTime, naiveTime, optimizedTime;

        //Show.diff(d, "D").showAndAwait();

        final Clock c = new Clock();

        final BiPredicate<Time, Projection> inV = DiffView.computeWhenNodesAreRelevant(d, rho);

        preprocessingTime = c.getPassedMilliseconds();

        // measure naive view generation
        try {
            c.start();
            DiffView.naive(d, rho, inV);
            naiveTime = c.getPassedMilliseconds();
        } catch (IOException | DiffParseException e) {
            throw new RuntimeException(e);
        }

        // measure optimized view generation
        c.start();
        final DiffTree view = DiffView.optimized(d, rho, inV);
        optimizedTime = c.getPassedMilliseconds();

        // export results
        final ViewEvaluation e = new ViewEvaluation(
                analysis.getCurrentCommitDiff().getAbbreviatedCommitHash(),
                analysis.getCurrentPatch().getFileName(),
                rho,
                preprocessingTime + naiveTime,
                preprocessingTime + optimizedTime,
                ViewEvaluation.DiffStatistics.of(d),
                ViewEvaluation.DiffStatistics.of(view)
        );
        csv.append(e.toCSV()).append(StringUtils.LINEBREAK);
    }

//    @Override
//    public boolean onParsedCommit(Analysis analysis) throws Exception {
//        Logger.info("Processing " + analysis.getCurrentCommitDiff().getCommitHash());
//        return Analysis.Hooks.super.onParsedCommit(analysis);
//    }

    @Override
    public boolean analyzeDiffTree(Analysis analysis) throws Exception {
        final DiffTree d                    = analysis.getCurrentDiffTree();
        final Collection<Relevance> queries = generateRandomRelevances(d);

        for (final Relevance r : queries) {
            runRelevanceExperiment(analysis, d, r);
        }

        return Analysis.Hooks.super.analyzeDiffTree(analysis);
    }

    private List<Relevance> generateRandomRelevances(final DiffTree d) {
        final List<Node>  deselectedPCs = new ArrayList<>();
        final Set<String> features      = new HashSet<>();
        final Set<String> artifacts     = new HashSet<>();

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

        final List<Relevance> relevances = new ArrayList<>(3);
        addRandomRelevance(deselectedPCs, this::randomConfigure,  relevances);
        addRandomRelevance(features,      this::randomTrace,  relevances);
        addRandomRelevance(artifacts,     this::randomSearch, relevances);

        // For debugging:
//        addAll(deselectedPCs, this::allVariantQueries,  queries);
//        addAll(features,      this::allFeatureQueries,  queries);
//        addAll(artifacts,     this::allArtifactQueries, queries);

        return relevances;
    }

    private static <RelevanceParams, RelevanceCandidates extends Collection<RelevanceParams>> void addRandomRelevance(
            RelevanceCandidates source,
            Function<RelevanceCandidates, Relevance> pick,
            Collection<Relevance> target
    ) {
        if (!source.isEmpty()) {
            final Relevance e = pick.apply(source);
            if (e != null) {
                target.add(e);
            }
        }
    }

    private static <RelevanceParams, RelevanceCandidates extends Collection<? extends RelevanceParams>> void addAll(
            RelevanceCandidates source,
            Function<? super RelevanceCandidates, ? extends Collection<? extends Relevance>> prepare,
            Collection<Relevance> target
    ) {
        if (!source.isEmpty()) {
            target.addAll(prepare.apply(source));
        }
    }

    private List<Configure> allConfigureRelevances(final List<Node> deselectedPCs) {
        /*
         * Select a random satisfiable configuration (i.e., a non-false config).
         * Unsatisfiable configs cause empty views which
         * (1) we suspect to be rather useless and thus unused in practice
         * (2) cause a crash in view generation because everything is removed, even the mandatory root.
         */
        final List<Configure> all = new ArrayList<>();
        for (final Node deselectedPC : deselectedPCs) {
            final FixTrueFalse.Formula p = FixTrueFalse.EliminateTrueAndFalseInplace(deselectedPC);
            if (SAT.isSatisfiable(p)) {
                all.add(new Configure(p));
            }
        }

        return all;
    }

    private Configure randomConfigure(final List<Node> deselectedPCs) {
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
        FixTrueFalse.Formula winner = null;
        while (winner == null && !deselectedPCs.isEmpty()) {
            final Node candidate = deselectedPCs.get(random.nextInt(deselectedPCs.size()));
            final FixTrueFalse.Formula fixedCandidate = FixTrueFalse.EliminateTrueAndFalseInplace(candidate);
            if (SAT.isSatisfiable(fixedCandidate)) {
                winner = fixedCandidate;
            } else {
                deselectedPCs.remove(candidate);
            }
        }

        if (winner == null) {
            return null;
        }

        return new Configure(winner);
    }

    private List<Trace> allTraceRelevances(final Set<String> features) {
        return features.stream().map(Trace::new).toList();
    }

    private Trace randomTrace(final Set<String> features) {
        /*
        Pick a random feature for our relevance.
        Since we actually just need a single random value, we could also just pick the first element
        but I don't know if there is any hidden sorting within the set that would bias this choice.
         */
        return new Trace(CollectionUtils.getRandomElement(random, features));
    }

    private List<Search> allSearchRelevances(final Set<String> artifacts) {
        return artifacts.stream().map(Search::new).toList();
    }

    private Search randomSearch(final Set<String> artifacts) {
        /*
        Pick a random artifact for our relevance.
        Since we actually just need a single random value, we could also just pick the first element
        but I don't know if there is any hidden sorting within the set that would bias this choice.
         */
        return new Search(CollectionUtils.getRandomElement(random, artifacts));
    }

    @Override
    public void endBatch(Analysis analysis) throws IOException {
        IO.write(
                FileUtils.addExtension(analysis.getOutputFile(), VIEW_CSV_EXTENSION),
                csv.toString()
        );
    }
}
