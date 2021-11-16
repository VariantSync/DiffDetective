package main;
import analysis.GDAnalysisUtils;
import analysis.GDAnalyzer;
import analysis.TreeGDAnalyzer;
import analysis.data.GDAnalysisResult;
import datasets.LoadingParameter;
import datasets.Repository;
import diff.DiffFilter;
import diff.GitDiffer;
import diff.GitDiff;
import evaluation.GDEvaluator;
import load.GitLoader;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.writers.ConsoleWriter;

/**
 * The main class used to run DiffDetective
 *
 * @author Sören Viegener
 */
public class Main {
    private static final String TREE_ANALYSIS = "tree";
    private static final String ATOMIC_TREE_ANALYSIS = "tree_atomic";
    private static final String SEMANTIC_TREE_ANALYSIS = "tree_semantic";

    // The filter used by the GitDiffer
    public static final DiffFilter DefaultDiffFilterForMarlin = new DiffFilter.Builder()
            //.allowBinary(false)
            .allowMerge(false)
            .allowedPaths("Marlin.*")
            .blockedPaths(".*arduino.*")
            .allowedChangeTypes(DiffEntry.ChangeType.MODIFY)
            .allowedFileExtensions("c", "cpp", "h", "pde")
            .build();

    public static void main(String[] args) {

        // sets the logging level (TRACE < INFO < DEBUG < WARNING < ERROR)
        Level loggingLevel = Level.DEBUG;

        Repository repo = null;
        
        // Create Marlin Repo
		repo = Repository.createMarlinZipRepo();

        // which analyzer will be used
        String analysisName = ATOMIC_TREE_ANALYSIS;

        // whether to exit either directly before the analysis or the evaluation
        boolean noAnalysis = false;
        boolean noEvaluation = false;

        // whether to export the analysis result to a csv file
        boolean exportAnalysis = false;
        String exportAnalysisFilename = "marlin_old_analysis.csv";

        // whether to export the evaluation result to a csv file
        boolean exportEvaluation = false;
        String exportEvaluationFilename = "marlin_old_evaluation.csv";

        // whether to print the results of the evaluation
        boolean printEvaluationResults = true;

        /* ************************ *\
        |      END OF ARGUMENTS      |
        \* ************************ */

        setupLogger(loggingLevel);

        // load Git
        Git git = GitLoader.loadRepository(repo);
        if (git == null) {
            Logger.error("Failed to load git.\nExiting program.");
            System.exit(1);
        }

        // create GitDiff
        GitDiff gitDiff = new GitDiffer(git, DefaultDiffFilterForMarlin, repo.shouldSaveMemory()).createGitDiff();
        if (gitDiff == null) {
            Logger.error("Failed to create GitDiff");
            System.exit(1);
        }

        Logger.info("GitDiff has {} commits and {} patches%n", gitDiff.getCommitAmount(),
                gitDiff.getPatchAmount());

        if (noAnalysis) {
            System.exit(0);
        }


        GDAnalyzer analyzer = null;

        switch (analysisName) {
            case TREE_ANALYSIS:
                analyzer = new TreeGDAnalyzer(gitDiff);
                break;
            case ATOMIC_TREE_ANALYSIS:
                analyzer = new TreeGDAnalyzer(gitDiff, true, false);
                break;
            case SEMANTIC_TREE_ANALYSIS:
                analyzer = new TreeGDAnalyzer(gitDiff, false, true);
                break;
            default:
                Logger.error("GDAnalyzer \"{}\" does not exist", analysisName);
                System.exit(1);
                break;
        }

        GDAnalysisResult result = analyzer.analyze();


        // export analysis as csv
        if (exportAnalysis) {
            Logger.info("Exporting analysis ...");
            GDAnalysisUtils.exportCsv(result, exportAnalysisFilename);
            Logger.info("Analysis exported to {}", exportAnalysisFilename);
        }

        if (noEvaluation) {
            System.exit(0);
        }

        GDEvaluator evaluator = new GDEvaluator(analyzer, result);

        if(exportEvaluation){
            Logger.info("Exporting evaluation ...");
            evaluator.exportEvaluationCsv(exportEvaluationFilename);
            Logger.info("Evaluation exported to {}", exportEvaluationFilename);
        }

        if(printEvaluationResults){
            evaluator.printFull();
        }


        /*
        Map<CommitDiffAnalysisResult, List<FeatureContext>> differentFeatureContexts = evaluator
        .getDifferentFeatureContextsPerCommit();

        int[] modAmounts = new int[171];
        int[] diffAmounts = new int[171];
        CommitDiffAnalysisResult maxCommit = null;
        int max = Integer.MIN_VALUE;
        for(CommitDiffAnalysisResult c : differentFeatureContexts.keySet()){
            int diffAmount = differentFeatureContexts.get(c).size();
            for(PatchDiffAnalysisResult p : c.getPatchDiffAnalysisResults()){
                for(PatternMatch pm : p.getPatternMatches()){
                    if(pm.getPattern() instanceof AddWithMappingAtomicPattern || pm.getPattern()
                    instanceof RemWithMapping ||
                            pm.getPattern() instanceof AddToPCAtomicPattern || pm.getPattern()
                            instanceof RemFromPCAtomicPattern){
                        modAmounts[diffAmount] += pm.getEndLineDiff()-pm.getStartLineDiff();
                    }
                }
            }
            diffAmounts[diffAmount] ++;

            if(diffAmount > max){
                max = diffAmount;
                maxCommit = c;
            }
        }

        System.out.printf("max: %d%n", max);
        System.out.println(maxCommit.getCommitDiff().getAbbreviatedCommitHash());
        System.out.println(differentFeatureContexts.get(maxCommit));

        for (int i = 1; i < modAmounts.length; i++) {
            if(diffAmounts[i] != 0) {
                System.out.printf("%d, %d, %d, %d%n", i, diffAmounts[i], modAmounts[i] /
                diffAmounts[i], modAmounts[i]);
            }
        }

        System.out.println(Arrays.stream(modAmounts).sum());


        System.out.println(evaluator.getMaxFeatureContextComplexityPme().getPatch().getPatchDiff());


         */


//        evaluator.exportPatches("patches", evaluator.getPatchDiffsWithoutPattern());

//        evaluator.exportEvaluationCsv("semantic_pattern_eval.csv");
//        evaluator.exportDifferentFeatureContextsDistributionCsv("diff_fc.csv");
//        evaluator.exportFeatureContextComplexityDistributionCsv("semantic_fc_complexity.csv");
    }

    public static void setupLogger(Level loggingLevel) {
        Configurator configurator = Configurator.defaultConfig()
                .writer(new ConsoleWriter(), loggingLevel)
                .formatPattern("{{level}:|min-size=8} {message}");
        configurator.activate();
    }
}
