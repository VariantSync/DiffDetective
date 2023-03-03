package org.variantsync.diffdetective.show.engine;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class Transform {
    public static AffineTransform mult(AffineTransform l, AffineTransform r) {
        final AffineTransform result = new AffineTransform(l);
        result.concatenate(r);
        return result;
    }

    public static AffineTransform mult(AffineTransform... rs) {
        AffineTransform result = new AffineTransform();
        for (int i = rs.length - 1; i >= 0; --i) {
            result = mult(rs[i], result);
        }
        return result;
    }

    public static void renderTransformed(AffineTransform t, double x, double y, BiConsumer<Double, Double> action) {
        Point2D.Double dest = new Point2D.Double();
        t.transform(new Point2D.Double(x, y), dest);
        action.accept(dest.x, dest.y);
    }

    public static void transformed(AffineTransform t, Point2D.Double p, Consumer<Point2D> action) {
        Point2D.Double dest = new Point2D.Double();
        t.transform(p, dest);
        action.accept(dest);
    }

    public static void deltaTransformed(AffineTransform t, Point2D.Double p, Consumer<Point2D> action) {
        Point2D.Double dest = new Point2D.Double();
        t.deltaTransform(p, dest);
        action.accept(dest);
    }

    public static void transformed(AffineTransform t, double x, double y, Consumer<Point2D> action) {
        transformed(t, new Point2D.Double(x, y), action);
    }

    public static void deltaTransformed(AffineTransform t, double x, double y, Consumer<Point2D> action) {
        deltaTransformed(t, new Point2D.Double(x, y), action);
    }

    public static void transformed2(AffineTransform t, Point2D.Double a, Point2D.Double b, BiConsumer<Point2D, Point2D> action) {
        Point2D[] pts = new Point2D[]{
                new Point2D.Double(a.x, a.y),
                new Point2D.Double(b.x, b.y),
        };

        t.transform(pts, 0, pts, 0, pts.length);
        action.accept(pts[0], pts[1]);
    }

    public static void transformed2(AffineTransform t, double ax, double ay, double bx, double by, BiConsumer<Point2D, Point2D> action) {
        transformed2(
                t,
                new Point2D.Double(ax, ay),
                new Point2D.Double(bx, by),
                action);
    }

    public static void boxed(AffineTransform t, double width, double height,
                             BiConsumer<
                                     Point2D, // upper left corner
                                     Point2D  // lower right corner
                                     > action)
    {
        transformed2(
                t,
                -width / 2.0, -height / 2.0,
                 width / 2.0,  height / 2.0,
                action
        );
    }
}
