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

/**
 * Implementation of the feasibility study from Section 6 of our paper
 * Views on Edits to Variational Software
 * at SPLC'23.
 * This Analysis is run on a batch of commits of a single repository.
 * For the whole feasibility study, multiple analysis are run in parallel, each
 * processing a part of a repositories history.
 */
public class ViewAnalysis implements Analysis.Hooks {
    /**
     * File extension for csv files created by this analysis.
     */
    public static final String VIEW_CSV_EXTENSION = ".views.csv";
    /**
     * StringBuilder that is used to iteratively create a csv file with this analysis' results.
     */
    private StringBuilder csv;
    /**
     * Random instance to generate random numbers.
     * Used to generate random relevance predicates.
     */
    private Random random;

    @Override
    public void initializeResults(Analysis analysis) {
        Analysis.Hooks.super.initializeResults(analysis);

        random = new Random();

        csv = new StringBuilder();
        csv.append(ViewEvaluation.makeHeader(CSV.DEFAULT_CSV_DELIMITER)).append(StringUtils.LINEBREAK);
    }

    /**
     * Benchmark for view generation on the given variation diff with the given relevance.
     * This method generates a view once with each algorithm:
     * - once with the {@link DiffView#naive(DiffTree, Relevance) naive algorithm} view_naive (Equation 8 from our paper),
     * - and once with the {@link DiffView#optimized(DiffTree, Relevance) optimized algorithm} view_smart (Equation 10 in our paper).
     * This method measures both algorithms runtimes and stores the runtimes and metadata in terms of a
     * {@link ViewEvaluation} in this objects {@link #csv} field.
     * @param analysis The current instance of the analysis that is run.
     *                 Used to access metadata of the current commit that is processed.
     * @param d The variation diff to benchmark view generation on.
     * @param rho A relevance predicate that determines which nodes should be contained in the view.
     */
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
                analysis.getCurrentPatch().getFileName(Time.AFTER),
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

    /**
     * Runs the feasibility study on the current variation diff.
     * Creates random relevance predicates as explained in Section 6 of our paper.
     * Then runs {@link #runRelevanceExperiment(Analysis, DiffTree, Relevance)} for each relevance on the
     * current variation diff.
     * @param analysis The current instance of the analysis that is run.
     *                 Used to access metadata of the current commit that is processed.
     * @return {@link Analysis.Hooks#analyzeDiffTree(Analysis)}
     * @throws Exception
     */
    @Override
    public boolean analyzeDiffTree(Analysis analysis) throws Exception {
        final DiffTree d                    = analysis.getCurrentDiffTree();
        final Collection<Relevance> queries = generateRandomRelevances(d);

        for (final Relevance r : queries) {
            runRelevanceExperiment(analysis, d, r);
        }

        return Analysis.Hooks.super.analyzeDiffTree(analysis);
    }

    /**
     * Generates random relevance predicates for creating random views on
     * the given variation diff d, as explained in Section 6.1 in our paper.
     * If possible, this method will generate one relevance of each type of
     * {@link Configure}, {@link Trace}, and {@link Search}.
     * @param d The variation diff to generate relevance predicates for.
     * @return A list of three random relevance predicates.
     */
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
    
    /**
     * This is a convenience method for creating a random relevance predicate from a collection of
     * potential parameterisations.
     * <p>
     * If the given collection {@code source} of parameters for relevance predicates is not empty, this method
     * picks a random parameterization and creates a relevance predicate from it.
     * The created predicate is added to the given target collection.
     * @param source A potentially empty collection of arguments for relevance predicates.
     * @param pick A function that picks a parameterisation at random and creates a relevance predicate from it.
     * @param target A collection to which the randomly created relevance predicate will be added to.
     * @param <RelevanceParams> The type of the parameters for the relevance predicates.
     * @param <RelevanceCandidates> The type of collection of the parameters.
     */
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

