package org.variantsync.diffdetective.metadata;

import org.variantsync.diffdetective.analysis.AnalysisResult.ResultKey;
import org.variantsync.diffdetective.diff.git.CommitDiff;
import org.variantsync.diffdetective.editclass.EditClass;
import org.variantsync.diffdetective.editclass.EditClassCatalogue;
import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.functjonal.Cast;
import org.variantsync.functjonal.Functjonal;
import org.variantsync.functjonal.category.InplaceSemigroup;
import org.variantsync.functjonal.map.MergeMap;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Metadata that tracks how often edit classes were matched.
 * @author Paul Bittner
 */
public class EditClassCount implements Metadata<EditClassCount> {
    public static final ResultKey<EditClassCount> KEY = new ResultKey<>("EditClassCount");

    /**
     * Counts the occurrences of a data point across commits.
     */
    public static class Occurrences {
        /**
         * Combine two occurrences by adding their counts
         * and performing a set union on the set of origin commits.
         */
        public static InplaceSemigroup<Occurrences> ISEMIGROUP = (a, b) -> {
            a.totalAmount += b.totalAmount;
            a.uniqueCommits.addAll(b.uniqueCommits);
        };

        private int totalAmount = 0;
        private final HashSet<String> uniqueCommits = new HashSet<>();

        /**
         * Report the occurrence of a relevant data point in the given CommitDiff.
         * @param commit The commit in which a match was found.
         */
        public void increment(final CommitDiff commit) {
            ++totalAmount;
            uniqueCommits.add(commit.getCommitHash());
        }

        /**
         * Returns the amount how often a data point was found.
         */
        public int getTotalAmount() {
            return totalAmount;
        }

        /**
         * Returns the set of all commits in which a data point was found.
         */
        public int getAmountOfUniqueCommits() {
            return uniqueCommits.size();
        }

        @Override
        public String toString() {
            return "{ total = " + totalAmount + "; commits = " + getAmountOfUniqueCommits() + " }";
        }
    }

    /**
     * Composes two edit class counts by composing their respective occurrence counts for each edit
     * class.
     * @see Occurrences#ISEMIGROUP
     */
    public static InplaceSemigroup<EditClassCount> ISEMIGROUP() {
        return (a, b) -> MergeMap.putAllValues(
            a.occurrences,
            b.occurrences,
            Occurrences.ISEMIGROUP
        );
    }

    private final LinkedHashMap<EditClass, Occurrences> occurrences;

    /**
     * Create a new empty count that reports occurrences of the given edit class.
     * @param editClasses edit classes whose occurrences should be counted.
     */
    public EditClassCount(final EditClassCatalogue editClasses) {
        occurrences = new LinkedHashMap<>();
        for (final EditClass p : editClasses.all()) {
            occurrences.put(p, new Occurrences());
        }
    }

    /**
     * Report the match of the given edit class in the given commit diff.
     * The count of the given edit class will be incremented and the commit will be memorized as
     * one of the commits in which this edit class was matched.
     * @param editClass The edit class that was matched.
     * @param commit The CommitDiff in which the edit class match occurred.
     * @throws AssertionError when the given edit class is not present this counts catalog.
     * @see #EditClassCount(EditClassCatalogue)
     */
    public void reportOccurrenceFor(final EditClass editClass, CommitDiff commit) {
        Assert.assertTrue(
                occurrences.containsKey(editClass),
                () -> "Reported unkown edit class \""
                        + editClass.getName()
                        + "\" but expected one of "
                        + occurrences.keySet().stream()
                          .map(EditClass::getName)
                          .collect(Collectors.joining())
                        + "!"
        );
        occurrences.get(editClass).increment(commit);
    }
    
    /**
     * Parses lines containing {@link EditClass edit classes} to {@link EditClassCount}.
     * 
     * @param lines Lines containing {@link EditClass edit classes} to be parsed
     * @return {@link EditClassCount}
     */
    public static EditClassCount parse(final List<String> lines, final String uuid) {
        EditClassCount count = new EditClassCount(ProposedEditClasses.Instance);
        String[] keyValuePair;
        String key;
        String value;
        String[] innerKeyValuePair;
        int total;
        int commits;
        for (final String line : lines) {
            keyValuePair = line.split(": ");
            key = keyValuePair[0]; // edit class
            value = keyValuePair[1]; // key value content
            value = value.replaceAll("[{} ]", ""); // remove unnecessary symbols
            innerKeyValuePair = value.split(";");
            total = Integer.parseInt(innerKeyValuePair[0].split("=")[1]); // total count
            commits = Integer.parseInt(innerKeyValuePair[1].split("=")[1]);
            
            // get edit class from key
            final String finalKey = key;
            EditClass editClass = ProposedEditClasses.Instance.fromName(key).orElseThrow(
                    () -> new RuntimeException("Could not find EditClass with name " + finalKey)
            );
            
            Occurrences occurence = new Occurrences();
            occurence.totalAmount = total;

            // add fake commits
            for (int i = 0; i < commits; ++i) {
                occurence.uniqueCommits.add(uuid + i);
            }
            
            // add occurrence
            count.occurrences.put(editClass, occurence);
        }
        
        return count;
    }

    @Override
    public LinkedHashMap<String, String> snapshot() {
        return Functjonal.bimap(
                occurrences,
                EditClass::getName,
                Occurrences::toString,
                LinkedHashMap::new
        );
    }

    @Override
    public void setFromSnapshot(LinkedHashMap<String, String> snap) {
        for (var occurrence : occurrences.entrySet()) {
            var editClass = occurrence.getKey();
            var value = snap.get(editClass.getName());
            if (value != null) {
                value = value.replaceAll("[{} ]", ""); // remove unnecessary symbols
                var innerKeyValuePair = value.split(";");
                var total = Integer.parseInt(innerKeyValuePair[0].split("=")[1]); // total count
                var commits = Integer.parseInt(innerKeyValuePair[1].split("=")[1]);

                Occurrences occurence = new Occurrences();
                occurence.totalAmount = total;

                // add fake commits
                for (int i = 0; i < commits; ++i) {
                    occurence.uniqueCommits.add(String.valueOf(i));
                }

                // add occurrence
                occurrences.put(editClass, occurence);
            }
        }
    }

    /**
     * Mutates and returns first element.
     */
    @Override
    public InplaceSemigroup<EditClassCount> semigroup() {
        return ISEMIGROUP();
    }

    /**
     * Returns the current occurrence count for each considered edit class.
     */
    public LinkedHashMap<EditClass, Occurrences> getOccurrences() {
        return occurrences;
    }
}
