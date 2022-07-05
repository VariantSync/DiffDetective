package org.variantsync.diffdetective.metadata;

import org.variantsync.diffdetective.diff.CommitDiff;
import org.variantsync.diffdetective.pattern.elementary.ElementaryPattern;
import org.variantsync.diffdetective.pattern.elementary.ElementaryPatternCatalogue;
import org.variantsync.diffdetective.pattern.elementary.proposed.ProposedElementaryPatterns;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.functjonal.Functjonal;
import org.variantsync.functjonal.category.InplaceSemigroup;
import org.variantsync.functjonal.map.MergeMap;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Metadata that tracks how often elementary edit patterns were matched.
 * @author Paul Bittner
 */
public class ElementaryPatternCount implements Metadata<ElementaryPatternCount> {
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
     * Composes two pattern counts by composing their respective occurrence counts for elementary edit patterns.
     * @see Occurrences#ISEMIGROUP
     */
    public static InplaceSemigroup<ElementaryPatternCount> ISEMIGROUP = (a, b) -> MergeMap.putAllValues(
            a.occurences,
            b.occurences,
            Occurrences.ISEMIGROUP
    );

    private final LinkedHashMap<ElementaryPattern, Occurrences> occurences;

    /**
     * Create a new empty count with the {@link ProposedElementaryPatterns default pattern catalog}.
     */
    public ElementaryPatternCount() {
        this(ProposedElementaryPatterns.Instance);
    }

    /**
     * Create a new empty count that reports occurrences of the given elementary edit patterns.
     * @param patterns Patterns whose occurrences should be counted.
     */
    public ElementaryPatternCount(final ElementaryPatternCatalogue patterns) {
        occurences = new LinkedHashMap<>();
        for (final ElementaryPattern p : patterns.all()) {
            occurences.put(p, new Occurrences());
        }
    }

    /**
     * Report the match of the given pattern in the given commit diff.
     * The count of the given pattern will be incremented and the commit will be memorized as
     * one of the commits in which this elementary pattern was matched.
     * @param pattern The pattern that was matched.
     * @param commit The CommitDiff in which the pattern match occurred.
     * @throws AssertionError when the given pattern is not a pattern of this counds catalog.
     * @see #ElementaryPatternCount(ElementaryPatternCatalogue)
     */
    public void reportOccurrenceFor(final ElementaryPattern pattern, CommitDiff commit) {
        Assert.assertTrue(
                occurences.containsKey(pattern),
                () -> "Reported unkown pattern \""
                        + pattern.getName()
                        + "\" but expected one of "
                        + occurences.keySet().stream()
                          .map(ElementaryPattern::getName)
                          .collect(Collectors.joining())
                        + "!"
        );
        occurences.get(pattern).increment(commit);
    }
    
    /**
     * Parses lines containing {@link ElementaryPattern elementary patterns} to {@link ElementaryPatternCount}.
     * 
     * @param lines Lines containing {@link ElementaryPattern elementary patterns} to be parsed
     * @return {@link ElementaryPatternCount}
     */
    public static ElementaryPatternCount parse(final List<String> lines, final String uuid) {
        ElementaryPatternCount count = new ElementaryPatternCount();
        String[] keyValuePair;
        String key;
        String value;
        String[] innerKeyValuePair;
        int total;
        int commits;
        for (final String line : lines) {
            keyValuePair = line.split(": ");
            key = keyValuePair[0]; // elementary pattern
            value = keyValuePair[1]; // key value content
            value = value.replaceAll("[{} ]", ""); // remove unnecessary symbols
            innerKeyValuePair = value.split(";");
            total = Integer.parseInt(innerKeyValuePair[0].split("=")[1]); // total count
            commits = Integer.parseInt(innerKeyValuePair[1].split("=")[1]);
            
            // get pattern from key
            final String finalKey = key;
            ElementaryPattern pattern = ProposedElementaryPatterns.Instance.fromName(key).orElseThrow(
                    () -> new RuntimeException("Could not find Elementary Pattern with name " + finalKey)
            );
            
            Occurrences occurence = new Occurrences();
            occurence.totalAmount = total;

            // add fake commits
            for (int i = 0; i < commits; ++i) {
                occurence.uniqueCommits.add(uuid + i);
            }
            
            // add occurrence
            count.occurences.put(pattern, occurence);
        }
        
        return count;
    }

    @Override
    public LinkedHashMap<String, String> snapshot() {
        return Functjonal.bimap(
                occurences,
                ElementaryPattern::getName,
                Occurrences::toString,
                LinkedHashMap::new
        );
    }

    /**
     * Mutates and returns first element.
     */
    @Override
    public InplaceSemigroup<ElementaryPatternCount> semigroup() {
        return ISEMIGROUP;
    }

    /**
     * Returns the current occurrence count for each considered elementary edit pattern.
     */
    public LinkedHashMap<ElementaryPattern, Occurrences> getOccurences() {
        return occurences;
    }
}
