package org.variantsync.diffdetective.preliminary.evaluation;

import org.prop4j.Implies;
import org.prop4j.Node;
import org.prop4j.Not;
import org.prop4j.explain.solvers.SatSolver;
import org.prop4j.explain.solvers.SatSolverFactory;
import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.PatchDiff;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.preliminary.pattern.Pattern;
import org.variantsync.diffdetective.preliminary.analysis.GDAnalyzer;
import org.variantsync.diffdetective.preliminary.analysis.data.CommitDiffAnalysisResult;
import org.variantsync.diffdetective.preliminary.analysis.data.GDAnalysisResult;
import org.variantsync.diffdetective.preliminary.analysis.data.PatchDiffAnalysisResult;
import org.variantsync.diffdetective.preliminary.analysis.data.PatternMatch;
import org.variantsync.diffdetective.preliminary.pattern.FeatureContextReverseEngineering;
import org.variantsync.diffdetective.util.IO;

import java.io.*;
import java.util.*;

/**
 * Class for evaluating GDAnalysisResults.
 * <p>
 * This class offers methods for calculating metrics from an analysis result, exporting data, and printing
 * the evaluation.
 */
@Deprecated
public class GDEvaluator {
    private static final String[] CSV_COLUMN_NAMES = {"Commit", "Patch", "Pattern", "Mappings",
            "Start Line", "End Line", "Feature Context"};

    private final GDAnalysisResult analysisResult;
    private final GDAnalyzer<DiffNode> analyzer;
    private final List<PatternMatchEvaluation> pmEvaluations;

    public GDEvaluator(GDAnalyzer<DiffNode> analyzer, GDAnalysisResult analysisResult) {
        this.analyzer = analyzer;
        this.analysisResult = analysisResult;
        this.pmEvaluations = getEvaluations();
    }

    /**
     * Gets a list of PatternMatchEvaluations which each contain a pattern match and its feature
     * context
     *
     * @return The list of PatternMatchEvaluations
     */
    private List<PatternMatchEvaluation> getEvaluations() {
        List<PatternMatchEvaluation> evaluations = new ArrayList<>();
        for (CommitDiffAnalysisResult commitResult :
                analysisResult.getCommitDiffAnalysisResults()) {
            for (PatchDiffAnalysisResult patchResult :
                    commitResult.getPatchDiffAnalysisResults()) {
                for (PatternMatch<DiffNode> patternMatch : patchResult.getPatternMatches()) {
                    PatternMatchEvaluation pme = new PatternMatchEvaluation(commitResult,
                            patchResult, patternMatch, patternMatch.getFeatureContexts());
                    evaluations.add(pme);
                }
            }
        }
        return evaluations;
    }

    /**
     * Gets the total amount of matches for the given patterns
     * @param patterns The patterns to search for
     * @return An int array containing the amount of matches for each pattern
     */
    public <E> int[] getPatternCounts(List<Pattern<E>> patterns) {
        int[] patternCounts = new int[patterns.size()];

        for (PatternMatchEvaluation pme : pmEvaluations) {
            for (int i = 0; i < patterns.size(); i++) {
                if (pme.getPatternMatch().getPattern().getClass() == patterns.get(i).getClass()) {
                    patternCounts[i]++;
                }
            }
        }

        return patternCounts;
    }

    /**
     * Gets the amount of patches that contain a match for the given patterns
     * @param patterns The patterns to search for
     * @return An int array containing the amount of patches tha contain a match for each pattern
     */
    public <E> int[] getPatchesWithPatternCounts(List<FeatureContextReverseEngineering<E>> patterns) {
        int[] patternCounts = new int[patterns.size()];

        for (CommitDiffAnalysisResult cdar : analysisResult.getCommitDiffAnalysisResults()) {
            for (PatchDiffAnalysisResult pdar : cdar.getPatchDiffAnalysisResults()) {
                for (int i = 0; i < patterns.size(); i++) {
                    for (PatternMatch<DiffNode> pm : pdar.getPatternMatches()) {
                        if (pm.getPattern().getClass() == patterns.get(i).getClass()) {
                            patternCounts[i]++;
                            break;
                        }
                    }
                }
            }
        }
        return patternCounts;
    }

    private <E> int[] getLineCounts(List<Pattern<E>> patterns) {
        int[] lineCounts = new int[patterns.size()];

        for (PatternMatchEvaluation pme : pmEvaluations) {
            for (int i = 0; i < patterns.size(); i++) {
                if (pme.getPatternMatch().getPattern().getClass() == patterns.get(i).getClass()) {
                    lineCounts[i] += pme.getPatternMatch().getEndLineDiff() - pme.getPatternMatch().getStartLineDiff();
                }
            }
        }
        return lineCounts;
    }

