package org.variantsync.diffdetective.analysis;

import org.variantsync.diffdetective.editclass.EditClass;
import org.variantsync.diffdetective.editclass.EditClassCatalogue;
import org.variantsync.diffdetective.util.CSV;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gathers statistics about matching edit classes.
 * @author Paul Bittner
 */
public class EditClassCount implements CSV {
    private final EditClassCatalogue catalogue;
    private final Map<EditClass, Integer> editClassCounts;

    /**
     * Creates a new counter object for the given catalogue of edit classes.
     * @param catalogue The catalogue whose edit classes to match and count.
     */
    public EditClassCount(final EditClassCatalogue catalogue) {
        this.catalogue = catalogue;
        this.editClassCounts = new HashMap<>();
        catalogue.all().forEach(e -> editClassCounts.put(e, 0));
    }

    /**
     * Increment the count for the given edit class.
     * The given edit class is assumed to be part of this counts catalog.
     * @see EditClassCount#EditClassCount(EditClassCatalogue)
     * @param editClass The edit class whose count to increase by one.
     */
    public void increment(final EditClass editClass) {
        editClassCounts.computeIfPresent(editClass, (p, i) -> i + 1);
    }

    @Override
    public String toCSV(final String delimiter) {
        return catalogue.all().stream()
                .map(editClassCounts::get)
                .map(Object::toString)
                .collect(Collectors.joining(delimiter));
    }
}
