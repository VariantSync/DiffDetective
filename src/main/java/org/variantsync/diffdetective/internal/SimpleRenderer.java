package org.variantsync.diffdetective.internal;

import org.tinylog.Logger;
import org.variantsync.diffdetective.datasets.PatchDiffParseOptions;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.git.PatchDiff;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.feature.cpp.CPPAnnotationParser;
import org.variantsync.diffdetective.mining.RWCompositePatternNodeFormat;
import org.variantsync.diffdetective.mining.RWCompositePatternTreeFormat;
import org.variantsync.diffdetective.mining.VariationDiffMiner;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.util.FileUtils;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParser;
import org.variantsync.diffdetective.variation.diff.render.RenderOptions;
import org.variantsync.diffdetective.variation.diff.render.VariationDiffRenderer;
import org.variantsync.diffdetective.variation.diff.serialize.nodeformat.MappingsDiffNodeFormat;
import org.variantsync.diffdetective.variation.diff.transform.VariationDiffTransformer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

/**
 * NOT INTENDED FOR API USE.
 * Renderer that may be invoked directly on a directory containing diff
 * and linegraph files. The renderer will render all valid files in the given
 * directory.
 * This class is mostly used for debuggin purposes within DiffDetective and
 * contains mostly quick-and-dirty hardcoded configuration options.
 *
 * @author Paul Bittner
 */
public class SimpleRenderer {
    private static final VariationDiffRenderer renderer = VariationDiffRenderer.WithinDiffDetective();
    private static final RenderOptions<DiffLinesLabel> renderOptions = new RenderOptions.Builder<DiffLinesLabel>()
//            .setNodeFormat(new ReleaseMiningDiffNodeFormat()),
            .setNodeFormat(new MappingsDiffNodeFormat<>())
            .setDpi(RenderOptions.DEFAULT().dpi() / 2)
            .setNodesize(3 * RenderOptions.DEFAULT().nodesize())
            .setEdgesize(2 * RenderOptions.DEFAULT().edgesize())
            .setArrowsize(2 * RenderOptions.DEFAULT().arrowsize())
            .setFontsize(8)
//            .addExtraArguments("--format", "patternsrelease")
            .setCleanUpTemporaryFiles(false)
            .build();
    private static final RenderOptions<DiffLinesLabel> vulkanRenderOptions = new RenderOptions.Builder<DiffLinesLabel>()
//            .setNodeFormat(new ReleaseMiningDiffNodeFormat()),
            .setNodeFormat(new MappingsDiffNodeFormat<>())
            .setDpi(1500)
            .setNodesize(3)
            .setEdgesize(0.1)
            .setArrowsize(2)
            .setFontsize(2)
//            .addExtraArguments("--format", "patternsrelease")
            .setCleanUpTemporaryFiles(false)
            .setWithlabels(false)
            .build();

    private static final RenderOptions<DiffLinesLabel> renderExampleOptions = new RenderOptions.Builder<DiffLinesLabel>()
            .setTreeFormat(new RWCompositePatternTreeFormat())
            .setNodesize(3 * RenderOptions.DEFAULT().nodesize())
            .setEdgesize(2 * RenderOptions.DEFAULT().edgesize())
            .setArrowsize(2 * RenderOptions.DEFAULT().arrowsize())
            .setFontsize(8)
            .addExtraArguments("--startlineno", "4201")
            .build();

    private static final RenderOptions<DiffLinesLabel> renderCompositePatterns = new RenderOptions.Builder<DiffLinesLabel>()
            .setNodesize(3 * RenderOptions.DEFAULT().nodesize())
            .setEdgesize(2 * RenderOptions.DEFAULT().edgesize())
            .setArrowsize(2 * RenderOptions.DEFAULT().arrowsize())
            .setFontsize(2 * RenderOptions.DEFAULT().fontsize())
            .setTreeFormat(new RWCompositePatternTreeFormat())
            .setNodeFormat(new RWCompositePatternNodeFormat())
            .setCleanUpTemporaryFiles(true)
            .addExtraArguments("--format", "patternsdebug")
            .build();

