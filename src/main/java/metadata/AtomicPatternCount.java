package metadata;

import diff.CommitDiff;
import pattern.atomic.AtomicPattern;
import pattern.atomic.AtomicPatternCatalogue;
import pattern.atomic.proposed.ProposedAtomicPatterns;
import util.Assert;
import util.functional.Functional;
import util.functional.Semigroup;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AtomicPatternCount implements Metadata<AtomicPatternCount> {
    public static class Occurrences implements Semigroup<Occurrences> {
        private int totalAmount = 0;
        private final HashSet<String> uniqueCommits = new HashSet<>();

        public void increment(final CommitDiff commit) {
            ++totalAmount;
            uniqueCommits.add(commit.getCommitHash());
        }

        public int get() {
            return totalAmount;
        }

        @Override
        public void append(Occurrences other) {
            this.totalAmount += other.totalAmount;
            this.uniqueCommits.addAll(other.uniqueCommits);
        }

        @Override
        public String toString() {
            return "{ total = " + totalAmount + "; commits = " + uniqueCommits.size() + " }";
        }
    }

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

    @Override
    public void append(AtomicPatternCount other) {
        for (final Map.Entry<AtomicPattern, Occurrences> otherEntry : other.occurences.entrySet()) {
            Semigroup.appendValue(this.occurences, otherEntry.getKey(), otherEntry.getValue());
        }
    }

    @Override
    public LinkedHashMap<String, String> snapshot() {
        return Functional.bimap(
                occurences,
                AtomicPattern::getName,
                Occurrences::toString,
                LinkedHashMap::new
        );
    }
}
