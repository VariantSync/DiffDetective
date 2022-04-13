package org.variantsync.diffdetective.diff.difftree.render;

import org.variantsync.diffdetective.diff.difftree.serialize.GraphFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.edgeformat.DefaultEdgeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.edgeformat.EdgeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.nodeformat.DebugDiffNodeFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.nodeformat.DiffNodeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.treeformat.DiffTreeLabelFormat;

import java.util.ArrayList;
import java.util.List;

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
		List<String> extraArguments) {
	
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
		 * Default options for {@link RenderOptions}.
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
		 * Complete creation of {@link RenderOptions}.
		 * 
		 * @return {@link RenderOptions}
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
		
		public Builder setGraphFormat(GraphFormat format) {
			this.format = format;
			return this;
		}

		public Builder setTreeFormat(DiffTreeLabelFormat treeFormat) {
			this.treeParser = treeFormat;
			return this;
		}

		public Builder setNodeFormat(DiffNodeLabelFormat nodeFormat) {
			this.nodeParser = nodeFormat;
			return this;
		}

		public Builder setEdgeFormat(EdgeLabelFormat edgeFormat) {
			this.edgeParser = edgeFormat;
			return this;
		}

		public Builder setCleanUpTemporaryFiles(boolean cleanUpTemporaryFiles) {
			this.cleanUpTemporaryFiles = cleanUpTemporaryFiles;
			return this;
		}

		public Builder setDpi(int dpi) {
			this.dpi = dpi;
			return this;
		}

		public Builder setNodesize(int nodesize) {
			this.nodesize = nodesize;
			return this;
		}

		public Builder setEdgesize(double edgesize) {
			this.edgesize = edgesize;
			return this;
		}

		public Builder setArrowsize(int arrowsize) {
			this.arrowsize = arrowsize;
			return this;
		}

		public Builder setFontsize(int fontsize) {
			this.fontsize = fontsize;
			return this;
		}

		public Builder setWithlabels(boolean withlabels) {
			this.withlabels = withlabels;
			return this;
		}

		public Builder setExtraArguments(List<String> extraArguments) {
			this.extraArguments = new ArrayList<>(extraArguments);
			return this;
		}

		public Builder addExtraArguments(String... args) {
			// add new list arguments to already existing arguments
            this.extraArguments.addAll(List.of(args));
			return this;
		}

	}

}