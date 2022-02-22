package metadata;

import de.variantsync.functjonal.Functjonal;
import de.variantsync.functjonal.category.InplaceSemigroup;
import de.variantsync.functjonal.map.MergeMap;
import diff.CommitDiff;
import pattern.atomic.AtomicPattern;
import pattern.atomic.AtomicPatternCatalogue;
import pattern.atomic.proposed.ProposedAtomicPatterns;
import util.Assert;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class AtomicPatternCount implements Metadata<AtomicPatternCount> {
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

    public static InplaceSemigroup<AtomicPatternCount> ISEMIGROUP = (a, b) -> MergeMap.putAllValues(a.occurences, b.occurences, Occurrences.ISEMIGROUP);

    private final LinkedHashMap<AtomicPattern, Occurrences> occurences;

    public AtomicPatternCount() {
        this(ProposedAtomicPatterns.Instance);
    }

    public AtomicPatternCount(final AtomicPatternCatalogue patterns) {
        occurences = new LinkedHashMap<>();
        for (final AtomicPattern p : patterns.all()) {
            occurences.put(p, new Occurrences());
        }
    }

    public void reportOccurrenceFor(final AtomicPattern pattern, CommitDiff commit) {
        Assert.assertTrue(
                occurences.containsKey(pattern),
                () ->     "Reported unkown pattern \""
                        + pattern.getName()
                        + "\" but expected one of "
                        + occurences.keySet().stream()
                          .map(AtomicPattern::getName)
                          .collect(Collectors.joining())
                        + "!"
        );
        occurences.get(pattern).increment(commit);
    }
    
    /**
     * Parses lines containing {@link AtomicPattern AtomicPatterns} to {@link AtomicPatternCount}.
     * 
     * @param lines Lines containing {@link AtomicPattern AtomicPatterns} to be parsed
     * @return {@link AtomicPatternCount}
     */
    public static AtomicPatternCount parse(final List<String> lines, final String uuid) {
        AtomicPatternCount count = new AtomicPatternCount();
        String[] keyValuePair;
        String key;
        String value;
        String[] innerKeyValuePair;
        int total;
        int commits;
        for (final String line : lines) {
            keyValuePair = line.split(": ");
            key = keyValuePair[0]; // atomic pattern
            value = keyValuePair[1]; // key value content
            value = value.replaceAll("[{} ]", ""); // remove unnecessary symbols
            innerKeyValuePair = value.split(";");
            total = Integer.parseInt(innerKeyValuePair[0].split("=")[1]); // total count
            commits = Integer.parseInt(innerKeyValuePair[1].split("=")[1]);
            
            // get pattern from key
            AtomicPattern pattern = ProposedAtomicPatterns.Instance.fromName(key).get();
            
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
                AtomicPattern::getName,
                Occurrences::toString,
                LinkedHashMap::new
        );
    }

    /**
     * Mutates and returns first element.
     */
    @Override
    public InplaceSemigroup<AtomicPatternCount> semigroup() {
        return ISEMIGROUP;
    }

    public LinkedHashMap<AtomicPattern, Occurrences> getOccurences() {
        return occurences;
    }
}
