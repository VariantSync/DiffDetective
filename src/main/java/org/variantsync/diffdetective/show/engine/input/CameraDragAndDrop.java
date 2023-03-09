package org.variantsync.diffdetective.show.engine.input;

import org.variantsync.diffdetective.show.engine.Camera;
import org.variantsync.diffdetective.show.engine.geom.Vec2;

public final class CameraDragAndDrop extends MouseDragListener {
    private Vec2 camDelta;

    public CameraDragAndDrop(int dragAndDropMouseButton) {
        super(dragAndDropMouseButton);
    }

    @Override
    protected void dragStart(Vec2 windowPos) {
        Camera c = getWindow().getApp().getWorld().getCamera();
        camDelta = windowPos.minus(c.getLocation());
    }

    @Override
    protected void dragUpdate(Vec2 windowPos) {
        getWindow().getApp().getWorld().getCamera().setLocation(windowPos.minus(camDelta));
    }

    @Override
    protected void dragEnd() {

    }
}
