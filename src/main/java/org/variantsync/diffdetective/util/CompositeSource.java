package org.variantsync.diffdetective.util;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a {@link Source} without arguments.
 */
public class CompositeSource implements Source {
    private final String sourceExplanation;
    private final List<Source> sources;

    /**
     * @param sourceExplanation is returned verbatim by {@link getSourceExplanation}
     * @param sources is returned as immutable list by {@link getSources}
     */
    public CompositeSource(String sourceExplanation, Source... sources) {
        this.sourceExplanation = sourceExplanation;
        this.sources = Arrays.asList(sources);
    }

    @Override
    public String getSourceExplanation() {
        return sourceExplanation;
    }

    @Override
    public List<Source> getSources() {
        return sources;
    }

    @Override
    public String toString() {
        return Source.shallowExplanation(this);
    }
}
