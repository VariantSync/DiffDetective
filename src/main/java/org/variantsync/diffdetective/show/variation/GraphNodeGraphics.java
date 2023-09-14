package org.variantsync.diffdetective.show.variation;

import org.variantsync.diffdetective.show.engine.Draw;
import org.variantsync.diffdetective.show.engine.entity.EntityGraphics;
import org.variantsync.diffdetective.show.engine.geom.Transform;
import org.variantsync.diffdetective.show.engine.geom.Vec2;
import org.variantsync.diffdetective.show.engine.hitbox.CircleHitbox;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class GraphNodeGraphics extends EntityGraphics {
    //// Represented content
    final NodeView<?> node;

    //// Rendering values
    double circle_borderwidth_relative = 0.05;
    double textbox_borderwidth_absolute = 2;
    double textbox_insets_absolute = 12;
    int textbox_borderarcwidth_absolute = 7;
    Font basic = new Font(null, Font.PLAIN, VariationDiffApp.DEFAULT_FONT_SIZE);

    public GraphNodeGraphics(NodeView<?> node) {
        this.node = node;
    }

    @Override
    public void draw(Graphics2D screen, AffineTransform parentTransform) {
        final AffineTransform oldTransform = new AffineTransform(screen.getTransform());
        final Font oldFont = screen.getFont();

        final AffineTransform t = Transform.mult(parentTransform, getEntity().getRelativeTransform());
        final double radius = getEntity().get(CircleHitbox.class).getCircle().radius();
        final double width  = 2 * radius;
        @SuppressWarnings("SuspiciousNameCombination") final double height = width;

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
                textwidth + textbox_insets_absolute, textheight + textbox_insets_absolute,
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
