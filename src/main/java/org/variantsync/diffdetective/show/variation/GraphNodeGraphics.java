package org.variantsync.diffdetective.show.variation;

import org.variantsync.diffdetective.show.engine.Draw;
import org.variantsync.diffdetective.show.engine.entity.EntityGraphics;
import org.variantsync.diffdetective.show.engine.geom.Transform;
import org.variantsync.diffdetective.show.engine.geom.Vec2;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class GraphNodeGraphics<N> extends EntityGraphics {
    //// Represented content
    final NodeView<N> node;

    //// Rendering values
    int width, height;
    double circle_borderwidth_relative = 0.05;
    double textbox_borderwidth_absolute = 3;
    int textbox_borderarcwidth_absolute = 7;
    Font basic = new Font(null, Font.PLAIN, 20);

    public GraphNodeGraphics(NodeView<N> node) {
        this.node = node;
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
                node.nodeColor(), node.borderColor(),
                box -> Draw.fillOval(screen, box)
        );

        // Draw label box
        final String text = node.nodeLabel();
        screen.setFont(basic);
        FontMetrics fm = screen.getFontMetrics();
        int textwidth  = fm.stringWidth(text);
        int textheight = fm.getHeight();

        Draw.borderedShapeAbsolute(
                screen, t,
                textwidth + 2 * textbox_borderwidth_absolute, textheight + 2 * textbox_borderwidth_absolute,
                textbox_borderwidth_absolute,
                Color.WHITE, Color.BLACK,
                box -> {
                    final Vec2 arcSize = Vec2.all(textbox_borderarcwidth_absolute).deltaTransform(t);
                    screen.fillRoundRect(
                            (int) box.upperLeft().x(),
                            (int) box.upperLeft().y(),
                            (int) box.getWidth(),
                            (int) box.getHeight(),
                            (int) arcSize.x(),
                            (int) arcSize.y()
                    );
                }
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
