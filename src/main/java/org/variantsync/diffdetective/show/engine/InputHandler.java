package org.variantsync.diffdetective.show.engine;

import java.awt.event.*;
import java.awt.geom.Point2D;

public class InputHandler implements MouseListener, MouseMotionListener, MouseWheelListener {
    private final Window window;

    private double camDeltaXOnPress, camDeltaYOnPress;
    private int buttonHold;

    public InputHandler(Window window) {
        this.window = window;
        buttonHold = -1;
    }

    private void cancelButtonHoldAction() {
        buttonHold = -1;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (buttonHold == -1 || buttonHold == e.getButton())
            buttonHold = e.getButton();
        else
            cancelButtonHoldAction();

        // Drag nodes with LMB
//        if (buttonHold == MouseEvent.BUTTON1) {
//            Camera c = window.getWorld().getCamera();
//            camDeltaXOnPress = e.getX() - c.getX();
//            camDeltaYOnPress = e.getY() - c.getY();
//        }

        // Move camera by holding RMB
        if (buttonHold == MouseEvent.BUTTON3) {
            Camera c = window.getApp().getWorld().getCamera();
            camDeltaXOnPress = e.getX() - c.getX();
            camDeltaYOnPress = e.getY() - c.getY();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (buttonHold == e.getButton()) {
            buttonHold = -1;
            window.refresh();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Move camera by holding RMB
        if (buttonHold == MouseEvent.BUTTON3) {
            window.getApp().getWorld().getCamera().setLocation(
                    e.getX() - camDeltaXOnPress,
                    e.getY() - camDeltaYOnPress);
        }

        window.refresh();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        Camera c = window.getApp().getWorld().getCamera();
        Point2D p = window.getScreen().screenToLocalCoord(e.getX(), e.getY());
        c.zoom(
                -e.getWheelRotation(),
                p.getX(),
                p.getY());
        window.refresh();
    }
}