    public int getUnknownFeatureContextAmount() {
        int amount = 0;
        for (PatternMatchEvaluation pme : pmEvaluations) {
            if (pme.isFeatureContextUnknown()) {
                amount++;
            }
        }
        return amount;
    }

    public int getFeatureContextNullAmount() {
        int amount = 0;
        for (PatternMatchEvaluation pme : pmEvaluations) {
            if (!pme.isFeatureContextUnknown() && pme.canFeatureContextBeNull()) {
                amount++;
            }
        }
        return amount;
    }


    public List<PatchDiff> getPatchDiffsWithPattern(String patternName) {
        List<PatchDiff> patches = new ArrayList<>();

        for (CommitDiffAnalysisResult commitResult :
                analysisResult.getCommitDiffAnalysisResults()) {
            for (PatchDiffAnalysisResult patchResult :
                    commitResult.getPatchDiffAnalysisResults()) {
                for (PatternMatch<DiffNode> patternMatch : patchResult.getPatternMatches()) {
                    if (patternName.equals(patternMatch.getPatternName())) {
                        patches.add(patchResult.getPatchDiff());
                    }
                }
            }
        }
        return patches;
    }

    public List<PatchDiff> getPatchDiffsWithoutPattern() {
        List<PatchDiff> patches = new ArrayList<>();

        for (CommitDiffAnalysisResult commitResult :
                analysisResult.getCommitDiffAnalysisResults()) {
            for (PatchDiffAnalysisResult patchResult :
                    commitResult.getPatchDiffAnalysisResults()) {
                if (patchResult.getPatternMatches().isEmpty()) {
                    patches.add(patchResult.getPatchDiff());
                }
            }
        }
        return patches;
    }

    public List<PatchDiff> getAllPatchDiffs() {
        List<PatchDiff> patches = new ArrayList<>();

        for (CommitDiffAnalysisResult commitResult :
                analysisResult.getCommitDiffAnalysisResults()) {
            for (PatchDiffAnalysisResult patchResult :
                    commitResult.getPatchDiffAnalysisResults()) {
                patches.add(patchResult.getPatchDiff());
            }
        }
        return patches;
    }


    /////////////////////////////////////////////
    /////    FEATURE CONTEXT COMPLEXITY     /////
    /////////////////////////////////////////////

    /**
     * Gets the distribution of the complexities of feature contexts
     *
     * @return Integer array containing the distribution of the complexities of feature contexts
     * (i.e., [3] contains the amount of pattern matches that have a feature complexity of 3)
     */
    public int[] getFeatureContextComplexityAmounts() {
        int[] amounts =
                new int[getMaxFeatureContextComplexityPme().getFeatureContextComplexity() + 1];
        for (PatternMatchEvaluation pme : pmEvaluations) {
            if (!pme.isFeatureContextUnknown()) {
                amounts[pme.getFeatureContextComplexity()]++;
            }
        }
        return amounts;
    }

    public double getAvgFeatureContextComplexity() {
        int sum = 0;
        int amount = 0;
        for (PatternMatchEvaluation pme : pmEvaluations) {
            if (!pme.isFeatureContextUnknown()) {
                sum += pme.getFeatureContextComplexity();
                amount++;
            }
        }
        return (double) sum / amount;
    }

    public PatternMatchEvaluation getMinFeatureContextComplexityPme() {
        int min = Integer.MAX_VALUE;
        PatternMatchEvaluation minPme = null;
        for (PatternMatchEvaluation pme : pmEvaluations) {
            if (!pme.isFeatureContextUnknown() && pme.getFeatureContextComplexity() < min) {
                min = pme.getFeatureContextComplexity();
                minPme = pme;
            }
        }
        return minPme;
    }

    public PatternMatchEvaluation getMaxFeatureContextComplexityPme() {
        int max = Integer.MIN_VALUE;
        PatternMatchEvaluation maxPme = null;
        for (PatternMatchEvaluation pme : pmEvaluations) {
            if (!pme.isFeatureContextUnknown() && pme.getFeatureContextComplexity() > max) {
                max = pme.getFeatureContextComplexity();
                maxPme = pme;
            }
        }
        return maxPme;
    }


    /////////////////////////////////////////////
    /////    DIFFERENT FEATURE CONTEXTS     /////
    /////////////////////////////////////////////

