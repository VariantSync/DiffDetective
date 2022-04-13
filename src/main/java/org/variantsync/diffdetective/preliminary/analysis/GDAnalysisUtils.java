package org.variantsync.diffdetective.preliminary.analysis;

import org.prop4j.Node;
import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.preliminary.analysis.data.CommitDiffAnalysisResult;
import org.variantsync.diffdetective.preliminary.analysis.data.GDAnalysisResult;
import org.variantsync.diffdetective.preliminary.analysis.data.PatchDiffAnalysisResult;
import org.variantsync.diffdetective.preliminary.analysis.data.PatternMatch;
import org.variantsync.diffdetective.util.IO;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Utility class for GDAnalysisResults.
 * Currently only used for exporting the analysis
 *
 * @author Sören Viegener
 */
@Deprecated
public class GDAnalysisUtils {
    private static final String[] CSV_COLUMN_NAMES = {"Commit", "Patch", "Pattern", "Mappings",
            "Start Line", "End Line"};

    /**
     * Exports a GDAnalysisResult to a csv-file
     *
     * @param analysisResult The GDAnalysisResult to be exported
     * @param fileName           The name of the file to export to (usually a .csv-file)
     */
    public static void exportCsv(GDAnalysisResult analysisResult, String fileName) {

        List<String> commits = new ArrayList<>();
        List<String> patches = new ArrayList<>();
        List<String> patterns = new ArrayList<>();
        List<String> mappings = new ArrayList<>();
        List<Integer> startLines = new ArrayList<>();
        List<Integer> endLines = new ArrayList<>();
        for (CommitDiffAnalysisResult commitResult :
                analysisResult.getCommitDiffAnalysisResults()) {
            for (PatchDiffAnalysisResult patchResult :
                    commitResult.getPatchDiffAnalysisResults()) {
                for (PatternMatch<DiffNode> patternMatch : patchResult.getPatternMatches()) {
                    commits.add(commitResult.getCommitDiff().getCommitHash());
                    patches.add(patchResult.getPatchDiff().getFileName());
                    patterns.add(patternMatch.getPatternName());
                    if (patternMatch.hasFeatureMappings()) {
                        StringJoiner sj = new StringJoiner(";");
                        for (Node n : patternMatch.getFeatureMappings()) {
                            sj.add(n.toString());
                        }
                        mappings.add(sj.toString());
                    }else{
                        mappings.add("");
                    }
                    startLines.add(patternMatch.getStartLineDiff());
                    endLines.add(patternMatch.getEndLineDiff());
                }
            }
        }

        try {
            IO.exportCsv(fileName, CSV_COLUMN_NAMES, commits.toArray(), patches.toArray(),
                    patterns.toArray(), mappings.toArray(), startLines.toArray(), endLines.toArray());
        } catch (FileNotFoundException e) {
            Logger.warn("Could not save analysis result to {}", fileName);
        }
    }
}
