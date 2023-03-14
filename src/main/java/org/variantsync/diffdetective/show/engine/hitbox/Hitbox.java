package org.variantsync.diffdetective.show.engine.hitbox;

import org.variantsync.diffdetective.show.engine.EntityComponent;
import org.variantsync.diffdetective.show.engine.geom.Vec2;

public abstract class Hitbox extends EntityComponent {
    public abstract boolean contains(final Vec2 point);

//    Point2D.Double getBounds();
}