    /**
     * This is a convenience method for creating relevance predicates from their parameters.
     * <p>
     * If the given collection of parameters for relevance predicates is not empty, this method
     * generates corresponding relevance predicates for all those predicates and adds them to the given target collection.
     * @param source A potentially empty collection of arguments for relevance predicates.
     * @param prepare A function that creates a relevance predicate from suitable arguments.
     * @param target A collection to which all relevance predicates will be added to.
     * @param <RelevanceParams> The type of the parameters for the relevance predicates.
     * @param <RelevanceCandidates> The type of collection of the parameters.
     */
    private static <RelevanceParams, RelevanceCandidates extends Collection<? extends RelevanceParams>> void addAll(
            RelevanceCandidates source,
            Function<? super RelevanceCandidates, ? extends Collection<? extends Relevance>> prepare,
            Collection<Relevance> target
    ) {
        if (!source.isEmpty()) {
            target.addAll(prepare.apply(source));
        }
    }

    /**
     * Generates all {@link Configure} relevance predicates that can be generated from the given list of
     * deselected presence conditions.
     * The returned list of predicates is complete: For every (partial) variant of the variation tree or diff the
     * deselected presence conditions come from, a corresponding relevance predicate is within the returned list.
     * @param deselectedPCs A list of negations of all presence conditions that occur in a variation tree or diff.
     * @return A complete list of {@link Configure} predicates.
     */
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
    
    /**
     * Creates a random {@link Configure} relevance predicate from the given list of
     * deselected presence conditions.
     * This method picks a random satisfiable formula from the given list and returns it as a relevance predicate
     * for configuration.
     * @return A random, satisfiable {@link Configure} predicate, created from the given presence conditions.
     *         Null if the given list is empty or if it does not contain a satisfiable formula.
     */
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
    
    /**
     * Generates a {@link Trace} relevance predicate for each feature in the given set of feature names.
     * @param features A set of feature names to trace.
     * @return A {@link Trace} predicate for each feature name in the given set.
     */
    private List<Trace> allTraceRelevances(final Set<String> features) {
        return features.stream().map(Trace::new).toList();
    }

    /**
     * Creates a random {@link Trace} relevance predicate from the given set of feature names.
     * @param features A set of feature names.
     * @return A {@link Trace} predicate for a feature name randomly picked from the given set.
     */
    private Trace randomTrace(final Set<String> features) {
        /*
        Pick a random feature for our relevance.
        Since we actually just need a single random value, we could also just pick the first element
        but I don't know if there is any hidden sorting within the set that would bias this choice.
         */
        return new Trace(CollectionUtils.getRandomElement(random, features));
    }

    /**
     * Generates a {@link Search} relevance predicate for each artifact in the given set.
     * @param artifacts A list of text-based artifacts.
     * @return A {@link Search} predicate for each artifact.
     */
    private List<Search> allSearchRelevances(final Set<String> artifacts) {
        return artifacts.stream().map(Search::new).toList();
    }

    /**
     * Creates a random {@link Search} relevance predicate from the given set of artifacts.
     * @param artifacts A set of text-based artifacts (e.g., lines of code).
     * @return A {@link Search} predicate for an artifact randomly picked from the given set.
     */
    private Search randomSearch(final Set<String> artifacts) {
        /*
        Pick a random artifact for our relevance.
        Since we actually just need a single random value, we could also just pick the first element
        but I don't know if there is any hidden sorting within the set that would bias this choice.
         */
        return new Search(CollectionUtils.getRandomElement(random, artifacts));
    }

    /**
     * Writes the results of this analysis to disk as CSV file.
     * @param analysis The current state of the analysis.
     * @throws IOException When the file cannot be created or written.
     */
    @Override
    public void endBatch(Analysis analysis) throws IOException {
        IO.write(
                FileUtils.addExtension(analysis.getOutputFile(), VIEW_CSV_EXTENSION),
                csv.toString()
        );
    }
}
