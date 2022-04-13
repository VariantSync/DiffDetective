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

public class ElementaryPatternCount implements Metadata<ElementaryPatternCount> {
    public static class Occurrences {
        public static InplaceSemigroup<Occurrences> ISEMIGROUP = (a, b) -> {
            a.totalAmount += b.totalAmount;
            a.uniqueCommits.addAll(b.uniqueCommits);
        };

        private int totalAmount = 0;
        private final HashSet<String> uniqueCommits = new HashSet<>();

        public void increment(final CommitDiff commit) {
            ++totalAmount;
            uniqueCommits.add(commit.getCommitHash());
        }

        public int getTotalAmount() {
            return totalAmount;
        }

        public int getAmountOfUniqueCommits() {
            return uniqueCommits.size();
        }

        @Override
        public String toString() {
            return "{ total = " + totalAmount + "; commits = " + getAmountOfUniqueCommits() + " }";
        }
    }

    public static InplaceSemigroup<ElementaryPatternCount> ISEMIGROUP = (a, b) -> MergeMap.putAllValues(a.occurences, b.occurences, Occurrences.ISEMIGROUP);

    private final LinkedHashMap<ElementaryPattern, Occurrences> occurences;

    public ElementaryPatternCount() {
        this(ProposedElementaryPatterns.Instance);
    }

    public ElementaryPatternCount(final ElementaryPatternCatalogue patterns) {
        occurences = new LinkedHashMap<>();
        for (final ElementaryPattern p : patterns.all()) {
            occurences.put(p, new Occurrences());
        }
    }

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

    public LinkedHashMap<ElementaryPattern, Occurrences> getOccurences() {
        return occurences;
    }
}
