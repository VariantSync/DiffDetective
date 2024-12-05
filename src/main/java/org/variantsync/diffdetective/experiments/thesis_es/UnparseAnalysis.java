package org.variantsync.diffdetective.experiments.thesis_es;

import java.io.IOException;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.diff.git.PatchDiff;
import org.variantsync.diffdetective.util.CSV;
import org.variantsync.diffdetective.util.FileUtils;
import org.variantsync.diffdetective.util.IO;
import org.variantsync.diffdetective.util.StringUtils;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.VariationUnparser;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.construction.JGitDiff;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.diffdetective.variation.tree.source.VariationTreeSource;

public class UnparseAnalysis implements Analysis.Hooks {

    public static final String CSV_EXTENSION = ".thesis_es.csv";

    public static int count = 0;

    private StringBuilder csv;

    @Override
    public void initializeResults(Analysis analysis) {
        Analysis.Hooks.super.initializeResults(analysis);

        csv = new StringBuilder();
        csv.append(UnparseEvaluation.makeHeader(CSV.DEFAULT_CSV_DELIMITER)).append(StringUtils.LINEBREAK);
    }

    @Override
    public boolean analyzeVariationDiff(Analysis analysis) throws Exception {
        PatchDiff patch = analysis.getCurrentPatch();
        String textDiff = patch.getDiff();
        String codeBefore = "";
        String codeAfter = "";
        /*
        if (count < 10) {
            Path pathTreeBefore = Paths.get("results", "thesis_es", "treeBefore" + count + ".txt");
            Path pathTreeAfter = Paths.get("results", "thesis_es", "treeAfter" + count + ".txt");
            Path pathDiff = Paths.get("results", "thesis_es", "diff" + count + ".txt");
            Files.writeString(pathTreeBefore, codeBefore);
            Files.writeString(pathTreeAfter, codeAfter);
            Files.writeString(pathDiff, textDiff);
            count++;
        }
        */

        codeBefore = VariationUnparser.undiff(patch.getDiff(), Time.BEFORE);
        codeAfter = VariationUnparser.undiff(patch.getDiff(), Time.AFTER);
        // codeBefore = codeBefore.replaceAll("\\r\\n", "\n");
        // codeAfter = codeAfter.replaceAll("\\r\\n", "\n");
        boolean[][] diffTestAll = runTestsDiff(textDiff);
        boolean[] treeBeforeTest = runTestsTree(codeBefore);
        boolean[] treeAfterTest = runTestsTree(codeAfter);
        boolean[] dataTests = runDataTest(textDiff, codeBefore, codeAfter);
        int error = 1;
        String[] errorSave = new String[] { "", "", "" };
        if (!(boolOr(diffTestAll[0]) || boolOr(diffTestAll[1]))) {
            error = error * 2;
            // errorSave[0] = ;
        }
        if (!boolOr(treeBeforeTest)) {
            error = error * 3;
            errorSave[1] = codeBefore;
        }
        if (!boolOr(treeAfterTest)) {
            error = error * 5;
            // errorSave[2] = codeAfter;
        }
        /*
        if (!boolOr(dataTests)) {
            error = error * 7;
        }
        */

        final UnparseEvaluation ue = new UnparseEvaluation(
                boolToInt(dataTests),
                boolToInt(diffTestAll[0]),
                boolToInt(diffTestAll[1]),
                boolToInt(treeBeforeTest),
                boolToInt(treeAfterTest),
                error,
                errorSave);
        csv.append(ue.toCSV()).append(StringUtils.LINEBREAK);
        return Analysis.Hooks.super.analyzeVariationDiff(analysis);
    }

    @Override
    public void endBatch(Analysis analysis) throws IOException {
        IO.write(
                FileUtils.addExtension(analysis.getOutputFile(), CSV_EXTENSION),
                csv.toString());
    }

    public static String removeWhitespace1(String string) {
        string = string.replaceAll("\n(\\s*\n)+", "\n");
        string = string.replaceAll("\n\\s+", "\n");
        if (!string.isEmpty()) {
            if (string.charAt(string.length() - 1) == '\n') {
                return string.substring(0, string.length() - 1);
            }
        }
        return string;
    }

    public static String removeWhitespace(String string) {
        if (string.isEmpty()) {
            return "";
        } else {
            StringBuilder result = new StringBuilder();
            for (String line : string.split("\n")) {
                if (!line.replaceAll("\\s+", "").isEmpty()) {
                    result.append(line.trim());
                    result.append("\n");
                }
            }
            return result.substring(0, result.length() - 1);
        }
    }

