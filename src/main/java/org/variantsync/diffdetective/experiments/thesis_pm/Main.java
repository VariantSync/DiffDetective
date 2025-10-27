package org.variantsync.diffdetective.experiments.thesis_pm;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;

import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;
import org.variantsync.diffdetective.variation.diff.transform.RevertSomeChanges;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.show.Show;
import org.variantsync.diffdetective.show.engine.GameEngine;
import org.variantsync.diffdetective.util.FileUtils;

import java.io.IOException;
import org.tinylog.Logger;

public class Main {
    public static void main(String[] args) throws IOException, DiffParseException {
    	
//    	VariationDiff<DiffLinesLabel> d = VariationDiff.fromDiff(
//                """
//                +#ifdef A
//                +  ins
//                +#endif
//                 foo
//                 #if B
//                -  rem
//                   baz
//                   #if C
//                     nono
//                   #endif
//                 #endif
//                -#if Stay
//                -  rem
//                -#endif
//                """
//                , VariationDiffParseOptions.Default
//            );
//            VariationDiff<DiffLinesLabel> pruned = d.deepCopy();
//            new RevertSomeChanges<DiffLinesLabel>(n -> {
//                    String l = n.getLabel().toString();
//                    System.out.println(l);
//                    return l.contains("ins") || l.contains("rem");
//            }).transform(pruned);
//            GameEngine.showAndAwaitAll(
//                Show.diff(d, "original"),
//                Show.diff(pruned, "pruned changes to 'ins' and 'rem'"),
//                Show.tree(pruned.project(Time.BEFORE), "pruned before"),
//                Show.tree(pruned.project(Time.AFTER), "pruned after")
//            );
    	
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
					PatchScenario<DiffLinesLabel> scenario = Generator.generatePatchScenario(d);
					Generator.runPatchers(scenario);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }
    }
}
