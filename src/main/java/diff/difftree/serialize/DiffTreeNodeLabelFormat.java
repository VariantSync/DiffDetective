package diff.difftree.serialize;

import diff.difftree.DiffTreeSource;

/*
 * TODO Does it make sense to put both import and export methods in here? Then the importer and exporter classes have to implement both methods?
 * 
 */
public interface DiffTreeNodeLabelFormat {
	
	/**
	 * The print style of the {@link diff.difftree.DiffNode DiffNodes} in the line graph.
	 */
	public enum NodePrintStyle {
	    /// Print only the label
	    LabelOnly,
	    /// Print CodeType and DiffType
	    Type,
	    /// Print Node as Code
	    Code,
	    /// Print CodeType and DiffType and Mappings of Macros
	    Mappings,
	    /// Print CodeType and DiffType and Mappings if Macro and Text if Code
	    Debug,
	    /// Print metadata required for semantic pattern mining
	    Mining
	}

	/**
	 * Creates a {@link DiffTreeSource} from a lineGraphLine.
	 * 
	 * @param lineGraphLine A line read from a line graph
	 * @return A parsed {@link DiffTreeSource}
	 */
	DiffTreeSource importTree(String lineGraphLine);
	
	// TODO see above todo
//	void exportTree;
}
