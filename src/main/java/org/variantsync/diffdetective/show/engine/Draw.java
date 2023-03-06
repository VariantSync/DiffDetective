package org.variantsync.diffdetective.show.engine;

import org.variantsync.diffdetective.show.engine.geom.Box;
import org.variantsync.diffdetective.show.engine.geom.Transform;
import org.variantsync.diffdetective.show.engine.geom.Vec2;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Draw {
    public static void borderedShape(
            Graphics2D screen,
            AffineTransform t,
            double width,
            double height,
            Function<Box, Box> innerTransform,
            Color fillColor,
            Color borderColor,
            Consumer<Box> draw
    ) {
        // The following works only if there was no rotation in the translation (i.e., it is rotation unaware).
        final Box borderBox = Transform.box(t, width, height);
        screen.setColor(borderColor);
        draw.accept(borderBox);
        screen.setColor(fillColor);
        draw.accept(innerTransform.apply(borderBox));
    }

    public static void borderedShapeRelative(
            Graphics2D screen,
            AffineTransform t,
            double width,
            double height,
            double relativeBorderWidth,
            Color fillColor,
            Color borderColor,
            Consumer<Box> draw
    ) {
        borderedShape(
                screen, t, width, height,
                box -> box.shrink(new Vec2(relativeBorderWidth * box.getWidth(), relativeBorderWidth * box.getHeight())),
                fillColor, borderColor, draw
        );
    }

    public static void borderedShapeAbsolute(
            Graphics2D screen,
            AffineTransform t,
            double width,
            double height,
            double absoluteBorderWidth,
            Color fillColor,
            Color borderColor,
            Consumer<Box> draw
    ) {
        borderedShape(
                screen, t, width, height,
                box -> box.shrink(Vec2.all(absoluteBorderWidth).deltaTransform(t)),
                fillColor, borderColor, draw
        );
    }

    public static void fillOval(
            Graphics2D screen,
            Box box
    ) {
        screen.fillOval(
                (int) box.upperLeft().x(),
                (int) box.upperLeft().y(),
                (int) box.getWidth(),
                (int) box.getHeight()
        );
    }
}
