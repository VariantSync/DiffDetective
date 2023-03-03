package org.variantsync.diffdetective.show;

import org.tinylog.Logger;
import org.variantsync.diffdetective.show.diff.DiffNodeGraphics;
import org.variantsync.diffdetective.show.engine.Entity;
import org.variantsync.diffdetective.show.engine.Window;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.serialize.Format;
import org.variantsync.diffdetective.variation.diff.serialize.GraphvizExporter;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.DefaultEdgeLabelFormat;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.EdgeLabelFormat;
import org.variantsync.diffdetective.variation.diff.serialize.nodeformat.ShowNodeFormat;
import org.variantsync.functjonal.Pair;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Show {
    private final static Format format =
        new Format(
                new ShowNodeFormat(),
                // There is a bug in the exporter currently that accidentally switches direction so as a workaround we revert it here.
                new DefaultEdgeLabelFormat(EdgeLabelFormat.Direction.ParentToChild)
        );
    
    public static void show(final DiffTree d) {
        final int resx = 800;
        final int resy = 600;
        Window w = new Window(d.getSource().toString(), resx, resy);

        // If desired, this would be the point to insert a game loop.
        // For now, we redraw after every action.
        final Map<DiffNode, Pair<Double, Double>> locations = new HashMap<>();
        double[] xmin = new double[]{Double.MAX_VALUE};
        double[] xmax = new double[]{Double.MIN_VALUE};
        double[] ymin = new double[]{Double.MAX_VALUE};
        double[] ymax = new double[]{Double.MIN_VALUE};
        try {
            GraphvizExporter.layoutNodesIn(d, format, GraphvizExporter.LayoutAlgorithm.DOT,
                    (id, x, y) -> {
                        xmin[0] = Math.min(xmin[0], x);
                        xmax[0] = Math.max(xmax[0], x);
                        ymin[0] = Math.min(ymin[0], y);
                        ymax[0] = Math.max(ymax[0], y);
                        locations.put(
                                d.getNodeWithID(id),
                                new Pair<>(x, y)
                        );
                    });
        } catch (IOException e) {
            Logger.error(e);
            w.dispose();
            return;
        }

        Logger.debug("xmin " + xmin[0]);
        Logger.debug("xmax " + xmax[0]);
        Logger.debug("ymin " + ymin[0]);
        Logger.debug("ymax " + ymax[0]);

        double layout_width  = xmax[0] - xmin[0];
        double layout_height = ymax[0] - ymin[0];
        Logger.debug("layout_width " + layout_width);
        Logger.debug("layout_height " + layout_height);
        double scale_x = resx / layout_width;
        double scale_y = resy / layout_height;
        Logger.debug("scale_x " + scale_x);
        Logger.debug("scale_y " + scale_y);

        if (xmin[0] == xmax[0]) {
            scale_x = 1;
            xmin[0] -= resx/2.0;
        }
        if (ymin[0] == ymax[0]) {
            scale_y = 1;
            ymin[0] -= resy/2.0;
        }

        for (Map.Entry<DiffNode, Pair<Double, Double>> entry : locations.entrySet()) {
            Entity e = new Entity(new DiffNodeGraphics(entry.getKey()));
            e.setLocation(
                     0.7 * (scale_x * (entry.getValue().first()  - xmin[0]) - resx / 2.0),
                    -0.7 * (scale_y * (entry.getValue().second() - ymin[0]) - resy / 2.0)
            );
            w.getWorld().spawn(e);
        }

        w.setVisible(true);
    }
}
