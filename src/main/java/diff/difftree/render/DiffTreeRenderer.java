package diff.difftree.render;

import de.ovgu.featureide.fm.core.analysis.cnf.generator.configuration.util.Pair;
import diff.difftree.DiffTree;
import diff.PatchDiff;
import org.pmw.tinylog.Logger;
import shell.*;
import diff.serialize.DiffTreeSerializeDebugData;
import util.IO;
import diff.serialize.LineGraphExport;
import util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Supplier;

public class DiffTreeRenderer {
    private static final Path DiffDetectiveRenderScriptPath = Path.of("linegraph", "renderLinegraph.py");
    private static final Path DiffDetectiveWorkDir = null;
    private static final Supplier<PythonCommand> DiffDetectivePythonCommand
            = () -> PythonCommand.DiffDetectiveVenvPython3(DiffDetectiveRenderScriptPath);

    private final Path workDir;
    private final Supplier<PythonCommand> pythonCommandFactory;

    public static record RenderOptions(
            LineGraphExport.NodePrintStyle nodeStyle,
            boolean cleanUpTemporaryFiles,
            int dpi,
            int nodesize,
            boolean withlabels)
    {}
    public static final RenderOptions DefaultRenderOptions = new RenderOptions(
            LineGraphExport.NodePrintStyle.Verbose,
            true,
            300,
            700,
            true);

    private DiffTreeRenderer(final Supplier<PythonCommand> pythonCommandFactory, final Path workDir) {
        this.workDir = workDir;
        this.pythonCommandFactory = pythonCommandFactory;
    }

    public static DiffTreeRenderer WithinDiffDetective() {
        return new DiffTreeRenderer(DiffDetectivePythonCommand, DiffDetectiveWorkDir);
    }

    /**
     * Creates a renderer that operates from a third party application (i.e., a program that uses DiffDetective as a library).
     * @param pythonCommandFactory A factory to create a python command that renders the given path.
     *                             The factory thus specifies the python instance to run and the location of the render script.
     *                             DiffDetective comes with a render script in linegraph/renderLinegraph.py.
     *                             However, when invoking this method from a third party application, the location of this script is unknown.
     *                             Thus, pythonCommandFactory has to locate this script and create a PythonCommand running it.
     *                             Assume r is an absolute path to renderLinegraph.py on your file system and you want to use python3.
     *                             Then a possible a value for pythonCommandFactory would be: () -> PythonCommand.Python3(r);
     * @param workDir Working directory, to run the rendering in.
     * @return A renderer that uses the given python instance and render script to render diff trees.
     */
    public static DiffTreeRenderer FromThirdPartyApplication(final Supplier<PythonCommand> pythonCommandFactory, final Path workDir) {
        return new DiffTreeRenderer(pythonCommandFactory, workDir);
    }

    public void render(PatchDiff patchDiff, final Path directory) {
        render(patchDiff, directory, DefaultRenderOptions);
    }

    public boolean render(PatchDiff patchDiff, final Path directory, RenderOptions options) {
        final String treeAndFileName =
                patchDiff.getFileName()
                        + LineGraphExport.TREE_NAME_SEPARATOR
                        + patchDiff.getCommitDiff().getCommitHash();
        return render(patchDiff.getDiffTree(), treeAndFileName, directory, options);
    }

    public boolean render(final DiffTree tree, final String treeAndFileName, final Path directory) {
        return render(tree, treeAndFileName, directory, DefaultRenderOptions);
    }

    public boolean render(final DiffTree tree, final String treeAndFileName, final Path directory, RenderOptions options) {
        final LineGraphExport.Options lgoptions = new LineGraphExport.Options(options.nodeStyle);

        final Path tempFile = directory.resolve(treeAndFileName + ".lg");

        final Pair<DiffTreeSerializeDebugData, String> result = LineGraphExport.toLineGraphFormat(tree, lgoptions);
        final String lg = "t # " + treeAndFileName + LineGraphExport.TREE_NAME_SEPARATOR + "0" + StringUtils.LINEBREAK + result.getValue();
        try {
            IO.write(tempFile, lg);
        } catch (IOException e) {
            Logger.error("Could not render difftree " + treeAndFileName + " because:", e);
            return false;
        }

        if (renderFile(tempFile, options) && options.cleanUpTemporaryFiles) {
            try {
                Files.delete(tempFile);
            } catch (IOException e) {
                Logger.error("Could not remove generated temp file " + tempFile + " because:", e);
            }
        }

        return false;
    }

    public boolean renderFile(final Path lineGraphFile) {
        return renderFile(lineGraphFile, DefaultRenderOptions);
    }

    public boolean renderFile(final Path lineGraphFile, RenderOptions options) {
        final PythonCommand cmd = pythonCommandFactory.get();//apply(lineGraphFile);

        cmd.addArg("--nodesize").addArg(options.nodesize);
        cmd.addArg("--dpi").addArg(options.dpi);
        if (!options.withlabels) {
            cmd.addArg("--nolabels");
        }
        cmd.addArg(lineGraphFile.toString());

        final ShellExecutor runner = new ShellExecutor(
//                Logger::info, Logger::error
                System.out::println, System.err::println
        );

        try {
            Logger.info("Running command " + cmd + (workDir != null ? "in " + workDir : ""));
            runner.execute(cmd, workDir);
        } catch (ShellException e) {
            Logger.error("Could not render linegraph file " + lineGraphFile + " because:", e);
            return false;
        }

        return true;
    }
}
