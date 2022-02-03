package diff.difftree.render;

import diff.difftree.serialize.GraphFormat;
import diff.difftree.serialize.edgeformat.DefaultEdgeLabelFormat;
import diff.difftree.serialize.edgeformat.EdgeLabelFormat;
import diff.difftree.serialize.nodeformat.DebugDiffNodeFormat;
import diff.difftree.serialize.nodeformat.DiffNodeLabelFormat;
import diff.difftree.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import diff.difftree.serialize.treeformat.DiffTreeLabelFormat;
import java.util.List;

public record RenderOptions(
		GraphFormat format, 
		DiffTreeLabelFormat treeParser, 
		DiffNodeLabelFormat nodeParser,
        EdgeLabelFormat edgeParser,
        boolean cleanUpTemporaryFiles,
        int dpi,
        int nodesize,
        double edgesize,
        int arrowsize,
        int fontsize,
        boolean withlabels,
        List<String> extraArguments) {
    public static RenderOptions DEFAULT = new RenderOptions(
    		GraphFormat.DIFFTREE,
    		new CommitDiffDiffTreeLabelFormat(),
            new DebugDiffNodeFormat(),
            new DefaultEdgeLabelFormat(),
            true,
            300,
            700,
            1.2,
            15,
            5,
            true,
            List.of()
    );
}