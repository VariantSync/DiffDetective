package org.variantsync.diffdetective.show.engine;

import org.variantsync.diffdetective.show.engine.entity.EntityGraphics;

import java.awt.*;
import java.awt.geom.AffineTransform;

public final class Entity {
    private double x, y;
    private final AffineTransform relativeTransform;
    private final EntityGraphics graphics;

    public Entity(EntityGraphics graphics) {
        x = 0;
        y = 0;
        relativeTransform = new AffineTransform();

        assert graphics.getEntity() == null;
        this.graphics = graphics;
        this.graphics.setEntity(this);
    }

    public AffineTransform getRelativeTransform() {
        return relativeTransform;
    }

    public void updateTransform() {
        relativeTransform.setTransform(
                1, 0,
                0, 1,
                x, y);
    }

    public void setLocation(double x, double y) {
        this.x = x;
        this.y = y;
        updateTransform();
    }

    public double getX() { return x; }

    public double getY() { return y; }

    public EntityGraphics getGraphics() {
        return graphics;
    }
}
