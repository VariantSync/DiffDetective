package org.variantsync.diffdetective.analysis;

public class PatchProcessDetails {
    private static final String STR_DELIMITER = "___";
    private static final String CSV_DELIMITER = ";";
    private String hash;
    private String fileName;
    private String repoName;
    private long milliseconds;
    private int addedComplexityPercents;
    
    public PatchProcessDetails(final String hash, final String fileName, final String reponame, long milliseconds, int addedComplexityPercents){
        this.hash = hash;
        this.fileName = fileName;
        this.repoName = reponame;
        this.milliseconds = milliseconds;
        this.addedComplexityPercents = addedComplexityPercents;
    }

    public String toString() {
        return hash + STR_DELIMITER + repoName + STR_DELIMITER + fileName + STR_DELIMITER+ addedComplexityPercents + "%" + STR_DELIMITER + milliseconds + "ms";
    }

    public String toCSV(){
        return hash + CSV_DELIMITER + repoName + CSV_DELIMITER + fileName + CSV_DELIMITER+ addedComplexityPercents + CSV_DELIMITER + milliseconds;
    }
}

