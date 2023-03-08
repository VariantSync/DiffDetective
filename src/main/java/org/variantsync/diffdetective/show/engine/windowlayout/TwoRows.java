package org.variantsync.diffdetective.show.engine.windowlayout;

import org.variantsync.diffdetective.show.engine.WindowLayout;
import org.variantsync.diffdetective.show.engine.geom.Box;
import org.variantsync.diffdetective.show.engine.geom.Vec2;

import java.util.ArrayList;
import java.util.List;

/**
 * Layout for exactly any number of windows.
 * Makes the windows appear in two equally packed rows.
 * In case an odd number of windows is given, the second row
 * will have its last spot empty.
 */
public class TwoRows extends WindowLayout {
    @Override
    public List<Box> layoutWithin(Box box, int numberOfWindows) {
        final List<Box> layout = new ArrayList<>(numberOfWindows);

        final int numWindowsSecondRow = numberOfWindows / 2;
        final int numWindowsFirstRow  = numberOfWindows  - numWindowsSecondRow;

        final int windowWidth  = (int)(box.getWidth() / numWindowsFirstRow);
        final int windowHeight = (int)(box.getHeight() / 2.0);

        // first row
        Vec2 ul = box.upperLeft();
        for (int i = 0; i < numWindowsFirstRow; ++i) {
            layout.add(new Box(ul, windowWidth, windowHeight));
            ul = ul.add(windowWidth, 0);
        }

        // second row
        ul = box.upperLeft().add(0, windowHeight);
        for (int i = 0; i < numWindowsSecondRow; ++i) {
            layout.add(new Box(ul, windowWidth, windowHeight));
            ul = ul.add(windowWidth, 0);
        }

        return layout;
    }
}
