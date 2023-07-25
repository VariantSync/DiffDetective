package org.variantsync.diffdetective.show.engine.input;

import org.variantsync.diffdetective.show.engine.Camera;
import org.variantsync.diffdetective.show.engine.InputListener;
import org.variantsync.diffdetective.show.engine.geom.Vec2;

import java.awt.event.MouseEvent;

public abstract class MouseDragListener extends InputListener {
    private final int dragMouseButton;
    private int buttonHold;

    public MouseDragListener(int dragMouseButton) {
        this.dragMouseButton = dragMouseButton;
    }

    private void cancelButtonHoldAction() {
        dragEnd();
        buttonHold = -1;
    }

    protected abstract void dragStart(Vec2 windowPos);
    protected abstract void dragUpdate(Vec2 windowPos);
    protected abstract void dragEnd();

    @Override
    public void mousePressed(MouseEvent e) {
        if (buttonHold == -1 || buttonHold == e.getButton())
            buttonHold = e.getButton();
        else
            cancelButtonHoldAction();

        if (buttonHold == dragMouseButton) {
            dragStart(new Vec2(e.getX(), e.getY()));
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (buttonHold == e.getButton()) {
            cancelButtonHoldAction();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Move camera by holding RMB
        if (buttonHold == dragMouseButton) {
            dragUpdate(new Vec2(e.getX(), e.getY()));
        }
    }
}
