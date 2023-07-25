package org.variantsync.diffdetective.show.engine;


import org.variantsync.diffdetective.show.engine.geom.Box;
import org.variantsync.diffdetective.show.engine.geom.Vec2;
import org.variantsync.diffdetective.util.Assert;

import java.awt.*;
import java.util.List;

public abstract class WindowLayout {
    protected abstract List<Box> layoutWithin(Box box, int numberOfWindows);

    public List<Box> computeLayoutFor(final List<Window> windows) {
        if (windows.isEmpty()) {
            return List.of();
        }

        final Window w0 = windows.get(0);

        final GraphicsConfiguration config = w0.getGraphicsConfiguration();
        final Rectangle bounds = config.getBounds();
        final Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(config);

        final Vec2 ulBound = new Vec2(bounds.getX(), bounds.getY());
        return layoutWithin(
                new Box(
                        ulBound.add(
                                new Vec2(insets.left, insets.top)
                        ),
                        ulBound.add(
                                new Vec2(bounds.getWidth() - insets.right, bounds.getHeight() - insets.bottom)
                        )
                ),
                windows.size()
        );
    }

    public void applyLayout(final List<Box> layout, final List<Window> windows) {
        Assert.assertEquals(layout.size(), windows.size());
        for (int i = 0; i < layout.size(); ++i) {
            final Box bounds = layout.get(i);
            final Window w = windows.get(i);
            w.setLocation((int) bounds.upperLeft().x(), (int) bounds.upperLeft().y());
            w.setSize((int) bounds.getWidth(), (int) bounds.getHeight());
        }
    }

    public void layout(final List<Window> windows) {
        applyLayout(computeLayoutFor(windows), windows);
    }
}
