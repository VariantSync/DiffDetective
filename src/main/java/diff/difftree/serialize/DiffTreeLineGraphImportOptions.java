package diff.difftree.serialize;

public class DiffTreeLineGraphImportOptions {
	
	/**
	 * Format of the graph.
	 */
    public enum GraphFormat {
        DIFFTREE,
        DIFFGRAPH
    }

    /**
     * Style of the node in the line graph.
     */
    public enum NodeStyle {
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
    
    public GraphFormat format;

    public NodeStyle style; 
 
    
    // TODO ?
    // ‘Add parser for tree labels to DiffTreeSource as mentioned above.’
	
}