package org.variantsync.diffdetective.experiments.uncertainty;

import org.variantsync.diffdetective.analysis.AnalysisResult;
import org.variantsync.diffdetective.metadata.EditClassCount;
import org.variantsync.diffdetective.metadata.Metadata;
import org.variantsync.functjonal.category.InplaceSemigroup;
import org.variantsync.functjonal.category.Semigroup;

import java.rmi.UnexpectedException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record InterestingCommit(String commitHash, List<String> filenames) implements Metadata<InterestingCommit> {

    public static Semigroup<InterestingCommit> SEMIGROUP = (a, b) -> {
        throw new RuntimeException("Key Collision");
    };

    @Override
    public LinkedHashMap<String, ?> snapshot() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put(commitHash, String.join(" ", filenames));
        return map;
    }

    @Override
    public void setFromSnapshot(LinkedHashMap<String, String> snapshot) {
        // Throw exception, we dont want this (is to read file contents)
    }

    @Override
    public InplaceSemigroup<InterestingCommit> semigroup() {
        return null;
    }

    public AnalysisResult.ResultKey<InterestingCommit> getKey() {
        return new AnalysisResult.ResultKey<>(commitHash);
    }
}
