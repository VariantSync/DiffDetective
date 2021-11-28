package diff.difftree.serialize;

import diff.difftree.parse.DiffTreeParser;

public class DiffTreeLineGraphImportOptions {
	// TODO where to differentiate between graphs and trees?
    public enum GraphFormat {
        DIFFTREE, // erstmal ignorieren
        DIFFGRAPH // nur für graphen implementieren, fromNodes verwenden
    }
    
    GraphFormat format;

    /**
     * For parsing a tree from a line graph.
     */
    public DiffTreeNodeLabelFormat treeParser = new DiffTreeParser();

    /**
     * For parsing a DiffNode from a line graph.
     */
    public DiffNodeLineGraphImporter nodeParser = new MiningDiffNodeLineGraphImporter();
    
    
}