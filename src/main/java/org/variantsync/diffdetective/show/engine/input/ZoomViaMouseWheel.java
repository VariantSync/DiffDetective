package org.variantsync.diffdetective.show.engine.input;

import org.variantsync.diffdetective.show.engine.Camera;
import org.variantsync.diffdetective.show.engine.InputListener;
import org.variantsync.diffdetective.show.engine.geom.Vec2;

import java.awt.event.MouseWheelEvent;

public class ZoomViaMouseWheel extends InputListener {
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        final Camera c = getWindow().getApp().getWorld().getCamera();
        c.zoomTowards(
                -e.getWheelRotation(),
                getWindow().getScreen().screenToViewportCoord(new Vec2(e.getX(), e.getY()))
        );
    }
}
