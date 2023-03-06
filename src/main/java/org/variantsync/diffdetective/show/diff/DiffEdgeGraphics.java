package org.variantsync.diffdetective.show.diff;

import org.variantsync.diffdetective.show.engine.Entity;
import org.variantsync.diffdetective.show.engine.geom.Transform;
import org.variantsync.diffdetective.show.engine.entity.EntityGraphics;
import org.variantsync.diffdetective.show.engine.geom.Vec2;
import org.variantsync.diffdetective.variation.diff.DiffType;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class DiffEdgeGraphics extends EntityGraphics {
    private DiffNodeGraphics fromGraphics;
    private DiffNodeGraphics toGraphics;
    private DiffType diffType;
    private double thickness = 4;

    public DiffEdgeGraphics(DiffNodeGraphics fromGraphics, DiffNodeGraphics toGraphics, DiffType diffType) {
        this.fromGraphics = fromGraphics;
        this.toGraphics = toGraphics;
        this.diffType = diffType;
    }
    @Override
    public void draw(Graphics2D screen, AffineTransform parentTransform) {
        final AffineTransform t = Transform.mult(parentTransform, getEntity().getRelativeTransform());
        final Stroke oldStroke = screen.getStroke();

        Entity fromEntity = fromGraphics.getEntity();
        Entity toEntity = toGraphics.getEntity();

        final Stroke stroke = new BasicStroke((float)Vec2.all(thickness).deltaTransform(t).x());
        screen.setStroke(stroke);

        screen.setColor(Colors.ofDiffType.get(diffType));
        Transform.transformed2(t,
                fromEntity.getLocation(),
                toEntity.getLocation(),
                (from, to) -> {
                    screen.drawLine((int)from.x(), (int)from.y(), (int)to.x(), (int)to.y());
                });

        screen.setStroke(oldStroke);
    }
}
