package org.variantsync.diffdetective.experiments.thesis_pm;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;

import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.util.FileUtils;

import java.io.IOException;
import org.tinylog.Logger;

public class Main {
    public static void main(String[] args) throws IOException, DiffParseException {
        final Path testDir = Path.of("data", "examples", "test");
        List<Path> inputDiffs = Files.list(testDir).toList();
        List<VariationDiff<DiffLinesLabel>> diffs = new ArrayList<>(inputDiffs.size());

        // for debugging, drop all but the first variant
        inputDiffs = List.of(inputDiffs.get(0));

        for (Path p : inputDiffs) {
            if (FileUtils.hasExtension(p, ".diff")) {
              Logger.info("Loading Diff {}", p);
              diffs.add(VariationDiff.fromFile(p, VariationDiffParseOptions.Default));
            }
        }

        for (VariationDiff<DiffLinesLabel> d : diffs) {
        	for (int i = 0; i < 10; i++) {
	            try {
					Generator.generatePatchScenario(d);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }
    }
}
