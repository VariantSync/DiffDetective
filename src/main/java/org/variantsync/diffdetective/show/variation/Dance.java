package org.variantsync.diffdetective.show.variation;

import org.tinylog.Logger;
import org.variantsync.diffdetective.show.engine.entity.Behaviour;
import org.variantsync.diffdetective.show.engine.geom.Vec2;

public class Dance extends Behaviour {
    private double passedTime = 0;
    private double intensity = 10;
    private Vec2 center = Vec2.all(0);

    public void setCenter(Vec2 center) {
        this.center = center;
    }

    public void resetTime() {
        passedTime = 0;
    }

    @Override
    public void update(double deltaSeconds) {
        passedTime += deltaSeconds;
        final double arg = Math.PI * passedTime;
        getEntity().setLocation(center.add(
                2 * intensity * Math.sin(    arg),
                    intensity * Math.sin(2 * arg)
        ));
    }
}
