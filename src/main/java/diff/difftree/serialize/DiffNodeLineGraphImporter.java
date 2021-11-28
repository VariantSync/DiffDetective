package diff.difftree.serialize;

import diff.difftree.DiffNode;

public interface DiffNodeLineGraphImporter {
	
	/**
	 * Creates a DiffNode from a given vertex in form of a lineGraphLine.
	 * 
	 * @param lineGraphLine A line read from a line graph
	 * @return A parsed {@link DiffNode}
	 */
    DiffNode parse(String lineGraphLine);
    
}