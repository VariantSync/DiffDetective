package diff.data.difftreerender;

import de.ovgu.featureide.fm.core.analysis.cnf.generator.configuration.util.Pair;
import diff.data.DiffTree;
import diff.data.PatchDiff;
import org.pmw.tinylog.Logger;
import shell.*;
import util.DebugData;
import util.ExportUtils;
import util.LineGraphExport;
import util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DiffTreeRenderer {
    private static final Path DiffDetectiveRenderScriptPath = Path.of("linegraph", "renderLinegraph.py");
    private static final Path DiffDetectiveWorkDir = null;
    private final Path renderScriptPath;
    private final Path workDir;

    private DiffTreeRenderer(final Path pythonRenderScriptPath, final Path workDir) {
        this.renderScriptPath = pythonRenderScriptPath;
        this.workDir = workDir;
    }

    public static DiffTreeRenderer WithinDiffDetective() {
        return new DiffTreeRenderer(DiffDetectiveRenderScriptPath, DiffDetectiveWorkDir);
    }

    public static DiffTreeRenderer FromThirdPartyApplication(final Path pythonRenderScriptPath, final Path workDir) {
        return new DiffTreeRenderer(pythonRenderScriptPath, workDir);
    }

    public void render(PatchDiff patchDiff) {
        final String treename = patchDiff.getFileName() + LineGraphExport.TREE_NAME_SEPARATOR + patchDiff.getCommitDiff().getCommitHash();
        render(patchDiff.getDiffTree(), treename);
    }

    public boolean render(DiffTree tree, String name) {
        final LineGraphExport.Options options =
                new LineGraphExport.Options(LineGraphExport.NodePrintStyle.Verbose, false);

        final Path tempFile = Path.of("temp", name);

        final Pair<DebugData, String> result = LineGraphExport.toLineGraphFormat(tree, options);
        final String lg = "t # " + name + StringUtils.LINEBREAK;
        try {
            ExportUtils.write(tempFile, lg);
        } catch (IOException e) {
            Logger.error("Could not render difftree " + name + " because:", e);
            return false;
        }

        if (renderFile(tempFile)) {
            try {
                Files.delete(tempFile);
            } catch (IOException e) {
                Logger.error("Could not remove generated temp file " + tempFile + " because:", e);
            }
        }

        return false;
    }

    public boolean renderFile(final Path lineGraphFile) {
        final PythonCommand cmd = PythonCommand.VenvPython3(renderScriptPath, lineGraphFile.toString());
        final ShellExecutor runner = new ShellExecutor(Logger::info, Logger::error);

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
