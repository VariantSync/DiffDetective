package metadata;

import diff.CommitDiff;
import pattern.atomic.AtomicPattern;
import util.Functional;
import util.Semigroup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public record AtomicPatternCount(Map<AtomicPattern, Occurrences> occurences) implements Metadata<AtomicPatternCount> {
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

    public AtomicPatternCount() {
        this(new HashMap<>());
    }

    public void reportOccurrenceFor(final AtomicPattern pattern, CommitDiff commit) {
        occurences.computeIfAbsent(pattern, p -> new Occurrences()).increment(commit);
    }

    @Override
    public void append(AtomicPatternCount other) {
        for (final Map.Entry<AtomicPattern, Occurrences> otherEntry : other.occurences.entrySet()) {
            Semigroup.appendValue(this.occurences, otherEntry.getKey(), otherEntry.getValue());
        }
    }

    @Override
    public Map<String, String> snapshot() {
        return Functional.bimap(occurences, AtomicPattern::getName, Occurrences::toString);
    }
}
