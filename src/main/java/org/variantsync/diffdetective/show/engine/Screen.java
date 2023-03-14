package org.variantsync.diffdetective.show.engine;

import org.tinylog.Logger;
import org.variantsync.diffdetective.show.engine.entity.EntityGraphics;
import org.variantsync.diffdetective.show.engine.geom.Vec2;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class Screen extends JPanel {
    private final AffineTransform viewTransform;
    private final Window window;

    public Screen(Window window) {
        super(true);

        this.window = window;
        viewTransform = new AffineTransform();
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

        Graphics2D renderTarget = (Graphics2D) gc;

        // draw Background
        renderTarget.setColor(Color.WHITE);
        renderTarget.fillRect(0, 0, window.getWidth(), window.getHeight());
        renderTarget.setColor(Color.BLACK);

        // draw all WorkingElements
        w.sortEntities();
        for (Entity e : w.getEntities()) {
            final EntityGraphics eGraphics = e.get(EntityGraphics.class);
            if (eGraphics != null) {
                eGraphics.draw(renderTarget, viewTransform);
            }
        }
    }

    public Vec2 screenToViewportCoord(Vec2 pos) {
        try {
            return Vec2.from(viewTransform.inverseTransform(
                    pos.toPoint2D(),
                    null));
        } catch (java.awt.geom.NoninvertibleTransformException e) {
            Logger.error(e.getMessage());
        }

        return null;
    }

    public AffineTransform getViewTransform() {
        return viewTransform;
    }

    public Texture screenshot() {
        final Texture screenshot = new Texture(getWidth(), getHeight());
        this.paint(screenshot.getAwtImage().getGraphics());
        return screenshot;
    }
}
