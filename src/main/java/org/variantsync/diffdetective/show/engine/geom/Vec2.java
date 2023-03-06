package org.variantsync.diffdetective.show.engine.geom;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public record Vec2(double x, double y) {
    public static Vec2 all(double val) {
        return new Vec2(val, val);
    }

    public static Vec2 from(final Point2D p) {
        return new Vec2(p.getX(), p.getY());
    }

    public Point2D.Double toPoint2D() {
        return new Point2D.Double(x(), y());
    }

    public Vec2 scale(double s) {
        return new Vec2(
                s * x(),
                s * y()
        );
    }

    public Vec2 scale(Vec2 s) {
        return new Vec2(
                s.x() * x(),
                s.y() * y()
        );
    }

    public Vec2 add(Vec2 b) {
        return new Vec2(
                x() + b.x(),
                y() + b.y()
        );
    }

    public Vec2 flip() {
        return new Vec2(
                -x(),
                -y()
        );
    }

    public Vec2 minus(Vec2 b) {
        return this.add(b.flip());
    }

    public Vec2 normalize() {
        final double l = length();
        return new Vec2(
                x() / l,
                y() / l
        );
    }

    public double length() {
        return Math.sqrt(x() * x() + y() * y());
    }

    public double distanceTo(Vec2 b) {
        return this.minus(b).length();
    }

    public Vec2 rotate90DegreesClockwise() {
        return new Vec2(
                y(),
                -x()
        );
    }

    public Vec2 rotate90DegreesCounterClockwise() {
        return new Vec2(
                -y(),
                x()
        );
    }

    public Vec2 transform(final AffineTransform t) {
        final Point2D.Double dest = new Point2D.Double();
        t.transform(this.toPoint2D(), dest);
        return from(dest);
    }

    public Vec2 deltaTransform(final AffineTransform t) {
        final Point2D.Double dest = new Point2D.Double();
        t.deltaTransform(this.toPoint2D(), dest);
        return from(dest);
    }
}
