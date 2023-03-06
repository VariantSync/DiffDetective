package org.variantsync.diffdetective.show.variation;

import org.variantsync.diffdetective.show.engine.Entity;
import org.variantsync.diffdetective.show.engine.geom.Transform;
import org.variantsync.diffdetective.show.engine.entity.EntityGraphics;
import org.variantsync.diffdetective.show.engine.geom.Vec2;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class EdgeGraphics<N> extends EntityGraphics {
    private final Entity from, to;
    private final Color color;
    private final double thickness = 4;

    public EdgeGraphics(Entity from, Entity to, Color color) {
        this.from = from;
        this.to = to;
        this.color = color;
    }
    @Override
    public void draw(Graphics2D screen, AffineTransform parentTransform) {
        final AffineTransform t = Transform.mult(parentTransform, getEntity().getRelativeTransform());
        final Stroke oldStroke = screen.getStroke();

        final Stroke stroke = new BasicStroke((float)Vec2.all(thickness).deltaTransform(t).x());
        screen.setStroke(stroke);

        screen.setColor(color);
        final Vec2 fromPos = from.getLocation().transform(t);
        final Vec2 toPos = to.getLocation().transform(t);
        screen.drawLine((int)fromPos.x(), (int)fromPos.y(), (int)toPos.x(), (int)toPos.y());

        screen.setStroke(oldStroke);
    }
}
