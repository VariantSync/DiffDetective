package org.variantsync.diffdetective.show.engine.geom;

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

    public static void transformed2(AffineTransform t, Point2D a, Point2D b, BiConsumer<Point2D, Point2D> action) {
        Point2D[] pts = new Point2D[]{a, b};
        t.transform(pts, 0, pts, 0, pts.length);
        action.accept(pts[0], pts[1]);
    }

    public static void transformed2(AffineTransform t, Vec2 a, Vec2 b, BiConsumer<Vec2, Vec2> action) {
        transformed2(t, a.toPoint2D(), b.toPoint2D(), (at, bt) -> action.accept(Vec2.from(at), Vec2.from(bt)));
    }

    public static Box box(AffineTransform t, double width, double height)
    {
        return new Box(
                new Vec2(-width / 2.0, -height / 2.0).transform(t),
                new Vec2( width / 2.0,  height / 2.0).transform(t)
        );
    }
}