    /**
     * Gets all different feature contexts for each commit
     *
     * @return A map containing a list of different feature contexts for each commit
     */
    public Map<CommitDiffAnalysisResult, List<FeatureContext>> getDifferentFeatureContextsPerCommit() {
        Map<CommitDiffAnalysisResult, List<FeatureContext>> result = new HashMap<>();
        for (CommitDiffAnalysisResult commitResult :
                analysisResult.getCommitDiffAnalysisResults()) {
            result.put(commitResult, getDifferentFeatureContextsFromCommit(commitResult));
        }
        return result;
    }

    /**
     * Gets the amount of different feature contexts for each commit and returns a distribution
     * of the amounts
     *
     * @return Integer array containing the distribution of the amounts of different feature
     * contexts (i.e., [3] contains the amount of commits that have 3 different feature contexts)
     */
    public int[] getDifferentFeatureContextsPerCommitAmounts() {
        List<Integer> amountList = new ArrayList<>();
        int max = 0;
        for (CommitDiffAnalysisResult commitResult :
                analysisResult.getCommitDiffAnalysisResults()) {
            int featureContextAmount = getDifferentFeatureContextsFromCommit(commitResult).size();
            amountList.add(featureContextAmount);
            if (featureContextAmount > max) {
                max = featureContextAmount;
            }
        }
        int[] amounts = new int[max + 1];
        for (int k : amountList) {
            amounts[k]++;
        }

        return amounts;
    }

    /**
     * Calculates the average of different feature contexts per commit
     *
     * @return the average of different feature contexts per commit
     */
    public double getAvgDifferentFeatureContextsPerCommit() {
        long sum = 0;
        for (CommitDiffAnalysisResult commitResult :
                analysisResult.getCommitDiffAnalysisResults()) {
            sum += getDifferentFeatureContextsFromCommit(commitResult).size();
        }
        return sum / (double) analysisResult.getCommitDiffAnalysisResults().size();
    }

    /**
     * Gets all different feature contexts from a single commit
     *
     * @param commitResult The commit from which to get the different feature contexts
     * @return A list of all different feature contexts from a single commit
     */
    public List<FeatureContext> getDifferentFeatureContextsFromCommit(CommitDiffAnalysisResult commitResult) {
        List<FeatureContext[]> allFeatureContexts = new ArrayList<>();
        for (PatternMatchEvaluation pme : pmEvaluations) {
            if (!pme.isFeatureContextUnknown() && pme.getCommit().equals(commitResult)) {
                allFeatureContexts.add(pme.getFeatureContexts());
            }
        }
        return getDifferentFeatureContexts(allFeatureContexts);
    }

    /**
     * Gets all different feature contexts from a list of feature contexts.
     *
     * @param allFeatureContexts The list of feature contexts from which to get the different
     *                           feature contexts
     * @return A list of all different feature contexts from the given list
     */
    public List<FeatureContext> getDifferentFeatureContexts(List<FeatureContext[]> allFeatureContexts) {
        List<FeatureContext[]> allDifferentFeatureContexts = new ArrayList<>();

        // iterate through all fcs
        for (FeatureContext[] currentFeatureContextOptions : allFeatureContexts) {

            boolean added = false;
            // iterate through all currently different fcs
            for (int i = 0; i < allDifferentFeatureContexts.size(); i++) {
                FeatureContext[] differentFeatureContextOptions =
                        allDifferentFeatureContexts.get(i);

                FeatureContext[] overlap =
                        getFeatureContextsOverlap(differentFeatureContextOptions,
                                currentFeatureContextOptions);
                if (overlap != null) {
                    allDifferentFeatureContexts.set(i, overlap);
                    added = true;
                    break;
                }
            }
            if (!added) {
                allDifferentFeatureContexts.add(currentFeatureContextOptions);
            }
        }

        List<FeatureContext> differentFeatureContexts = new ArrayList<>();

        for (FeatureContext[] featureContextOptions : allDifferentFeatureContexts) {
            differentFeatureContexts.add(featureContextOptions[0]);
        }
        return differentFeatureContexts;
    }

    FeatureContext[] getFeatureContextsOverlap(FeatureContext[] fcs1, FeatureContext[] fcs2) {
        if (Arrays.equals(fcs1, fcs2)) {
            return fcs1;
        }

        for (FeatureContext fc1 : fcs1) {
            for (FeatureContext fc2 : fcs2) {
                if (fc1.equals(fc2)) {
                    return new FeatureContext[]{fc1};
                }
                if (fc2.isWeakerOrEqual() && nodeIsWeakerOrEqual(fc1.getNode(), fc2.getNode())) {
                    return new FeatureContext[]{fc1};
                }
                if (fc1.isWeakerOrEqual() && nodeIsWeakerOrEqual(fc2.getNode(), fc1.getNode())) {
                    return new FeatureContext[]{fc2};
                }
            }
        }
        return null;
    }

