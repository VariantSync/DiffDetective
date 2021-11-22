package diff.difftree.serialize;

public class DiffTreeLineGraphImportOptions {
    public enum GraphFormat {
    	// TODO wo kann man das abgleichen?
        DIFFTREE, // erstmal ignorieren
        DIFFGRAPH 
    }

    IDiffNodeLineGraphImporter nodeParser;
    GraphFormat format;
}