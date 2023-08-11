package org.variantsync.diffdetective.show.engine.hitbox;

import org.variantsync.diffdetective.show.engine.geom.Circle;
import org.variantsync.diffdetective.show.engine.geom.Vec2;

public class CircleHitbox extends Hitbox {
    private Circle circle;

    public CircleHitbox(final Circle circle) {
        this.circle = circle;
    }

    public void setRadius(Circle circle) {
        this.circle = circle;
    }

    public Circle getCircle() {
        return circle;
    }

    @Override
    public boolean contains(final Vec2 point) {
        return getEntity().getLocation().distanceTo(point) <= circle.radius();
    }
}
