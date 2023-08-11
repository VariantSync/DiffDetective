package org.variantsync.diffdetective.show.engine.windowlayout;

import org.variantsync.diffdetective.show.engine.WindowLayout;
import org.variantsync.diffdetective.show.engine.geom.Box;
import org.variantsync.diffdetective.show.engine.geom.Vec2;
import org.variantsync.diffdetective.util.Assert;

import java.util.List;

/**
 * Layout for exactly two windows.
 * Makes the first window fill the left half of the screen and
 * makes the second window fill the right half of the screen.
 */
public class SideBySide extends WindowLayout {
    @Override
    protected List<Box> layoutWithin(Box box, int numberOfWindows) {
        Assert.assertTrue(numberOfWindows == 2, "SideBySide layout can only be used for exactly two windows but " + numberOfWindows + " were given.");

        final Vec2 halfScreenWidth = new Vec2(
                box.getWidth() / 2.0,
                0
        );

        return List.of(
                new Box(box.upperLeft(), box.lowerRight().minus(halfScreenWidth)),
                new Box(box.upperLeft().add(halfScreenWidth), box.lowerRight())
        );
    }
}
