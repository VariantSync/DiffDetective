package org.variantsync.diffdetective.show.variation;

import org.tinylog.Logger;
import org.variantsync.diffdetective.show.engine.Entity;
import org.variantsync.diffdetective.show.engine.InputListener;

import java.awt.event.KeyEvent;

public class MakeRootDance extends InputListener {
    private final DiffTreeApp app;
    private boolean dancing = false;
    private Dance dance;

    public MakeRootDance(DiffTreeApp app) {
        this.app = app;
        this.dance = new Dance();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_D) {
            final Entity rootNode = app.getEntityOf(app.getDiffTree().getRoot());
            if (dancing) {
                rootNode.remove(dance);
                app.removeUpdateable(dance);
            } else {
                dance.setCenter(rootNode.getLocation());
                dance.resetTime();
                rootNode.add(dance);
                app.addUpdateable(dance);
            }
            dancing = !dancing;
        }
    }
}
