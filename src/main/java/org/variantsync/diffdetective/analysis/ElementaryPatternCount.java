package org.variantsync.diffdetective.analysis;

import org.variantsync.diffdetective.pattern.elementary.ElementaryPattern;
import org.variantsync.diffdetective.pattern.elementary.ElementaryPatternCatalogue;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ElementaryPatternCount {
    private final ElementaryPatternCatalogue catalogue;
    private final Map<ElementaryPattern, Integer> patterncounts;

    public ElementaryPatternCount(final ElementaryPatternCatalogue catalogue) {
        this.catalogue = catalogue;
        this.patterncounts = new HashMap<>();
        catalogue.all().forEach(e -> patterncounts.put(e, 0));
    }

    public void increment(final ElementaryPattern pattern) {
        patterncounts.computeIfPresent(pattern, (p, i) -> i + 1);
    }

    public String toCSV() {
        return toCSV(";");
    }

    public String toCSV(final String delimiter) {
        return catalogue.all().stream()
                .map(patterncounts::get)
                .map(Object::toString)
                .collect(Collectors.joining(delimiter));
    }
}
