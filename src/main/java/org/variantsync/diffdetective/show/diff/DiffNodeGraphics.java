package org.variantsync.diffdetective.show.diff;

import org.variantsync.diffdetective.show.engine.Transform;
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
    double textbox_borderwidth_absolute = 4;
    int textbox_borderarcwidth_absolute = 7;
    Font basic = new Font(null, Font.PLAIN, 20);

    public DiffNodeGraphics(DiffNode d) {
        this.d = d;
        width = 100;
        height = 100;
        f = new ShowNodeFormat();
    }

    @Override
    public void draw(Graphics2D screen, AffineTransform parentTransform) {
        final AffineTransform oldTransform = new AffineTransform(screen.getTransform());
        final Font oldFont = screen.getFont();

        final AffineTransform t = Transform.mult(parentTransform, getEntity().getRelativeTransform());

        Transform.boxed(t, width, height,
                (ul, br) -> {
                    // The following works only if there was no rotation in the translation (i.e., it is rotation unaware).
                    final double w = br.getX() - ul.getX();
                    final double h = br.getY() - ul.getY();
                    screen.setColor(Colors.ofNodeType(d.nodeType));
                    screen.fillOval(
                            (int) ul.getX(),
                            (int) ul.getY(),
                            (int) w,
                            (int) h
                    );
                    screen.setColor(Colors.ofDiffType.get(d.getDiffType()));
                    screen.fillOval(
                            (int) (ul.getX() + circle_borderwidth_relative * w),
                            (int) (ul.getY() + circle_borderwidth_relative * h),
                            (int) (w - 2 * circle_borderwidth_relative * w),
                            (int) (h - 2 * circle_borderwidth_relative * h)
                    );
                });

        final String text = f.toLabel(d);
        screen.setFont(basic);
        FontMetrics fm = screen.getFontMetrics();
        int textwidth  = fm.stringWidth(text);
        int textheight = fm.getHeight();

        screen.setColor(Color.BLACK);
        Transform.boxed(t, textwidth + 2 * textbox_borderwidth_absolute, textheight + 2 * textbox_borderwidth_absolute,
                (ul, br) -> Transform.deltaTransformed(t, textbox_borderarcwidth_absolute, textbox_borderarcwidth_absolute, (arc) -> {
                    final double w = br.getX() - ul.getX();
                    final double h = br.getY() - ul.getY();
                    screen.fillRoundRect((int)ul.getX(), (int)ul.getY(), (int)w, (int)h, (int)arc.getX(), (int)arc.getY());
                }));

        screen.setColor(Color.WHITE);
        Transform.boxed(t, textwidth + textbox_borderwidth_absolute, textheight + textbox_borderwidth_absolute,
                (ul, br) -> Transform.deltaTransformed(t, textbox_borderarcwidth_absolute, textbox_borderarcwidth_absolute, (arc) -> {
                    final double w = br.getX() - ul.getX();
                    final double h = br.getY() - ul.getY();
                    screen.fillRoundRect((int)ul.getX(), (int)ul.getY(), (int)w, (int)h, (int)arc.getX(), (int)arc.getY());
                }));

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
