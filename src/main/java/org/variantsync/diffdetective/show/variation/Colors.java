package org.variantsync.diffdetective.show.variation;

import org.variantsync.diffdetective.variation.NodeType;
import org.variantsync.diffdetective.variation.diff.DiffType;

import java.awt.*;
import java.util.Map;

public class Colors {
    public static Color ANNOTATION = new Color(0.2f, 0.4f, 0.9f);
    public static Map<DiffType, Color> ofDiffType = Map.of(
            DiffType.ADD, new Color(161,215,106),
            DiffType.REM, new Color(233,163,201),
            DiffType.NON, Color.LIGHT_GRAY
    );

    public static Color ofNodeType(NodeType t) {
        if (t.isAnnotation()) {
            return ANNOTATION;
        }
        return Color.BLACK;
    }
}
