package org.variantsync.diffdetective.show.diff;

import org.tinylog.Logger;
import org.variantsync.diffdetective.show.engine.*;
import org.variantsync.diffdetective.show.engine.geom.Vec2;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.DiffType;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.serialize.Format;
import org.variantsync.diffdetective.variation.diff.serialize.GraphvizExporter;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.DefaultEdgeLabelFormat;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.EdgeLabelFormat;
import org.variantsync.diffdetective.variation.diff.serialize.nodeformat.ShowNodeFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowGraphApp extends App {
    private final static Format DEFAULT_FORMAT =
            new Format(
                    new ShowNodeFormat(),
                    // There is a bug in the exporter currently that accidentally switches direction so as a workaround we revert it here.
                    new DefaultEdgeLabelFormat(EdgeLabelFormat.Direction.ParentToChild)
            );
    private final int resx;
    private final int resy;
    private final DiffTree d;

    final Map<DiffNode, DiffNodeGraphics> nodeGraphics = new HashMap<>();

    public static Map<DiffNode, Vec2> computeNodeLayout(
            DiffTree d,
            GraphvizExporter.LayoutAlgorithm layout,
            Format format
    ) throws IOException {
        final Map<DiffNode, Vec2> locations = new HashMap<>();

        GraphvizExporter.layoutNodesIn(d, format, layout,
                (id, x, y) -> {
                    locations.put(
                            d.getNodeWithID(id),
                            new Vec2(x, y)
                    );
                });

        return locations;
    }

    public static void alignInBox(int width, int height, final Map<DiffNode, Vec2> locations) {
        double xmin = Double.MAX_VALUE;
        double xmax = Double.MIN_VALUE;
        double ymin = Double.MAX_VALUE;
        double ymax = Double.MIN_VALUE;

        for (final Map.Entry<DiffNode, Vec2> location : locations.entrySet()) {
            final double x = location.getValue().x();
            final double y = location.getValue().y();
            xmin = Math.min(xmin, x);
            xmax = Math.max(xmax, x);
            ymin = Math.min(ymin, y);
            ymax = Math.max(ymax, y);
        }

        Logger.debug("xmin " + xmin);
        Logger.debug("xmax " + xmax);
        Logger.debug("ymin " + ymin);
        Logger.debug("ymax " + ymax);

        double layout_width  = xmax - xmin;
        double layout_height = ymax - ymin;
        Logger.debug("layout_width " + layout_width);
        Logger.debug("layout_height " + layout_height);
        double scale_x = width / layout_width;
        double scale_y = height / layout_height;
        Logger.debug("scale_x " + scale_x);
        Logger.debug("scale_y " + scale_y);

        if (xmin == xmax) {
            scale_x = 1;
            xmin -= width / 2.0;
        }
        if (ymin == ymax) {
            scale_y = 1;
            ymin -= height / 2.0;
        }

        // TODO: Use vector arithmetics here instead of operating on individual components
        for (final Map.Entry<DiffNode, Vec2> entry : locations.entrySet()) {
            final Vec2 graphvizpos = entry.getValue();
            entry.setValue(new Vec2(
                     0.7 * (scale_x * (graphvizpos.x() - xmin) - width / 2.0),
                    -0.7 * (scale_y * (graphvizpos.y() - ymin) - height / 2.0)
            ));
        }
    }

    public ShowGraphApp(final DiffTree d, int resx, int resy) {
        super(new Window(d.getSource().toString(), resx, resy));
        this.d = d;
        this.resx = resx;
        this.resy = resy;
    }

    @Override
    public void initialize(final World world) {
        // If desired, this would be the point to insert a game loop.
        // For now, we redraw after every action.

        // compute node locations
        final Map<DiffNode, Vec2> locations;
        try {
            locations = computeNodeLayout(
                    d,
                    GraphvizExporter.LayoutAlgorithm.DOT,
                    DEFAULT_FORMAT);
        } catch (IOException e) {
            Logger.error(e);
            return;
        }
        alignInBox(resx, resy, locations);

        for (final Map.Entry<DiffNode, Vec2> entry : locations.entrySet()) {
            final DiffNodeGraphics graphics = new DiffNodeGraphics(entry.getKey(), DEFAULT_FORMAT.getNodeFormat());
            nodeGraphics.put(entry.getKey(), graphics);
            final Entity e = new Entity();
            e.add(graphics);
            e.setLocation(entry.getValue());
            world.spawn(e);
        }
        //// Edges

        final List<DiffEdgeGraphics> edges = new ArrayList<>();
        d.forAll(node -> {
            final DiffNode pbefore = node.getParent(Time.BEFORE);
            final DiffNode pafter = node.getParent(Time.AFTER);
            if (pbefore != null) {
                if (pafter != null) {
                    edges.add(new DiffEdgeGraphics(
                            nodeGraphics.get(node),
                            nodeGraphics.get(pbefore),
                            DiffType.NON
                    ));
                } else {
                    edges.add(new DiffEdgeGraphics(
                            nodeGraphics.get(node),
                            nodeGraphics.get(pbefore),
                            DiffType.REM
                    ));
                }
            } else if (pafter != null) {
                edges.add(new DiffEdgeGraphics(
                        nodeGraphics.get(node),
                        nodeGraphics.get(pafter),
                        DiffType.ADD
                ));
            }
        });
        for (DiffEdgeGraphics edge : edges) {
            Entity e = new Entity();
            e.add(edge);
            world.spawn(e);
        }

//        for (DiffNodeGraphics g : nodeGraphics.values()) {
//            world.spawn(g.getEntity());
//        }
    }
}
