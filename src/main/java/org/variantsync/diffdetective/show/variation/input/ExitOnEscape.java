package org.variantsync.diffdetective.show.variation.input;

import org.variantsync.diffdetective.show.engine.InputListener;

import java.awt.event.KeyEvent;

public class ExitOnEscape extends InputListener {
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            getWindow().dispose();
        }
    }
}
