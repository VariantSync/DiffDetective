package org.variantsync.diffdetective.diff.difftree.render;

import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.GitPatch;
import org.variantsync.diffdetective.diff.PatchDiff;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.DiffTreeSource;
import org.variantsync.diffdetective.diff.difftree.LineGraphConstants;
import org.variantsync.diffdetective.diff.difftree.serialize.DiffTreeLineGraphExportOptions;
import org.variantsync.diffdetective.diff.difftree.serialize.DiffTreeSerializeDebugData;
import org.variantsync.diffdetective.diff.difftree.serialize.LineGraphExport;
import org.variantsync.diffdetective.shell.PythonCommand;
import org.variantsync.diffdetective.shell.ShellException;
import org.variantsync.diffdetective.shell.ShellExecutor;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.util.IO;
import org.variantsync.diffdetective.util.StringUtils;
import org.variantsync.functjonal.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class DiffTreeRenderer {
    private static final Path DiffDetectiveRenderScriptPath = Path.of("linegraph", "renderLinegraph.py");
    private static final Path DiffDetectiveWorkDir = null;
    private static final Supplier<PythonCommand> DiffDetectivePythonCommand
            = () -> PythonCommand.DiffDetectiveVenvPython3(DiffDetectiveRenderScriptPath);

    private final Path workDir;
    private final Supplier<PythonCommand> pythonCommandFactory;

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

    public static DiffTreeRenderer FromThirdPartyApplication(final Path relativePathFromWorkDirToDiffDetectiveSources, final Path workDir) {
        return FromThirdPartyApplication(
                () -> new PythonCommand(
                        relativePathFromWorkDirToDiffDetectiveSources.resolve(PythonCommand.DiffDetectiveVenv).toString(),
                        relativePathFromWorkDirToDiffDetectiveSources.resolve(DiffDetectiveRenderScriptPath)),
                workDir);
    }

    public boolean render(PatchDiff patchDiff, final Path directory) {
        return render(patchDiff, directory, RenderOptions.DEFAULT);
    }

    public boolean render(PatchDiff patchDiff, final Path directory, final RenderOptions options) {
        return render(patchDiff.getDiffTree(), patchDiff, directory, options);
    }

    public boolean render(PatchDiff patchDiff, final Path directory, final RenderOptions options, final DiffTreeLineGraphExportOptions exportOptions) {
        return render(patchDiff.getDiffTree(), patchDiff, directory, options, exportOptions);
    }

    public boolean render(final DiffTree tree, final GitPatch patch, final Path directory, final RenderOptions options) {
        final String treeAndFileName =
                patch.getFileName()
                        + LineGraphConstants.TREE_NAME_SEPARATOR
                        + patch.getCommitHash();
        return render(tree, treeAndFileName, directory, options, options.toLineGraphOptions());
    }

    public boolean render(final DiffTree tree, final GitPatch patch, final Path directory, final RenderOptions options, final DiffTreeLineGraphExportOptions exportOptions) {
        final String treeAndFileName =
                patch.getFileName()
                        + LineGraphConstants.TREE_NAME_SEPARATOR
                        + patch.getCommitHash();
        return render(tree, treeAndFileName, directory, options, exportOptions);
    }

    public boolean render(final DiffTree tree, final String treeAndFileName, final Path directory) {
        return render(tree, treeAndFileName, directory, RenderOptions.DEFAULT);
    }

    public boolean render(final DiffTree tree, final String treeAndFileName, final Path directory, RenderOptions options) {
        return render(tree, treeAndFileName, directory, options, options.toLineGraphOptions());
    }

    public boolean render(final DiffTree tree, final String treeAndFileName, final Path directory, RenderOptions options, final DiffTreeLineGraphExportOptions exportOptions) {
        return render(tree, treeAndFileName, directory, options, exportOptions,
                (treeName, treeSource) -> LineGraphConstants.LG_TREE_HEADER + " " + treeAndFileName + LineGraphConstants.TREE_NAME_SEPARATOR + "0"
                );
    }

    public boolean renderWithTreeFormat(final DiffTree tree, final String treeAndFileName, final Path directory, RenderOptions options) {
        return render(tree, treeAndFileName, directory, options, options.toLineGraphOptions(),
                (treeName, treeSource) -> options.treeFormat().toLineGraphLine(treeSource)
        );
    }

    private boolean render(final DiffTree tree, final String treeAndFileName, final Path directory, RenderOptions options, DiffTreeLineGraphExportOptions exportOptions, BiFunction<String, DiffTreeSource, String> treeHeader) {
        final Path tempFile = directory.resolve(treeAndFileName + ".lg");

        final Pair<DiffTreeSerializeDebugData, String> result = LineGraphExport.toLineGraphFormat(tree, exportOptions);
        Assert.assertNotNull(result);
        final String lg = treeHeader.apply(treeAndFileName, tree.getSource()) + StringUtils.LINEBREAK + result.second();

        try {
            IO.write(tempFile, lg);
        } catch (IOException e) {
            Logger.error(e, "Could not render difftree {} because", treeAndFileName);
            return false;
        }

        if (renderFile(tempFile, options) && options.cleanUpTemporaryFiles()) {
            try {
                Files.delete(tempFile);
            } catch (IOException e) {
                Logger.error(e, "Could not remove generated temp file {} because", tempFile);
            }
        }

        return false;
    }

    public boolean renderFile(final Path lineGraphFile) {
        return renderFile(lineGraphFile, RenderOptions.DEFAULT);
    }

    public boolean renderFile(final Path lineGraphFile, RenderOptions options) {
        final PythonCommand cmd = pythonCommandFactory.get();//apply(lineGraphFile);

        cmd.addArg("--nodesize").addArg(options.nodesize());
        cmd.addArg("--dpi").addArg(options.dpi());
        cmd.addArg("--edgesize").addArg(options.edgesize());
        cmd.addArg("--arrowsize").addArg(options.arrowsize());
        cmd.addArg("--fontsize").addArg(options.fontsize());
        if (!options.withlabels()) {
            cmd.addArg("--nolabels");
        }
        for (final String arg : options.extraArguments()) {
            cmd.addArg(arg);
        }
        cmd.addArg(lineGraphFile.toString());

        final ShellExecutor runner = new ShellExecutor(
                m -> System.out.println("  [RENDER] " + m),
                m -> System.err.println("  [RENDER] " + m)
        );

        try {
            Logger.debug("Running command {}{}", cmd, (workDir != null ? "in " + workDir : ""));
            runner.execute(cmd, workDir);
        } catch (ShellException e) {
            Logger.error(e, "Could not render linegraph file {} because", lineGraphFile);
            return false;
        }

        return true;
    }
}
