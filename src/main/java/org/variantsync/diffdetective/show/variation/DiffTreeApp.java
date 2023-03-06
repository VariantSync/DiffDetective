package org.variantsync.diffdetective.show.variation;

import org.tinylog.Logger;
import org.variantsync.diffdetective.show.engine.*;
import org.variantsync.diffdetective.show.engine.geom.Circle;
import org.variantsync.diffdetective.show.engine.geom.Vec2;
import org.variantsync.diffdetective.show.engine.hitbox.CircleHitbox;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.DiffType;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.serialize.Format;
import org.variantsync.diffdetective.variation.diff.serialize.GraphvizExporter;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.DefaultEdgeLabelFormat;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.EdgeLabelFormat;
import org.variantsync.diffdetective.variation.diff.serialize.nodeformat.ShowNodeFormat;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiffTreeApp extends App {
    private final static double DEFAULT_NODE_RADIUS = 50;
    private final static Format DEFAULT_FORMAT =
            new Format(
                    new ShowNodeFormat(),
                    // There is a bug in the exporter currently that accidentally switches direction so as a workaround we revert it here.
                    new DefaultEdgeLabelFormat(EdgeLabelFormat.Direction.ParentToChild)
            );
    private final int resx;
    private final int resy;
    private final DiffTree diffTree;

    protected final Map<DiffNode, Entity> nodes = new HashMap<>();

    private JMenuBar menuBar;

    public DiffTreeApp(final String name, final DiffTree diffTree, int resx, int resy) {
        super(new Window(name, resx, resy));
        this.diffTree = diffTree;
        this.resx = resx;
        this.resy = resy;
    }

    private void saveScreenshot() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(
                new FileNameExtensionFilter("Image Files", javax.imageio.ImageIO.getReaderFileSuffixes())
        );

        int result = fileChooser.showSaveDialog(getWindow());
        if (result == JFileChooser.APPROVE_OPTION) {
            final File targetFile = fileChooser.getSelectedFile();
            final Texture screenshot = getWindow().getScreen().screenshot();
            try {
                screenshot.saveAsPng(targetFile);
            } catch (IOException e) {
                Logger.error(e, "Could not save screenshot.");
            }
        }
    }

    private void setupMenu() {
        final Window w = getWindow();
        menuBar = new JMenuBar();
        //w.add(menuBar);
        w.setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        JMenu layoutMenu = new JMenu("Layout");
        menuBar.add(fileMenu);
        menuBar.add(layoutMenu);

        // Switch layout
        for (final GraphvizExporter.LayoutAlgorithm layoutAlgorithm : GraphvizExporter.LayoutAlgorithm.values()) {
            final JMenuItem setLayoutMenuItem = new JMenuItem(layoutAlgorithm.getExecutableName());
            setLayoutMenuItem.addActionListener(event -> layoutNodes(layoutAlgorithm));
            layoutMenu.add(setLayoutMenuItem);
        }

        // Screenshot
        final JMenuItem saveScreenshotMenuItem = new JMenuItem("Save Screenshot");
        saveScreenshotMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveScreenshotMenuItem.addActionListener(event -> saveScreenshot());
        fileMenu.add(saveScreenshotMenuItem);
    }

    private void layoutNodes(GraphvizExporter.LayoutAlgorithm layoutAlgorithm) {
        final Map<DiffNode, Vec2> locations;
        try {
            locations = calculateLayout(
                    diffTree,
                    layoutAlgorithm,
                    DEFAULT_FORMAT);
        } catch (IOException e) {
            Logger.error(e);
            return;
        }
        alignInBox(resx, resy, locations);
        locateDiffTreeNodesAt(locations);
        refresh();
    }

    private void spawnDiffTree(final World world) {
        // Create entities for all nodes
        diffTree.forAll(diffNode -> {
            final Entity e = new Entity();
            e.add(new CircleHitbox(new Circle(DEFAULT_NODE_RADIUS)));
            e.add(new GraphNodeGraphics<>(new DiffNodeView(diffNode, DEFAULT_FORMAT.getNodeFormat())));
            nodes.put(diffNode, e);
            world.spawn(e);
        });

        // layout the nodes
        layoutNodes(GraphvizExporter.LayoutAlgorithm.DOT);

        // spawn the edges
        final List<EdgeGraphics> edges = new ArrayList<>();
        getDiffTree().forAll(node -> {
            final DiffNode pbefore = node.getParent(Time.BEFORE);
            final DiffNode pafter = node.getParent(Time.AFTER);

            if (pbefore != null && pbefore == pafter) {
                edges.add(new EdgeGraphics(
                        nodes.get(node),
                        nodes.get(pbefore),
                        Colors.ofDiffType.get(DiffType.NON)
                ));
            } else {
                if (pbefore != null) {
                    edges.add(new EdgeGraphics(
                            nodes.get(node),
                            nodes.get(pbefore),
                            Colors.ofDiffType.get(DiffType.REM)
                    ));
                }
                if (pafter != null) {
                    edges.add(new EdgeGraphics(
                            nodes.get(node),
                            nodes.get(pafter),
                            Colors.ofDiffType.get(DiffType.ADD)
                    ));
                }
            }
        });

        for (final EdgeGraphics edge : edges) {
            Entity e = new Entity();
            e.add(edge);
            world.spawn(e);
        }
    }

    @Override
    public void initialize(final World world) {
        setupMenu();
        spawnDiffTree(world);
    }

    public DiffTree getDiffTree() {
        return diffTree;
    }

    public static Map<DiffNode, Vec2> calculateLayout(
            DiffTree d,
            GraphvizExporter.LayoutAlgorithm layout,
            Format format
    ) throws IOException {
        final Map<DiffNode, Vec2> locations = new HashMap<>();

        GraphvizExporter.layoutNodesIn(d, format, layout,
                (id, x, y) -> locations.put(
                        d.getNodeWithID(id),
                        new Vec2(x, y)
                ));

        return locations;
    }

    public void locateDiffTreeNodesAt(final Map<DiffNode, Vec2> locations) {
        for (final Map.Entry<DiffNode, Vec2> entry : locations.entrySet()) {
            final Entity e = nodes.get(entry.getKey());
            e.setLocation(entry.getValue());
        }
    }

    public static <V> void alignInBox(int width, int height, final Map<V, Vec2> locations) {
        double xmin = Double.MAX_VALUE;
        double xmax = Double.MIN_VALUE;
        double ymin = Double.MAX_VALUE;
        double ymax = Double.MIN_VALUE;

        for (final Map.Entry<V, Vec2> location : locations.entrySet()) {
            final double x = location.getValue().x();
            final double y = location.getValue().y();
            xmin = Math.min(xmin, x);
            xmax = Math.max(xmax, x);
            ymin = Math.min(ymin, y);
            ymax = Math.max(ymax, y);
        }

        double layout_width  = xmax - xmin;
        double layout_height = ymax - ymin;
        double scale_x = width / layout_width;
        double scale_y = height / layout_height;

        if (xmin == xmax) {
            scale_x = 1;
            xmin -= width / 2.0;
        }
        if (ymin == ymax) {
            scale_y = 1;
            ymin -= height / 2.0;
        }

        // TODO: Use vector arithmetics here instead of operating on individual components
        for (final Map.Entry<V, Vec2> entry : locations.entrySet()) {
            final Vec2 graphvizpos = entry.getValue();
            entry.setValue(new Vec2(
                    0.7 * (scale_x * (graphvizpos.x() - xmin) - width / 2.0),
                    -0.7 * (scale_y * (graphvizpos.y() - ymin) - height / 2.0)
            ));
        }
    }
}
