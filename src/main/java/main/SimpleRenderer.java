package main;

import datasets.ParseOptions;
import datasets.Repository;
import diff.PatchDiff;
import diff.difftree.DiffTree;
import diff.difftree.parse.DiffNodeParser;
import diff.difftree.parse.DiffTreeParser;
import diff.difftree.render.DiffTreeRenderer;
import diff.difftree.render.RenderOptions;
import diff.difftree.serialize.nodeformat.MappingsDiffNodeFormat;
import diff.difftree.transform.DiffTreeTransformer;
import mining.DiffTreeMiner;
import mining.RWCompositePatternNodeFormat;
import mining.RWCompositePatternTreeFormat;
import org.tinylog.Logger;
import util.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

public class SimpleRenderer {
    private static final DiffTreeRenderer renderer = DiffTreeRenderer.WithinDiffDetective();
    private static final RenderOptions renderOptions = new RenderOptions.Builder()
//            .setNodeFormat(new ReleaseMiningDiffNodeFormat()),
            .setNodeFormat(new MappingsDiffNodeFormat())
            .setDpi(RenderOptions.DEFAULT.dpi() / 2)
            .setNodesize(3*RenderOptions.DEFAULT.nodesize())
            .setEdgesize(2*RenderOptions.DEFAULT.edgesize())
            .setArrowsize(2*RenderOptions.DEFAULT.arrowsize())
            .setFontsize(8)
//            .addExtraArguments("--format", "patternsrelease")
            .setCleanUpTemporaryFiles(false)
            .build();
    private static final RenderOptions vulkanRenderOptions = new RenderOptions.Builder()
//            .setNodeFormat(new ReleaseMiningDiffNodeFormat()),
            .setNodeFormat(new MappingsDiffNodeFormat())
            .setDpi(1500)
            .setNodesize(3)
            .setEdgesize(0.1)
            .setArrowsize(2)
            .setFontsize(2)
//            .addExtraArguments("--format", "patternsrelease")
            .setCleanUpTemporaryFiles(false)
            .setWithlabels(false)
            .build();

    private static final RenderOptions renderExampleOptions = new RenderOptions.Builder()
            .setNodesize(3*RenderOptions.DEFAULT.nodesize())
            .setEdgesize(2*RenderOptions.DEFAULT.edgesize())
            .setArrowsize(2*RenderOptions.DEFAULT.arrowsize())
            .setFontsize(8)
            .addExtraArguments("--startlineno", "4201")
            .build();

    private static final RenderOptions renderCompositePatterns = new RenderOptions.Builder()
            .setNodesize(3*RenderOptions.DEFAULT.nodesize())
            .setEdgesize(2*RenderOptions.DEFAULT.edgesize())
            .setArrowsize(2*RenderOptions.DEFAULT.arrowsize())
            .setFontsize(2*RenderOptions.DEFAULT.fontsize())
            .setTreeFormat(new RWCompositePatternTreeFormat())
            .setNodeFormat(new RWCompositePatternNodeFormat())
            .setCleanUpTemporaryFiles(true)
            .addExtraArguments("--format", "patternsdebug")
            .build();

    private final static boolean collapseMultipleCodeLines = true;
    private final static boolean ignoreEmptyLines = true;
    private final static List<String> SUPPORTED_FILE_TYPES = List.of(".diff", ".c", ".cpp", ".h", ".hpp");

    private final static Function<Path, Path> GetRelativeOutputDir =
//            Path::getParent
            p -> p.getParent().resolve("render")
            ;

    private static void render(final Path fileToRender) {
        final String fileToRenderStr = fileToRender.toString();
        if (fileToRenderStr.endsWith(".lg")) {
            Logger.info("Rendering " + fileToRender);
            renderer.renderFile(fileToRender, vulkanRenderOptions);
        } else if (SUPPORTED_FILE_TYPES.stream().anyMatch(fileToRenderStr::endsWith)) {
            Logger.info("Rendering " + fileToRender);
            final DiffTree t;
            try {
                t = DiffTree.fromFile(fileToRender, collapseMultipleCodeLines, ignoreEmptyLines, DiffNodeParser.Default).unwrap().getSuccess();
            } catch (IOException e) {
                System.err.println("Could not read given file \"" + fileToRender + "\" because:\n" + e.getMessage());
                return;
            }
            renderer.renderWithTreeFormat(
                    t,
                    fileToRender.getFileName().toString(),
                    GetRelativeOutputDir.apply(fileToRender),
//                    renderExampleOptions
                    renderCompositePatterns
            );
        } else {
            Logger.warn("Skipping unsupported file " + fileToRender);
        }

    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Expected either a path to diff or lg file as argument, or a path to a git repository and a commit hash.");
            return;
        }

        if (args.length == 1) {
            final Path fileToRender = Path.of(args[0]);

            if (!Files.exists(fileToRender)) {
                Logger.error("Path " + fileToRender + " does not exist!");
                return;
            }

            Logger.info("Rendering " + (Files.isDirectory(fileToRender) ? "directory " : "file ") + fileToRender);

            if (Files.isDirectory(fileToRender)) {
                FileUtils.listAllFilesRecursively(fileToRender).forEach(SimpleRenderer::render);
            } else {
                render(fileToRender);
            }
        } else if (args.length == 3) {
            final Path repoPath = Path.of(args[0]);
            final String repoName = repoPath.getFileName().toString();
            final String commit = args[1];
            final String file = args[2];

            final Repository repository = Repository.fromDirectory(repoPath, repoName);
            repository.setParseOptions(repository.getParseOptions().withDiffStoragePolicy(ParseOptions.DiffStoragePolicy.REMEMBER_STRIPPED_DIFF));

            final List<DiffTreeTransformer> transform = DiffTreeMiner.Postprocessing(repository);
            final PatchDiff patch = DiffTreeParser.parsePatch(repository, file, commit);
            assert patch != null;
            DiffTreeTransformer.apply(transform, patch.getDiffTree());
            renderer.render(patch, Path.of("render", repoName), vulkanRenderOptions);
        }

        System.out.println("done");
    }
}
