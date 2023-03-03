package org.variantsync.diffdetective.show.engine;

import java.awt.event.*;
import java.awt.geom.Point2D;

public class InputHandler implements MouseListener, MouseMotionListener, MouseWheelListener {
    private Window window;

    public InputHandler(Window window) {
        this.window = window;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        Camera c = window.getWorld().getCamera();
        Point2D p = window.getScreen().screenToLocalCoord(e.getX(), e.getY());
        c.zoom(
                -e.getWheelRotation(),
                p.getX(),
                p.getY());
        window.refresh();
    }
}
