package org.variantsync.diffdetective.show.engine;

import org.tinylog.Logger;
import org.variantsync.diffdetective.show.engine.entity.EntityGraphics;
import org.variantsync.diffdetective.show.engine.geom.Vec2;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class Screen extends JPanel {
    AffineTransform viewTransform;
    Window window;

    public Screen(Window window) {
        super(true);

        this.window = window;
        viewTransform = new AffineTransform();

        this.addMouseListener(window.getInputHandler());
        this.addMouseMotionListener(window.getInputHandler());
        this.addMouseWheelListener(window.getInputHandler());
    }

    private void updateViewTransform(Camera camera) {
        final double zoom = camera.getZoom();
        final Vec2 camPos = camera.getLocation();

        viewTransform.setTransform(
                zoom, 0,
                0, zoom,
                camPos.x() + getWidth()  / 2.0,
                camPos.y() + getHeight() / 2.0);
    }

    protected void paintComponent(Graphics gc) {
        super.paintComponent(gc);

        World w = window.getApp().getWorld();

        updateViewTransform(w.getCamera());

        Graphics2D g2 = (Graphics2D) gc;

        // draw Background
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, window.getWidth(), window.getHeight());
        g2.setColor(Color.BLACK);

        // draw all WorkingElements
        w.sortEntities();
        for (Entity e : w.getEntities()) {
            final EntityGraphics eGraphics = e.get(EntityGraphics.class);
            if (eGraphics != null) {
                eGraphics.draw(g2, viewTransform);
            }
        }
    }

    public Vec2 screenToLocalCoord(Vec2 pos) {
        try {
            updateViewTransform(window.getApp().getWorld().getCamera());
            return Vec2.from(viewTransform.inverseTransform(
                    pos.toPoint2D(),
                    null));
        } catch (java.awt.geom.NoninvertibleTransformException e) {
            Logger.error(e.getMessage());
        }

        return null;
    }
}
