package org.variantsync.diffdetective.show.engine;

import java.awt.event.*;

public abstract class InputListener implements MouseListener, MouseMotionListener, MouseWheelListener {
    private Window window;

    void setWindow(Window window) {
        this.window = window;
    }

    public Window getWindow() {
        return window;
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {}

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {}
}
