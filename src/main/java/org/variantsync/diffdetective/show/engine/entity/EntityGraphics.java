package org.variantsync.diffdetective.show.engine.entity;

import org.variantsync.diffdetective.show.engine.EntityComponent;

import java.awt.*;
import java.awt.geom.AffineTransform;

public abstract class EntityGraphics extends EntityComponent {

    public EntityGraphics() {

    }

    public abstract void draw(Graphics2D screen, AffineTransform parentTransform);
}
