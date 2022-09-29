package org.variantsync.diffdetective.diff.difftree.render;

import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.GitPatch;
import org.variantsync.diffdetective.diff.PatchDiff;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.DiffTreeSource;
import org.variantsync.diffdetective.diff.difftree.LineGraphConstants;
import org.variantsync.diffdetective.diff.difftree.serialize.LineGraphExportOptions;
import org.variantsync.diffdetective.diff.difftree.serialize.DiffTreeSerializeDebugData;
import org.variantsync.diffdetective.diff.difftree.serialize.LineGraphExport;
import org.variantsync.diffdetective.shell.PythonCommand;
import org.variantsync.diffdetective.shell.ShellException;
import org.variantsync.diffdetective.shell.ShellExecutor;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.util.IO;
import org.variantsync.diffdetective.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * A DiffTreeRenderer renders DiffTrees and CommitDiffs.
 * The renderer may be configured with {@link RenderOptions}.
 * Internally, this renderer will invoke a python render script.
 * @author Paul Bittner
 */
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

    /**
     * Use this method only in DiffDetective and not from a project using DiffDetective as a library.
     * Creates a preconfigured renderer that uses the python virtual environment shipped with
     * DiffDetective's source code.
     * @return A renderer that may be used from applications within DiffDetective itself.
     */
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

    /**
     * Combination of {@link DiffTreeRenderer#WithinDiffDetective()} and {@link DiffTreeRenderer#FromThirdPartyApplication(Supplier, Path)}.
     * This method produces a renderer that uses the virtual environment within DiffDetective's source code but that
     * can be used from another application.
     * Use this, when you integrated DiffDetective as a library in your project but you still have the DiffDetective repository
     * cloned on your disk.
     * @param relativePathFromWorkDirToDiffDetectiveSources Path relative from the given working directory to the root directory of your local copy of the DiffDetective git repository.
     * @param workDir Path to your working directory. May be absolute or relative.
     * @return A renderer that uses the python virtual environment in the DiffDetective source files from an external application.
     */
    public static DiffTreeRenderer FromThirdPartyApplication(final Path relativePathFromWorkDirToDiffDetectiveSources, final Path workDir) {
        return FromThirdPartyApplication(
                () -> new PythonCommand(
                        relativePathFromWorkDirToDiffDetectiveSources.resolve(PythonCommand.DiffDetectiveVenv).toString(),
                        relativePathFromWorkDirToDiffDetectiveSources.resolve(DiffDetectiveRenderScriptPath)),
                workDir);
    }

    /**
     * Invokes {@link DiffTreeRenderer#render(PatchDiff, Path, RenderOptions)} with the {@link RenderOptions#DEFAULT default render options}.
     */
    public boolean render(PatchDiff patchDiff, final Path directory) {
        return render(patchDiff, directory, RenderOptions.DEFAULT);
    }

    /**
     * Invokes {@link DiffTreeRenderer#render(DiffTree, GitPatch, Path, RenderOptions)}
     * by extracting the given patchDiff's DiffTree.
     */
    public boolean render(PatchDiff patchDiff, final Path directory, final RenderOptions options) {
        return render(patchDiff.getDiffTree(), patchDiff, directory, options);
    }

    /**
     * Invokes {@link DiffTreeRenderer#render(DiffTree, GitPatch, Path, RenderOptions, LineGraphExportOptions)}
     * by extracting the given patchDiff's DiffTree.
     */
    public boolean render(PatchDiff patchDiff, final Path directory, final RenderOptions options, final LineGraphExportOptions exportOptions) {
        return render(patchDiff.getDiffTree(), patchDiff, directory, options, exportOptions);
    }

    /**
     * Invokes {@link DiffTreeRenderer#render(DiffTree, GitPatch, Path, RenderOptions, LineGraphExportOptions)}
     * by creating {@link LineGraphExportOptions} via {@link RenderOptions#toLineGraphOptions()}.
     */
    public boolean render(final DiffTree tree, final GitPatch patch, final Path directory, final RenderOptions options) {
        return render(tree, patch, directory, options, options.toLineGraphOptions());
    }

    /**
     * Invokes {@link DiffTreeRenderer#render(DiffTree, String, Path, RenderOptions, LineGraphExportOptions)}
     * by creating a name for the tree and its produced image file.
     * The created <code>treeAndFileName</code> is the given patches file name and commit hash, separated by {@link LineGraphConstants#TREE_NAME_SEPARATOR}.
     */
    public boolean render(final DiffTree tree, final GitPatch patch, final Path directory, final RenderOptions options, final LineGraphExportOptions exportOptions) {
        final String treeAndFileName =
                patch.getFileName()
                        + LineGraphConstants.TREE_NAME_SEPARATOR
                        + patch.getCommitHash();
        return render(tree, treeAndFileName, directory, options, exportOptions);
    }

    /**
     * Invokes {@link DiffTreeRenderer#render(DiffTree, String, Path, RenderOptions)}
     * with the {@link RenderOptions#DEFAULT default render options}.
     */
    public boolean render(final DiffTree tree, final String treeAndFileName, final Path directory) {
        return render(tree, treeAndFileName, directory, RenderOptions.DEFAULT);
    }

    /**
     * Invokes {@link DiffTreeRenderer#render(DiffTree, String, Path, RenderOptions, LineGraphExportOptions)}
     * by creating {@link LineGraphExportOptions} via {@link RenderOptions#toLineGraphOptions()}.
     */
    public boolean render(final DiffTree tree, final String treeAndFileName, final Path directory, RenderOptions options) {
        return render(tree, treeAndFileName, directory, options, options.toLineGraphOptions());
    }

    /**
     * Invokes {@link DiffTreeRenderer#render(DiffTree, String, Path, RenderOptions, LineGraphExportOptions, BiFunction)}
     * with the a default tree header factory.
     * The tree header factory uses the given <code>treeAndFileName</code> to create
     * <code>LineGraphConstants.LG_TREE_HEADER + " " + treeAndFileName + LineGraphConstants.TREE_NAME_SEPARATOR + "0"</code>.
     * @see LineGraphConstants#LG_TREE_HEADER
     * @see LineGraphConstants#TREE_NAME_SEPARATOR
     */
    public boolean render(final DiffTree tree, final String treeAndFileName, final Path directory, RenderOptions options, final LineGraphExportOptions exportOptions) {
        return render(tree, treeAndFileName, directory, options, exportOptions,
                (treeName, treeSource) -> LineGraphConstants.LG_TREE_HEADER + " " + treeAndFileName + LineGraphConstants.TREE_NAME_SEPARATOR + "0"
                );
    }

    /**
     * Invokes {@link DiffTreeRenderer#render(DiffTree, String, Path, RenderOptions, LineGraphExportOptions, BiFunction)}
     * with the a default tree header factory.
     * The tree header factory uses the given <code>treeAndFileName</code> to create
     * <code>LineGraphConstants.LG_TREE_HEADER + " " + treeAndFileName + LineGraphConstants.TREE_NAME_SEPARATOR + "0"</code>.
     * @see LineGraphConstants#LG_TREE_HEADER
     * @see LineGraphConstants#TREE_NAME_SEPARATOR
     */
    public boolean renderWithTreeFormat(final DiffTree tree, final String treeAndFileName, final Path directory, RenderOptions options) {
        return render(tree, treeAndFileName, directory, options, options.toLineGraphOptions(),
                (treeName, treeSource) -> options.treeFormat().toLineGraphLine(treeSource)
        );
    }

    /**
     * Renders the given DiffTree to an image file.
     * First, exports the tree to a linegraph file.
     * Second, invokes a python render script with the produced linegraph file as input.
     * The python script will produce an image file at the given directory.
     * @param tree The tree to render.
     * @param treeAndFileName A name for the written file as well as the tree (used as a caption in the produced image).
     * @param directory The directory to which the rendered file should be written to.
     * @param options Configuration options for the rendering process.
     * @param exportOptions Configuration options for the intermediate export to the linegraph format.
     *                      Should be compatible with the render options.
     *                      Most of the time, you just want to use {@link RenderOptions#toLineGraphOptions()} here.
     * @param treeHeader A factory that produces a name for the given tree in the intermediate linegraph file.
     *                   The function is invoked on the given treeAndFileName as first argument and the given DiffTree's source as second argument.
     * @return True iff rendering was successful. False iff an error occurred.
     */
    private boolean render(final DiffTree tree, final String treeAndFileName, final Path directory, RenderOptions options, LineGraphExportOptions exportOptions, BiFunction<String, DiffTreeSource, String> treeHeader) {
        final Path tempFile = directory.resolve(treeAndFileName + ".lg");

        try (var destination = IO.newBufferedOutputStream(tempFile)) {
            destination.write((treeHeader.apply(treeAndFileName, tree.getSource()) + StringUtils.LINEBREAK).getBytes());
            final DiffTreeSerializeDebugData result = LineGraphExport.toLineGraphFormat(tree, exportOptions, destination);
            Assert.assertNotNull(result);
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

    /**
     * Invokes {@link DiffTreeRenderer#renderFile(Path, RenderOptions)} with the {@link RenderOptions#DEFAULT default render options}.
     */
    public boolean renderFile(final Path lineGraphFile) {
        return renderFile(lineGraphFile, RenderOptions.DEFAULT);
    }

    /**
     * Renders the given linegraph file with the given options.
     * @param lineGraphFile The path of a linegraph file in which only DiffTrees are stored.
     * @param options Configuration options for the rendering process.
     * @return True iff rendering was successful. False iff an error occurred.
     */
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
