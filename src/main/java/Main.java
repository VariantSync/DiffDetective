import analysis.GDAnalysisUtils;
import analysis.GDAnalyzer;
import analysis.TreeGDAnalyzer;
import analysis.data.GDAnalysisResult;
import diff.DiffFilter;
import diff.GitDiffer;
import diff.data.GitDiff;
import evaluation.GDEvaluator;
import load.GitLoader;
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

    public static void main(String[] args) {

        // sets the logging level (TRACE < INFO < DEBUG < WARNING < ERROR)
        Level loggingLevel = Level.DEBUG;

        // use this option with large repositories
        // alternatively, increasing the java heap size also helps :D
        boolean saveMemory = false;

        // specifies where to load the git repository from
        boolean loadFromDir = true;
        String dirName = "Marlin_old";
//        String dirName = "Marlin_remote";

        boolean loadFromZip = false;
        String zipName = "Marlin_old.zip";

        boolean loadFromRemote = false;
        String remoteUri = "https://github.com/MarlinFirmware/Marlin";
//        String remoteUri = "https://github.com/torvalds/linux";
        String remoteRepoName = "marlin_remote"; // name of the folder, where the git repo is cloned to
//        String remoteRepoName = "linux";

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

        // The filter used by the GitDiffer
        DiffFilter diffFilter = new DiffFilter.Builder()
                //.allowBinary(false)
                .allowMerge(false)
                .allowedPaths("Marlin.*")
                .blockedPaths(".*arduino.*")
                .allowedChangeTypes(DiffEntry.ChangeType.MODIFY)
                .allowedFileExtensions("c", "cpp", "h", "pde")
                .build();

        /* ************************ *\
        |      END OF ARGUMENTS      |
        \* ************************ */

        setupLogger(loggingLevel);

        // check arguments
        if (!(loadFromDir || loadFromZip || loadFromRemote)) {
            Logger.error("No git or GitDiff specified");
            System.exit(1);
        }

        if (!(loadFromDir ^ loadFromZip ^ loadFromRemote)) {
            Logger.error("Can't load git or GitDiff from multiple sources");
            System.exit(1);
        }

        // load Git
        Git git = null;
        if (loadFromDir) {
            Logger.info("Loading git from {} ...", dirName);
            git = GitLoader.fromDefaultDirectory(dirName);
        } else if (loadFromZip) {
            Logger.info("Loading git from {} ...", zipName);
            git = GitLoader.fromZip(zipName);
        } else if (loadFromRemote) {
            Logger.info("Loading git from {} ...", remoteUri);
            git = GitLoader.fromRemote(remoteUri, remoteRepoName);
        }

        if (git == null) {
            Logger.error("Failed to load git");
            System.exit(1);
        }

        // create GitDiff
        GitDiff gitDiff = new GitDiffer(git, diffFilter, saveMemory).createGitDiff();
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

    private static void setupLogger(Level loggingLevel) {
        Configurator configurator = Configurator.defaultConfig()
                .writer(new ConsoleWriter(), loggingLevel)
                .formatPattern("{{level}:|min-size=8} {message}");
        configurator.activate();
    }
}
