package org.variantsync.diffdetective.analysis;

public class PatchProcessDetails {
    private static final String STR_DELIMITER = "___";
    private static final String CSV_DELIMITER = ";";
    private String hash;
    private String fileName;
    private String repoName;
    private long milliseconds;
    private int addedComplexityPercents;
    private int totalNodeCount;
    private int ifNodeCount;
    
    private int implicationEdges;
    
    private int alternativeEdges;

    
    public PatchProcessDetails(final String hash, final String fileName, final String reponame, long milliseconds, int addedComplexityPercents, int implicationEdges, int alternativeEdges, int totalNodeCount, int ifNodeCount){
        this.hash = hash;
        this.fileName = fileName;
        this.repoName = reponame;
        this.milliseconds = milliseconds;
        this.implicationEdges = implicationEdges;
        this.alternativeEdges = alternativeEdges;
        this.addedComplexityPercents = addedComplexityPercents;
        this.totalNodeCount = totalNodeCount;
        this.ifNodeCount = ifNodeCount;
    }

    public String toString() {
        return hash + STR_DELIMITER + repoName + STR_DELIMITER + fileName + STR_DELIMITER+ addedComplexityPercents + "%" + STR_DELIMITER+ implicationEdges + STR_DELIMITER+ alternativeEdges  + STR_DELIMITER + milliseconds + "ms" + STR_DELIMITER + totalNodeCount + STR_DELIMITER + ifNodeCount;
    }

    public String toCSV(){
        return repoName + CSV_DELIMITER + hash + CSV_DELIMITER + fileName + CSV_DELIMITER+ addedComplexityPercents + CSV_DELIMITER + implicationEdges + CSV_DELIMITER + alternativeEdges + CSV_DELIMITER + milliseconds + CSV_DELIMITER + totalNodeCount + CSV_DELIMITER + ifNodeCount;
    }
}