    private boolean nodeIsWeakerOrEqual(Node weqNode, Node node) {
        if (node == null || weqNode == null) {
            return false;
        }
        return isTautology(new Implies(node, weqNode));
    }

    private boolean isTautology(Node node) {
        return !isSatisfiable(new Not(node));
    }

    private boolean isSatisfiable(Node node) {
        final SatSolver solver = SatSolverFactory.getDefault().getSatSolver();
        solver.addFormulas(node);
        return solver.isSatisfiable();
    }


    /////////////////////////////////////////////
    /////              EXPORTING            /////
    /////////////////////////////////////////////

    /**
     * Exports the results of the evaluation as a .csv-file
     * @param fileName Name of the file to export to
     */
    public void exportEvaluationCsv(String fileName) {

        List<String> commits = new ArrayList<>();
        List<String> patches = new ArrayList<>();
        List<String> patterns = new ArrayList<>();
        List<String> mappings = new ArrayList<>();
        List<Integer> startLines = new ArrayList<>();
        List<Integer> endLines = new ArrayList<>();
        List<String> featureContexts = new ArrayList<>();

        for (PatternMatchEvaluation pme : pmEvaluations) {
            commits.add(pme.getCommit().getCommitDiff().getCommitHash());
            patches.add(pme.getPatch().getPatchDiff().getFileName());
            patterns.add(pme.getPatternMatch().getPatternName());
            if (pme.getPatternMatch().hasFeatureMappings()) {
                StringJoiner sj = new StringJoiner(";");
                for (Node n : pme.getPatternMatch().getFeatureMappings()) {
                    sj.add(n.toString());
                }
                mappings.add(sj.toString());
            } else {
                mappings.add("");
            }
            startLines.add(pme.getPatternMatch().getStartLineDiff());
            endLines.add(pme.getPatternMatch().getEndLineDiff());
            if (!pme.isFeatureContextUnknown()) {
                StringJoiner sj = new StringJoiner(" OR ");
                for (FeatureContext fc : pme.getFeatureContexts()) {
                    sj.add(fc.toString());
                }
                featureContexts.add(sj.toString());
            } else {
                featureContexts.add("");
            }
        }


        try {
            IO.exportCsv(fileName, CSV_COLUMN_NAMES, commits.toArray(), patches.toArray(),
                    patterns.toArray(), mappings.toArray(), startLines.toArray(),
                    endLines.toArray(), featureContexts.toArray());
        } catch (FileNotFoundException e) {
            Logger.warn("Could not save evaluation result to {}", fileName);
        }
    }

    /**
     * Exports the distribution of feature context complexities as a .csv-file
     * @param fileName Name of the file to export to
     */
    public void exportFeatureContextComplexityDistributionCsv(String fileName) {
        String[] COLUMN_NAMES = new String[]{"Complexity", "Amount"};
        Integer[] distribution =
                Arrays.stream(getFeatureContextComplexityAmounts()).boxed().toArray(Integer[]::new);
        Integer[] values = new Integer[distribution.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = i;
        }
        try {
            IO.exportCsv(fileName, COLUMN_NAMES, values, distribution);
        } catch (FileNotFoundException e) {
            Logger.warn("Could not save feature context complexity distribution to {}", fileName);
        }
    }

    /**
     * Exports the distribution of different feature contexts per commit as a .csv-file
     * @param fileName Name of the file to export to
     */
    public void exportDifferentFeatureContextsDistributionCsv(String fileName) {
        String[] COLUMN_NAMES = new String[]{"Different Feature Contexts", "Amount"};
        Integer[] distribution =
                Arrays.stream(getDifferentFeatureContextsPerCommitAmounts()).boxed().toArray(Integer[]::new);
        Integer[] values = new Integer[distribution.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = i;
        }
        try {
            IO.exportCsv(fileName, COLUMN_NAMES, values, distribution);
        } catch (FileNotFoundException e) {
            Logger.warn("Could not save different feature context distribution to {}", fileName);
        }
    }

