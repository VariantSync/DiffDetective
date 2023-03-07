package org.variantsync.diffdetective.show.variation;

import org.variantsync.diffdetective.show.engine.*;
import org.variantsync.diffdetective.show.engine.geom.Vec2;
import org.variantsync.diffdetective.show.engine.hitbox.Hitbox;
import org.variantsync.diffdetective.show.engine.input.MouseDragListener;

public class NodeDragAndDrop extends MouseDragListener {
    private Vec2 dragTargetDelta;
    private Hitbox dragTarget;

    public NodeDragAndDrop(int dragMouseButton) {
        super(dragMouseButton);
    }

    @Override
    protected void dragEnd() {
        dragTarget = null;
    }

    @Override
    protected void dragStart(Vec2 windowPos) {
        final Window w = getWindow();
        final Screen screen = w.getScreen();
        final World world = w.getApp().getWorld();

        final Vec2 clickPosInWorldSpace = screen.screenToViewportCoord(windowPos);

        for (Entity entity : world.getEntities()) {
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

    @Override
    protected void dragUpdate(Vec2 windowPos) {
        if (dragTarget != null) {
            final Vec2 clickPosInWorldSpace = getWindow().getScreen().screenToViewportCoord(windowPos);
            dragTarget.getEntity().setLocation(clickPosInWorldSpace.minus(dragTargetDelta));
        }
    }
}
