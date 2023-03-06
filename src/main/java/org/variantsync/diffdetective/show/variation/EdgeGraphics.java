package org.variantsync.diffdetective.show.variation;

import org.variantsync.diffdetective.show.engine.Entity;
import org.variantsync.diffdetective.show.engine.geom.Transform;
import org.variantsync.diffdetective.show.engine.entity.EntityGraphics;
import org.variantsync.diffdetective.show.engine.geom.Vec2;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class EdgeGraphics<N> extends EntityGraphics {
    private final GraphNodeGraphics<N> fromGraphics, toGraphics;
    private final Color color;
    private final double thickness = 4;

    public EdgeGraphics(GraphNodeGraphics<N> fromGraphics, GraphNodeGraphics<N> toGraphics, Color color) {
        this.fromGraphics = fromGraphics;
        this.toGraphics = toGraphics;
        this.color = color;
    }
    @Override
    public void draw(Graphics2D screen, AffineTransform parentTransform) {
        final AffineTransform t = Transform.mult(parentTransform, getEntity().getRelativeTransform());
        final Stroke oldStroke = screen.getStroke();

        Entity fromEntity = fromGraphics.getEntity();
        Entity toEntity = toGraphics.getEntity();

        final Stroke stroke = new BasicStroke((float)Vec2.all(thickness).deltaTransform(t).x());
        screen.setStroke(stroke);

        screen.setColor(color);
        Transform.transformed2(t,
                fromEntity.getLocation(),
                toEntity.getLocation(),
                (from, to) -> {
                    screen.drawLine((int)from.x(), (int)from.y(), (int)to.x(), (int)to.y());
                });

        screen.setStroke(oldStroke);
    }
}