    /**
     * Export a patch to a file
     * @param patchDiff The patch to export
     * @param fileName The name of the file to export to (usually a txt-file)
     */
    public void exportPatch(PatchDiff patchDiff, String fileName){
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))){
            writer.write(patchDiff.getCommitDiff().getCommitHash() + ", " + patchDiff.getFileName());
            writer.newLine();
            writer.write(patchDiff.getDiff());
            writer.flush();
        }catch(IOException e){
            Logger.warn("Could not export patch {} to {}", patchDiff, fileName);
        }
    }

    /**
     * Export multiple patches to a directory
     * @param directoryName The name of the directory to export to
     * @param patchDiffs The patches to export
     */
    public void exportPatches(String directoryName, List<PatchDiff> patchDiffs){
        File directory = new File(directoryName);
        if (! directory.exists()){
            directory.mkdirs();
        }
        for (int i = 0; i < patchDiffs.size(); i++) {
            PatchDiff p = patchDiffs.get(i);
            exportPatch(p, directoryName + File.separatorChar + (i+1) + "_" + p.getCommitDiff().getAbbreviatedCommitHash() + ".txt");
        }
    }

    /////////////////////////////////////////////
    /////               PRINTING            /////
    /////////////////////////////////////////////

    /**
     * Prints an overview of the evaluation to System.out
     */
    public void printFull() {
        System.out.println("##### START OF FULL EVALUATION RESULTS #####");
        System.out.println();

        System.out.printf("The git repository has %d commits%n",
                analysisResult.getGitDiff().getCommitAmount());
        System.out.printf("GitDiff has %d commitDiffs with %d patches%n",
                analysisResult.getGitDiff().getCommitDiffAmount(),
                analysisResult.getGitDiff().getPatchAmount());
        System.out.printf("%d patches analyzed using %s%n", analysisResult.getPatchAmount(),
                analyzer.getClass().getSimpleName());
        System.out.println();

        System.out.println("## Pattern results ##");
        int[] patternCounts = getPatchesWithPatternCounts(analyzer.getReverseEngineerings());
        int[] patternCountsTotal = getPatternCounts(analyzer.getPatterns());
        int[] lineCounts = getLineCounts(analyzer.getPatterns());
        System.out.printf("%-22s | %-6s | %-6s | %-6s%n", "Pattern", "#patch", "#total", "#lines");
        System.out.println("---------------------------------------------------");
        for (int i = 0; i < analyzer.getPatterns().size(); i++) {
            System.out.printf("%-22s | %-6s | %-6s | %-6s%n",
                    analyzer.getPatterns().get(i).getName(),
                    patternCounts[i],
                    patternCountsTotal[i],
                    lineCounts[i]);
        }
        System.out.printf("%-22s | %-6s%n",
                "NO MATCH",
                getPatchDiffsWithoutPattern().size());
        System.out.println();

        System.out.printf("%d pattern matches found%n", pmEvaluations.size());
        System.out.println();

        System.out.printf("%d unknown feature contexts%n", getUnknownFeatureContextAmount());
        System.out.println();

        System.out.printf("There are %d pattern matches where the feature context could be " +
                "omitted%n", getFeatureContextNullAmount());
        System.out.println();

        System.out.printf("Average feature context complexity: %f%n",
                getAvgFeatureContextComplexity());
        PatternMatchEvaluation maxPme = getMaxFeatureContextComplexityPme();
        if (maxPme != null) {
            System.out.printf("Max feature context complexity: %d (%s)%n",
                    maxPme.getFeatureContextComplexity(),
                    Arrays.toString(maxPme.getFeatureContexts()));
        }
        PatternMatchEvaluation minPme = getMinFeatureContextComplexityPme();
        if (minPme != null) {
            System.out.printf("Min feature context complexity: %d (%s)%n",
                    minPme.getFeatureContextComplexity(),
                    Arrays.toString(minPme.getFeatureContexts()));
        }
        System.out.println();

        // commented out because this takes so much time
//        System.out.printf("Avg different feature contexts per commit: %f%n",
//                getAvgDifferentFeatureContextsPerCommit());
//        System.out.println();

        System.out.println("#####  END OF FULL ANALYSIS RESULTS  #####");
    }

    public void printPatchDiffs(List<PatchDiff> patchDiffs) {
        printPatchDiffs(patchDiffs.toArray(new PatchDiff[0]));
    }

    /**
     * Prints PatchDiffs to System.out
     *
     * @param patchDiffs The PatchDiffs to be printed
     */
    public void printPatchDiffs(PatchDiff... patchDiffs) {
        for (PatchDiff patchDiff : patchDiffs) {
            System.out.println("######################################");
            System.out.println("######       START OF PATCH     ######");
            System.out.println("######################################");

            System.out.printf("patch (%s, %s)%n",
                    patchDiff.getCommitDiff().getAbbreviatedCommitHash(), patchDiff.getFileName());
            System.out.println(patchDiff.getDiff());

            System.out.println("######################################");
            System.out.println("######        END OF PATCH      ######");
            System.out.println("######################################");
        }
    }
}
