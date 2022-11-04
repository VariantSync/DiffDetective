package org.variantsync.diffdetective.diff.difftree.render;

import org.variantsync.diffdetective.diff.difftree.serialize.LineGraphExportOptions;
import org.variantsync.diffdetective.diff.difftree.serialize.GraphFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.edgeformat.DefaultEdgeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.edgeformat.EdgeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.nodeformat.DebugDiffNodeFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.nodeformat.DiffNodeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.treeformat.DiffTreeLabelFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration options to configure rendering of DiffTrees.
 * @param format The format specifies if the input to render is a DiffTree or DiffGraph. Most of the time you want to pick {@link GraphFormat#DIFFTREE}.
 * @param treeFormat The export format for DiffTree names and metadata. This format may read or write a DiffTree source.
 * @param nodeFormat The export format for DiffNodes. This format decides how nodes are labeled in the exported graph.
 * @param edgeFormat The export format for edges in DiffTrees. This format decides how edges are labeled as well as their direction.
 * @param cleanUpTemporaryFiles During rendering, some temporary files might be created. Set this to true if these files should be deleted after rendering.
 * @param dpi The resolution of the produced image. Higher yields a better resolution at the cost of a larger memory footprint.
 * @param nodesize The size, nodes should be printed (in pixels?).
 * @param edgesize The thickness of drawn edge lines.
 * @param arrowsize The size of arrows that are drawn at the end of directed edges.
 * @param fontsize The size of any rendered text.
 * @param withlabels Set this to true if labels of nodes should be printed as text. False will show unlabeled nodes.
 * @param extraArguments Arbitrary extra command-line arguments for the underlying renderer.
 *                       For example, some formats require additional information.
 *                       The list should be filled with alternating values for parameter names and their values.
 *                       All values will be passed as is, separated with spaces to the command line call of the internal renderer.
 *
 * @author Paul Bittner, Kevin Jedelhauser
 */
public record RenderOptions(
		GraphFormat format,
		DiffTreeLabelFormat treeFormat,
		DiffNodeLabelFormat nodeFormat,
		EdgeLabelFormat edgeFormat,
		boolean cleanUpTemporaryFiles,
		int dpi,
		int nodesize,
		double edgesize,
		int arrowsize,
		int fontsize,
		boolean withlabels,
		List<String> extraArguments)
{
	/**
	 * Default options.
	 */
	public static RenderOptions DEFAULT = new Builder().build();

	/**
	 * Builder for {@link RenderOptions}.
	 */
	public static class Builder {
		private GraphFormat format;
		private DiffTreeLabelFormat treeParser;
		private DiffNodeLabelFormat nodeParser;
		private EdgeLabelFormat edgeParser;
		private boolean cleanUpTemporaryFiles;
		private int dpi;
		private int nodesize;
		private double edgesize;
		private int arrowsize;
		private int fontsize;
		private boolean withlabels;
		private List<String> extraArguments;

		/**
		 * Creates a new builder with the default options for {@link RenderOptions}.
		 */
		public Builder() {
			format = GraphFormat.DIFFTREE;
			treeParser = new CommitDiffDiffTreeLabelFormat();
			nodeParser = new DebugDiffNodeFormat();
			edgeParser = new DefaultEdgeLabelFormat();
			cleanUpTemporaryFiles = true;
			dpi = 300;
			nodesize = 700;
			edgesize = 1.2;
			arrowsize = 15;
			fontsize = 5;
			withlabels = true;
			extraArguments = new ArrayList<>();
		}
		
		/**
		 * Complete the creation of {@link RenderOptions}.
		 * 
		 * @return {@link RenderOptions} with this builder's configured settings.
		 */
		public RenderOptions build() {
			return new RenderOptions(
					format, 
					treeParser, 
					nodeParser, 
					edgeParser, 
					cleanUpTemporaryFiles, 
					dpi, 
					nodesize, 
					edgesize, 
					arrowsize, 
					fontsize, 
					withlabels, 
					extraArguments);
		}

		/**
		 * @see RenderOptions#format
		 */
		public Builder setGraphFormat(GraphFormat format) {
			this.format = format;
			return this;
		}

		/**
		 * @see RenderOptions#treeFormat
		 */
		public Builder setTreeFormat(DiffTreeLabelFormat treeFormat) {
			this.treeParser = treeFormat;
			return this;
		}

		/**
		 * @see RenderOptions#nodeFormat
		 */
		public Builder setNodeFormat(DiffNodeLabelFormat nodeFormat) {
			this.nodeParser = nodeFormat;
			return this;
		}

		/**
		 * @see RenderOptions#edgeFormat
		 */
		public Builder setEdgeFormat(EdgeLabelFormat edgeFormat) {
			this.edgeParser = edgeFormat;
			return this;
		}

		/**
		 * @see RenderOptions#cleanUpTemporaryFiles
		 */
		public Builder setCleanUpTemporaryFiles(boolean cleanUpTemporaryFiles) {
			this.cleanUpTemporaryFiles = cleanUpTemporaryFiles;
			return this;
		}

		/**
		 * @see RenderOptions#dpi
		 */
		public Builder setDpi(int dpi) {
			this.dpi = dpi;
			return this;
		}

		/**
		 * @see RenderOptions#nodesize
		 */
		public Builder setNodesize(int nodesize) {
			this.nodesize = nodesize;
			return this;
		}

		/**
		 * @see RenderOptions#edgesize
		 */
		public Builder setEdgesize(double edgesize) {
			this.edgesize = edgesize;
			return this;
		}

		/**
		 * @see RenderOptions#arrowsize
		 */
		public Builder setArrowsize(int arrowsize) {
			this.arrowsize = arrowsize;
			return this;
		}

		/**
		 * @see RenderOptions#fontsize
		 */
		public Builder setFontsize(int fontsize) {
			this.fontsize = fontsize;
			return this;
		}

		/**
		 * @see RenderOptions#withlabels
		 */
		public Builder setWithlabels(boolean withlabels) {
			this.withlabels = withlabels;
			return this;
		}

		/**
		 * Resets the extra arguments to the given list.
		 * @see RenderOptions#extraArguments
		 */
		public Builder setExtraArguments(List<String> extraArguments) {
			this.extraArguments = new ArrayList<>(extraArguments);
			return this;
		}

		/**
		 * Adds further arguments to the already set extra arguments.
		 * @see RenderOptions#extraArguments
		 */
		public Builder addExtraArguments(String... args) {
			// add new list arguments to already existing arguments
            this.extraArguments.addAll(List.of(args));
			return this;
		}

	}

	/**
	 * Converts this RenderOptions to options for linegraph export.
	 * Linegraph options are a subset of render options.
	 * @return Options for linegraph export consistent to this RenderOptions.
	 */
	public LineGraphExportOptions toLineGraphOptions() {
		return new LineGraphExportOptions(format(), treeFormat(), nodeFormat(), edgeFormat());
	}
}