package diff.difftree.serialize;

import diff.difftree.DiffNode;

public interface IDiffNodeLineGraphImporter {
	
	/**
	 * Creates a DiffNode from a given vertex in form of a lineGraphLine.
	 * 
	 * @param lineGraphLine A line read from a line graph file with the format "v NODE_ID NAME"
	 * @return A parsed {@link DiffNode}
	 */
    DiffNode parse(String lineGraphLine);
    
}