package util;

import de.ovgu.featureide.fm.core.analysis.cnf.generator.configuration.util.Pair;

public class FeatureIDEUtils {
    public static <A, B> String toString(final Pair<A, B> p) {
        return "(" + p.getKey() + ", " + p.getValue() + ")";
    }
}
