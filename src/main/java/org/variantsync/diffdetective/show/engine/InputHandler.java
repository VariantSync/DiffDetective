package org.variantsync.diffdetective.show.engine;

import org.variantsync.diffdetective.show.engine.geom.Vec2;
import org.variantsync.diffdetective.show.engine.hitbox.Hitbox;

import java.awt.event.*;

public class InputHandler implements MouseListener, MouseMotionListener, MouseWheelListener {
    private final Window window;

    private Vec2 camDelta, dragTargetDelta;
    private Hitbox dragTarget;
    private int buttonHold;

    public InputHandler(Window window) {
        this.window = window;
        buttonHold = -1;
    }

    private void cancelButtonHoldAction() {
        buttonHold = -1;
        dragTarget = null;
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

        final Vec2 clickPos = new Vec2(e.getX(), e.getY());

        // Drag nodes with LMB
        if (buttonHold == MouseEvent.BUTTON1) {
            final Vec2 clickPosInWorldSpace = window.getScreen().screenToLocalCoord(clickPos);

            for (Entity entity : window.getApp().getWorld().getEntities()) {
                final Hitbox hitbox = entity.get(Hitbox.class);
                if (hitbox != null) {
                    if (hitbox.contains(clickPosInWorldSpace)) {
                        dragTarget = hitbox;
                        dragTargetDelta = clickPosInWorldSpace.minus(hitbox.getEntity().getLocation());
                        break;
                    }
                }
            }
        }

        // Move camera by holding RMB
        if (buttonHold == MouseEvent.BUTTON3) {
            Camera c = window.getApp().getWorld().getCamera();
            camDelta = clickPos.minus(c.getLocation());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (buttonHold == e.getButton()) {
            cancelButtonHoldAction();
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
        final Vec2 clickPos = new Vec2(e.getX(), e.getY());

        // Drag nodes with LMB
        if (buttonHold == MouseEvent.BUTTON1) {
            if (dragTarget != null) {
                final Vec2 clickPosInWorldSpace = window.getScreen().screenToLocalCoord(clickPos);
                dragTarget.getEntity().setLocation(clickPosInWorldSpace.minus(dragTargetDelta));
            }
        }

        // Move camera by holding RMB
        if (buttonHold == MouseEvent.BUTTON3) {
            window.getApp().getWorld().getCamera().setLocation(clickPos.minus(camDelta));
        }

        window.refresh();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        final Camera c = window.getApp().getWorld().getCamera();
        c.zoomTowards(
                -e.getWheelRotation(),
                window.getScreen().screenToLocalCoord(new Vec2(e.getX(), e.getY()))
        );
        window.refresh();
    }
}