    public static String parseUnparseTree(String text, VariationDiffParseOptions option) {
        String temp = "b";
        try {
            VariationTree<DiffLinesLabel> tree = VariationTree.fromText(text, VariationTreeSource.Unknown, option);
            temp = VariationUnparser.variationTreeUnparser(tree);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return temp;
    }

    public static String parseUnparseDiff(String textDiff, VariationDiffParseOptions option) {
        String temp = "b";
        try {
            VariationDiff<DiffLinesLabel> diff = VariationDiff.fromDiff(textDiff, option);
            temp = VariationUnparser.variationDiffUnparser(diff);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return temp;
    }

    public static VariationDiffParseOptions optionsSetter(int i) {
        if (i == 0) {
            return new VariationDiffParseOptions(false, false);
        } else if (i == 1) {
            return new VariationDiffParseOptions(true, false);
        } else if (i == 2) {
            return new VariationDiffParseOptions(false, true);
        } else {
            return new VariationDiffParseOptions(true, true);
        }
    }

    public static boolean equalsText(String text1, String text2, boolean whitespace) {
        if (whitespace) {
            return text1.equals(text2);
        } else {
            return removeWhitespace(text1).equals(removeWhitespace(text2));
        }
    }

    public static boolean[][] runTestsDiff(String text) {
        boolean[][] array = new boolean[2][8];
        for (int i = 0; i < 4; i++) {
            String diff = parseUnparseDiff(text, optionsSetter(i));
            array[0][i] = equalsText(text, diff, true);
            array[0][i + 4] = equalsText(text, diff, false);
            array[1][i] = (equalsText(VariationUnparser.undiff(text, Time.BEFORE),
                    VariationUnparser.undiff(diff, Time.BEFORE), true)
                    && equalsText(VariationUnparser.undiff(text, Time.AFTER),
                            VariationUnparser.undiff(diff, Time.AFTER), true))
                    || (equalsText(VariationUnparser.undiff(text, Time.BEFORE),
                            VariationUnparser.undiff(diff, Time.BEFORE), false)
                            && equalsText(VariationUnparser.undiff(text, Time.AFTER),
                                    VariationUnparser.undiff(diff, Time.AFTER), false));
            array[1][i + 4] = false;
        }
        return array;
    }

    public static boolean[] runTestsTree(String text) {
        boolean[] array = new boolean[8];
        for (int i = 0; i < 4; i++) {
            String temp = parseUnparseTree(text, optionsSetter(i));
            array[i] = equalsText(text, temp, true);
            array[i + 4] = equalsText(text, temp, false);
        }
        return array;
    }

    public static boolean[] runDataTest(String textDiff, String treeBefore, String treeAfter) throws IOException {
        boolean[] array = new boolean[8];
        array[0] = JGitDiff.textDiff(treeBefore, treeAfter, SupportedAlgorithm.MYERS).equals(textDiff);
        array[1] = removeWhitespace(JGitDiff.textDiff(treeBefore, treeAfter, SupportedAlgorithm.MYERS))
                .equals(removeWhitespace(textDiff));
        array[2] = JGitDiff.textDiff(treeBefore, treeAfter, SupportedAlgorithm.HISTOGRAM).equals(textDiff);
        array[3] = removeWhitespace(JGitDiff.textDiff(treeBefore, treeAfter, SupportedAlgorithm.HISTOGRAM))
                .equals(removeWhitespace(textDiff));
        array[4] = treeBefore.equals(VariationUnparser.undiff(textDiff, Time.BEFORE));
        array[5] = removeWhitespace(treeBefore)
                .equals(removeWhitespace(VariationUnparser.undiff(textDiff, Time.BEFORE)));
        array[6] = treeAfter.equals(VariationUnparser.undiff(textDiff, Time.AFTER));
        array[7] = removeWhitespace(treeAfter).equals(removeWhitespace(VariationUnparser.undiff(textDiff, Time.AFTER)));
        return array;
    }

    public static int[] boolToInt(boolean[] array) {
        int[] temp = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            if (array[i]) {
                temp[i] = 1;
            } else {
                temp[i] = 0;
            }
        }
        return temp;
    }

    public static boolean boolOr(boolean[] array) {
        for (boolean temp : array) {
            if (temp) {
                return true;
            }
        }
        return false;
    }

}
