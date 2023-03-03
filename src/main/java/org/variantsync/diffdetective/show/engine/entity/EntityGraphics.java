package org.variantsync.diffdetective.show.engine.entity;

import org.variantsync.diffdetective.show.engine.Entity;

import java.awt.*;
import java.awt.geom.AffineTransform;

public abstract class EntityGraphics {
    private Entity entity;

    public EntityGraphics() {

    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    public abstract void draw(Graphics2D screen, AffineTransform parentTransform);
}
