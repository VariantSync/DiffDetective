package org.variantsync.diffdetective.analysis;

import org.variantsync.diffdetective.pattern.elementary.ElementaryPattern;
import org.variantsync.diffdetective.pattern.elementary.ElementaryPatternCatalogue;
import org.variantsync.diffdetective.util.CSV;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class to gather statistics about matching elementary edit patterns.
 * @author Paul Bittner
 */
public class ElementaryPatternCount implements CSV {
    private final ElementaryPatternCatalogue catalogue;
    private final Map<ElementaryPattern, Integer> patterncounts;

    /**
     * Creates a new counter object for the given catalogue of elementary edit patterns.
     * @param catalogue The catalogue whose patterns to match and count.
     */
    public ElementaryPatternCount(final ElementaryPatternCatalogue catalogue) {
        this.catalogue = catalogue;
        this.patterncounts = new HashMap<>();
        catalogue.all().forEach(e -> patterncounts.put(e, 0));
    }

    /**
     * Increment the count for the given elementary pattern.
     * The given pattern is assumed to be part of this counts catalog.
     * @see ElementaryPatternCount#ElementaryPatternCount(ElementaryPatternCatalogue)
     * @param pattern The pattern whose count to increase by one.
     */
    public void increment(final ElementaryPattern pattern) {
        patterncounts.computeIfPresent(pattern, (p, i) -> i + 1);
    }

    @Override
    public String toCSV(final String delimiter) {
        return catalogue.all().stream()
                .map(patterncounts::get)
                .map(Object::toString)
                .collect(Collectors.joining(delimiter));
    }
}
