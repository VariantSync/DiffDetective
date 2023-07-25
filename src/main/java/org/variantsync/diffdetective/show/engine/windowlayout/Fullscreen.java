package org.variantsync.diffdetective.show.engine.windowlayout;

import org.variantsync.diffdetective.show.engine.WindowLayout;
import org.variantsync.diffdetective.show.engine.geom.Box;
import org.variantsync.diffdetective.util.Assert;

import java.util.List;

/**
 * Layout for exactly one window.
 * Makes the window fill the entire screen.
 */
public class Fullscreen extends WindowLayout {
    @Override
    protected List<Box> layoutWithin(Box box, int numberOfWindows) {
        Assert.assertTrue(numberOfWindows == 1, "Fullscreen layout can only be used for exactly one window but " + numberOfWindows + " were given.");
        return List.of(box);
    }
}
