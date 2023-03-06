package org.variantsync.diffdetective.show.variation;

import org.variantsync.diffdetective.show.engine.Entity;
import org.variantsync.diffdetective.show.engine.geom.Transform;
import org.variantsync.diffdetective.show.engine.entity.EntityGraphics;
import org.variantsync.diffdetective.show.engine.geom.Vec2;
import org.variantsync.diffdetective.show.engine.hitbox.CircleHitbox;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class EdgeGraphics extends EntityGraphics {
    private final Entity from, to;
    private final Color color;
    private final double thickness = 4;
    private final double tipLength = 14;

    public EdgeGraphics(Entity from, Entity to, Color color) {
        this.from = from;
        this.to = to;
        this.color = color;
    }
    @Override
    public void draw(Graphics2D screen, AffineTransform parentTransform) {
        final AffineTransform t = Transform.mult(parentTransform, getEntity().getRelativeTransform());

        /// setup screen
        final Stroke oldStroke = screen.getStroke();
        final double transformedThickness = Vec2.all(thickness).deltaTransform(t).x();
        screen.setStroke(new BasicStroke((float) transformedThickness));
        screen.setColor(color);

        /// draw line
        final double fromRadius = from.get(CircleHitbox.class).getCircle().radius();
        final double toRadius   =   to.get(CircleHitbox.class).getCircle().radius();

        final double strokeRadius = 0.5 * thickness;
        final Vec2 fromPos = from.getLocation();
        final Vec2 toPos   = to.getLocation();
        final Vec2 ray     = toPos.minus(fromPos).normalize();

        final Vec2 edgeStart = fromPos.add(
                ray.scale(strokeRadius + fromRadius)
        );

        final Vec2 edgeEnd = toPos.minus(
                ray.scale(toRadius + tipLength)
        );

        final Vec2 drawStart = edgeStart.transform(t);
        final Vec2 drawEnd   = edgeEnd.transform(t);
        screen.drawLine((int)drawStart.x(), (int)drawStart.y(), (int)drawEnd.x(), (int)drawEnd.y());

        /// draw tip
        final Vec2 tipNeck = edgeEnd;
        final Vec2 tipHead = tipNeck.add(
                ray.scale(tipLength)
        );
        final Vec2 tipRay = tipHead.minus(tipNeck);
        final Vec2 tipL   = tipNeck.add(tipRay.rotate90DegreesCounterClockwise());
        final Vec2 tipR   = tipNeck.add(tipRay.rotate90DegreesClockwise());

        final Vec2 transformedTipHead = tipHead.transform(t);
        final Vec2 transformedTipL = tipL.transform(t);
        final Vec2 transformedTipR = tipR.transform(t);
        final int[] tipx = new int[] {
                (int) transformedTipHead.x(),
                (int) transformedTipL.x(),
                (int) transformedTipR.x()
        };
        final int[] tipy = new int[] {
                (int) transformedTipHead.y(),
                (int) transformedTipL.y(),
                (int) transformedTipR.y()
        };
        screen.fillPolygon(tipx, tipy, tipx.length);

        // reset screen
        screen.setStroke(oldStroke);
    }
}
