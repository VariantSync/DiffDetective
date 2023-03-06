package org.variantsync.diffdetective.show.diff;

import org.variantsync.diffdetective.show.engine.Draw;
import org.variantsync.diffdetective.show.engine.geom.Transform;
import org.variantsync.diffdetective.show.engine.entity.EntityGraphics;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.serialize.nodeformat.*;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class DiffNodeGraphics extends EntityGraphics {
    final DiffNode d;
    final DiffNodeLabelFormat f;
    int width, height;
    double circle_borderwidth_relative = 0.05;
    double textbox_borderwidth_absolute = 3;
    int textbox_borderarcwidth_absolute = 7;
    Font basic = new Font(null, Font.PLAIN, 20);

    public DiffNodeGraphics(DiffNode d, DiffNodeLabelFormat format) {
        this.d = d;
        this.f = format;
        width = 100;
        height = 100;
    }

    @Override
    public void draw(Graphics2D screen, AffineTransform parentTransform) {
        final AffineTransform oldTransform = new AffineTransform(screen.getTransform());
        final Font oldFont = screen.getFont();

        final AffineTransform t = Transform.mult(parentTransform, getEntity().getRelativeTransform());

        // Draw circle
        Draw.borderedShapeRelative(
                screen, t,
                width, height,
                circle_borderwidth_relative,
                Colors.ofDiffType.get(d.getDiffType()), Colors.ofNodeType(d.nodeType),
                box -> Draw.fillOval(screen, box)
        );

        // Draw label box
        final String text = f.toLabel(d);
        screen.setFont(basic);
        FontMetrics fm = screen.getFontMetrics();
        int textwidth  = fm.stringWidth(text);
        int textheight = fm.getHeight();

        Draw.borderedShapeAbsolute(
                screen, t,
                textwidth + 2 * textbox_borderwidth_absolute, textheight + 2 * textbox_borderwidth_absolute,
                textbox_borderwidth_absolute,
                Color.WHITE, Color.BLACK,
                box -> Transform.deltaTransformed(t, textbox_borderarcwidth_absolute, textbox_borderarcwidth_absolute,
                        arc -> screen.fillRoundRect(
                                (int) box.upperLeft().x(),
                                (int) box.upperLeft().y(),
                                (int) box.getWidth(),
                                (int) box.getHeight(),
                                (int) arc.getX(),
                                (int) arc.getY()
                        )
                )
        );

        // Draw text
        screen.setColor(Color.BLACK);
        screen.setFont(basic.deriveFont(t));
        fm = screen.getFontMetrics();
        double textx = -fm.stringWidth(text) / 2.0;
        double texty = -fm.getHeight() / 2.0 + fm.getAscent();
        screen.drawString(text, (int) textx, (int) texty);

        screen.setFont(oldFont);
        screen.setTransform(oldTransform);
    }
}
