package feature;

import org.prop4j.Node;

@FunctionalInterface
public interface IsFeatureAnnotation {
    boolean test(final String diffLine, final Node parsedFormula);

    IsFeatureAnnotation YES_TO_ALL = (d, p) -> true;
}