    private static final RenderOptions<DiffLinesLabel> RENDER_OPTIONS_TO_USE = renderExampleOptions;

    private final static boolean collapseMultipleCodeLines = true;
    private final static boolean ignoreEmptyLines = true;
    private final static List<String> SUPPORTED_FILE_TYPES = List.of(".diff", ".c", ".cpp", ".h", ".hpp");

    private final static Function<Path, Path> GetRelativeOutputDir =
//            Path::getParent
            p -> p.getParent().resolve("render");

    private static void render(final Path fileToRender) {
        if (FileUtils.isLineGraph(fileToRender)) {
            Logger.info("Rendering {}", fileToRender);
            renderer.renderFile(fileToRender, RENDER_OPTIONS_TO_USE);
        } else if (SUPPORTED_FILE_TYPES.stream().anyMatch(extension -> FileUtils.hasExtension(fileToRender, extension))) {
            Logger.info("Rendering {}", fileToRender);
            final VariationDiff<DiffLinesLabel> t;
            try {
                t = VariationDiff.fromFile(fileToRender,
                        new VariationDiffParseOptions(
                                CPPAnnotationParser.Default, collapseMultipleCodeLines, ignoreEmptyLines
                        ));
            } catch (IOException | DiffParseException e) {
                Logger.error(e, "Could not read given file '{}'", fileToRender);
                return;
            }
            renderer.renderWithTreeFormat(
                    t,
                    fileToRender.getFileName().toString(),
                    GetRelativeOutputDir.apply(fileToRender),
//                    renderExampleOptions
                    RENDER_OPTIONS_TO_USE
            );
        } else {
            Logger.warn("Skipping unsupported file {}", fileToRender);
        }

    }

    /**
     * Expects one of the following argument configurations.
     * 1.) For rendering files: Exactly one argument pointing to a file or directory to render.
     * 2.) For rendering patches: Exactly three arguments.
     * The first argument is the path to a local directory from which a patch should be analyzed.
     * The second argument is a commit hash.
     * The third argument is the file name of the patched file in the given commit.
     *
     * @param args See above
     * @throws IOException when reading a file fails.
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Expected either a path to diff or lg file as argument, or a path to a git repository and a commit hash.");
            return;
        }

        if (args.length == 1) {
            final Path fileToRender = Path.of(args[0]);

            if (!Files.exists(fileToRender)) {
                Logger.error("Path {} does not exist!", fileToRender);
                return;
            }

            if (Files.isDirectory(fileToRender)) {
                Logger.info("Rendering directory {}", fileToRender);
                FileUtils.listAllFilesRecursively(fileToRender).forEach(SimpleRenderer::render);
            } else {
                Logger.info("Rendering file {}", fileToRender);
                render(fileToRender);
            }
        } else if (args.length == 3) {
            final Path repoPath = Path.of(args[0]);
            final String repoName = repoPath.getFileName().toString();
            final String commit = args[1];
            final String file = args[2];

            final Repository repository = Repository.fromDirectory(repoPath, repoName);
            repository.setParseOptions(repository.getParseOptions().withDiffStoragePolicy(PatchDiffParseOptions.DiffStoragePolicy.REMEMBER_STRIPPED_DIFF));

            final List<VariationDiffTransformer<DiffLinesLabel>> transform = VariationDiffMiner.Postprocessing(repository);
            final PatchDiff patch = VariationDiffParser.parsePatch(repository, file, commit);
            Assert.assertNotNull(patch != null);
            VariationDiffTransformer.apply(transform, patch.getVariationDiff());
            renderer.render(patch, Path.of("render", repoName), RENDER_OPTIONS_TO_USE);
        }

        System.out.println("done");
    }
}
