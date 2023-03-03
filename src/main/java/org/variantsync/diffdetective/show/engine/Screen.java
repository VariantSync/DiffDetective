package org.variantsync.diffdetective.show.engine;

import org.tinylog.Logger;
import org.w3c.dom.Text;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

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
        double zoom = camera.getZoom();

        viewTransform.setTransform(
                zoom, 0,
                0, zoom,
                camera.getX() + getWidth() / 2.0,
                camera.getY() + getHeight() / 2.0);
    }

    protected void paintComponent(Graphics gc) {
        super.paintComponent(gc);

        World w = window.getWorld();

        updateViewTransform(w.getCamera());

        Graphics2D g2 = (Graphics2D) gc;

        // draw Background
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, window.getWidth(), window.getHeight());
        g2.setColor(Color.BLACK);

        // draw all WorkingElements
        for (Entity e : w.getEntities()) {
            e.getGraphics().draw(g2, viewTransform);
        }
    }

    public Point2D screenToLocalCoord(int x, int y) {
        return screenToLocalCoord((double)x, (double)y);
    }

    public Point2D screenToLocalCoord(double x, double y) {
        try {
            updateViewTransform(window.getWorld().getCamera());
            return viewTransform.inverseTransform(
                    new Point2D.Double(x, y),
                    null);
        } catch (java.awt.geom.NoninvertibleTransformException e) {
            Logger.error(e.getMessage());
        }

        return null;
    }
}
