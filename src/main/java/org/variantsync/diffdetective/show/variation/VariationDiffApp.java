package org.variantsync.diffdetective.show.variation;

import org.tinylog.Logger;
import org.variantsync.diffdetective.show.engine.*;
import org.variantsync.diffdetective.show.engine.geom.Circle;
import org.variantsync.diffdetective.show.engine.geom.Vec2;
import org.variantsync.diffdetective.show.engine.hitbox.CircleHitbox;
import org.variantsync.diffdetective.show.engine.input.CameraDragAndDrop;
import org.variantsync.diffdetective.show.engine.input.ZoomViaMouseWheel;
import org.variantsync.diffdetective.show.variation.input.ExitOnEscape;
import org.variantsync.diffdetective.show.variation.input.NodeDragAndDrop;
import org.variantsync.diffdetective.util.StringUtils;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.DiffType;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.serialize.Format;
import org.variantsync.diffdetective.variation.diff.serialize.GraphvizExporter;
import org.variantsync.diffdetective.variation.diff.serialize.TikzExporter;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.DefaultEdgeLabelFormat;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.EdgeLabelFormat;
import org.variantsync.diffdetective.variation.diff.serialize.nodeformat.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VariationDiffApp<L extends Label> extends App {
    public final static double TIKZ_NODE_RADIUS = 6.5;
    public final static int TIKZ_FONT_SIZE = 5;
    public final static double DEFAULT_NODE_RADIUS = 50;
    public final static int DEFAULT_FONT_SIZE = 30;
    public final static <L extends Label> List<DiffNodeLabelFormat<L>> DEFAULT_FORMATS() {
        return List.of(
            new PaperNodeFormat<>(),
            new ShowNodeFormat<>(),
            new LabelOnlyDiffNodeFormat<>(),
            new EditClassesDiffNodeFormat<>(),
            new LineNumberFormat<>(),
            new FormulasAndLineNumbersNodeFormat<>()
        );
    }

    public final static double BOX_ALIGN_INDENT = 0.7;

    private final Format<L> defaultFormat;

    private final List<DiffNodeLabelFormat<L>> availableFormats;
    private Vec2 resolution;
    private final VariationDiff<L> variationDiff;

    protected final Map<DiffNode<L>, Entity> nodes = new HashMap<>();

    private boolean rootDancing = false;
    private final Dance rootDance;

    private DiffNodeLabelFormat<L> currentFormat;

    public VariationDiffApp(final String name, final VariationDiff<L> variationDiff, Vec2 resolution, List<DiffNodeLabelFormat<L>> availableFormats) {
        super(new Window(name, (int)resolution.x(), (int)resolution.y()));
        this.variationDiff = variationDiff;
        this.resolution = resolution;
        this.rootDance = new Dance();
        this.availableFormats = availableFormats;
        currentFormat = availableFormats.get(0);
        defaultFormat =
            new Format<>(
                    currentFormat,
                    // There is a bug in the exporter currently that accidentally switches direction so as a workaround we revert it here.
                    new DefaultEdgeLabelFormat<>(EdgeLabelFormat.Direction.ParentToChild)
            );
    }

    private void setupMenu() {
        final Window w = getWindow();
        JMenuBar menuBar = new JMenuBar();
        //w.add(menuBar);
        w.setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        JMenu layoutMenu = new JMenu("Layout");
        JMenu labelMenu = new JMenu("Labels");
        JMenu helpMenu = new JMenu("Help");
        menuBar.add(fileMenu);
        menuBar.add(layoutMenu);
        menuBar.add(labelMenu);
        menuBar.add(helpMenu);

        // Screenshot
        final JMenuItem saveScreenshotMenuItem = new JMenuItem("Save Screenshot");
        saveScreenshotMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveScreenshotMenuItem.addActionListener(event -> saveScreenshot());
        fileMenu.add(saveScreenshotMenuItem);

        // Tikz export
        final JMenuItem tikzExportMenuItem = new JMenuItem("Tikz Export");
        tikzExportMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_MASK));
        tikzExportMenuItem.addActionListener(event -> saveTikz());
        fileMenu.add(tikzExportMenuItem);

        // Switch layout
        for (final GraphvizExporter.LayoutAlgorithm layoutAlgorithm : GraphvizExporter.LayoutAlgorithm.values()) {
            final JMenuItem setLayoutMenuItem = new JMenuItem(layoutAlgorithm.getExecutableName());
            setLayoutMenuItem.addActionListener(event -> layoutNodes(layoutAlgorithm));
            layoutMenu.add(setLayoutMenuItem);
        }

        // Switch labels
        for (final DiffNodeLabelFormat<L> labelFormat : availableFormats) {
            final JMenuItem setLabelFormatMenuItem = new JMenuItem(labelFormat.getShortName());
            setLabelFormatMenuItem.addActionListener(event -> setLabelFormat(labelFormat));
            labelMenu.add(setLabelFormatMenuItem);
        }

        // show root
        {
            final JMenuItem showRootMenuItem = new JMenuItem("Show Root");
            showRootMenuItem.setAccelerator(KeyStroke.getKeyStroke('r'));
            showRootMenuItem.addActionListener(event -> this.toggleRootDance());
            helpMenu.add(showRootMenuItem);
        }
    }

    private void setupInput() {
        getWindow().addInputListener(new CameraDragAndDrop(MouseEvent.BUTTON3));
        getWindow().addInputListener(new ZoomViaMouseWheel());
        getWindow().addInputListener(new NodeDragAndDrop(MouseEvent.BUTTON1));
        getWindow().addInputListener(new ExitOnEscape());
    }

    private void toggleRootDance() {
        final Entity rootNode = getEntityOf(getVariationDiff().getRoot());
        if (rootDancing) {
            rootNode.remove(rootDance);
            removeUpdateable(rootDance);
        } else {
            rootDance.setCenter(rootNode.getLocation());
            rootDance.resetTime();
            rootNode.add(rootDance);
            addUpdateable(rootDance);
        }

        rootDancing = !rootDancing;
    }

    private void saveScreenshot() {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(
                new FileNameExtensionFilter("Image Files", javax.imageio.ImageIO.getReaderFileSuffixes())
        );

        final int result = fileChooser.showSaveDialog(getWindow());
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

    private void saveTikz() {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(
                new FileNameExtensionFilter("TeX", "tex")
        );

        final int result = fileChooser.showSaveDialog(getWindow());
        if (result == JFileChooser.APPROVE_OPTION) {
            final File targetFile = fileChooser.getSelectedFile();
            final TikzExporter<L> tikzExporter = new TikzExporter<>(new Format<>(currentFormat, new DefaultEdgeLabelFormat(EdgeLabelFormat.Direction.ChildToParent)));

            try (
                    var unbufferedOutput = Files.newOutputStream(targetFile.toPath());
                    var output = new BufferedOutputStream(unbufferedOutput)
            ) {
                final double millimetersPerPixel = TIKZ_NODE_RADIUS / (BOX_ALIGN_INDENT * resolution.x());
                final Vec2 flipY  = new Vec2(1.0, -1.0);
                final Vec2 scaleToTikz =
                        Vec2.all(millimetersPerPixel);
//                        Vec2.all(TIKZ_NODE_RADIUS).dividedBy(resolution);

                tikzExporter.exportVariationDiff(
                        variationDiff,
                        node -> {
                            Vec2 pos = nodes.get(node).getLocation();
                            pos = pos.scale(flipY);
                            pos = pos.scale(scaleToTikz);
                            return pos;
                        },
                        output,
                        true
                        );
            } catch (IOException e) {
                JOptionPane.showMessageDialog(
                        getWindow(),
                        "Export to "
                                + targetFile
                                + " failed because:"
                                + StringUtils.LINEBREAK
                                + e,
                        "Tikz export failed",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void setLabelFormat(DiffNodeLabelFormat<L> labelFormat) {
        this.currentFormat = labelFormat;
    }

    DiffNodeLabelFormat<? super L> getLabelFormat() {
        return currentFormat;
    }

    private void layoutNodes(GraphvizExporter.LayoutAlgorithm layoutAlgorithm) {
        final Map<DiffNode<?>, Vec2> locations;
        try {
            locations = calculateLayout(
                    variationDiff,
                    layoutAlgorithm,
                    defaultFormat);
        } catch (IOException e) {
            Logger.error(e);
            return;
        }

        alignInBox(resolution, locations);
        locateVariationDiffNodesAt(locations);
    }

    private void spawnVariationDiff(final World world) {
        // Create entities for all nodes
        variationDiff.forAll(diffNode -> {
            final Entity e = new Entity();
            e.add(new CircleHitbox(new Circle(DEFAULT_NODE_RADIUS)));
            e.add(new GraphNodeGraphics(
                    new NodeView<L>(
                            diffNode,
                            this
                    )
            ));
            e.setZ(Z.FOR_NODES);
            nodes.put(diffNode, e);
            world.spawn(e);
        });

        // layout the nodes
        layoutNodes(GraphvizExporter.LayoutAlgorithm.DOT);

        // spawn the edges
        final List<EdgeGraphics> edges = new ArrayList<>();
        getVariationDiff().forAll(node -> {
            final DiffNode<?> pbefore = node.getParent(Time.BEFORE);
            final DiffNode<?> pafter = node.getParent(Time.AFTER);

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
            e.setZ(Z.FOR_EDGES);
            world.spawn(e);
        }
    }

    @Override
    public void initialize(final World world) {
        resolution = new Vec2(getWindow().getWidth(), getWindow().getHeight());
        setupMenu();
        setupInput();
        spawnVariationDiff(world);

        getWindow().addComponentListener(new ComponentAdapter() {
                                             @Override
                                             public void componentResized(ComponentEvent e) {
                                                 super.componentResized(e);
                                                 resolution = Vec2.from(getWindow().getScreen().getSize());
                                             }
                                         }
        );
    }

    public Entity getEntityOf(DiffNode<?> diffNode) {
        return nodes.get(diffNode);
    }

    public VariationDiff<?> getVariationDiff() {
        return variationDiff;
    }

    public static <L extends Label> Map<DiffNode<?>, Vec2> calculateLayout(
            VariationDiff<L> d,
            GraphvizExporter.LayoutAlgorithm layout,
            Format<L> format
    ) throws IOException {
        final Map<DiffNode<?>, Vec2> locations = new HashMap<>();

        GraphvizExporter.layoutNodesIn(d, format, layout,
                (id, x, y) -> locations.put(
                        d.getNodeWithID(id),
                        new Vec2(x, y)
                ));

        return locations;
    }

    public void locateVariationDiffNodesAt(final Map<DiffNode<?>, Vec2> locations) {
        for (final Map.Entry<DiffNode<?>, Vec2> entry : locations.entrySet()) {
            final Entity e = nodes.get(entry.getKey());
            e.setLocation(entry.getValue());
        }
    }

    public static <V> void alignInBox(Vec2 resolution, final Map<V, Vec2> locations) {
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
        double scale_x = resolution.x() / layout_width;
        double scale_y = resolution.y() / layout_height;

        if (xmin == xmax) {
            scale_x = 1;
            xmin -= resolution.x() / 2.0;
        }
        if (ymin == ymax) {
            scale_y = 1;
            ymin -= resolution.y() / 2.0;
        }

        // TODO: Use vector arithmetics here instead of operating on individual components
        for (final Map.Entry<V, Vec2> entry : locations.entrySet()) {
            final Vec2 graphvizpos = entry.getValue();
            entry.setValue(new Vec2(
                     BOX_ALIGN_INDENT * (scale_x * (graphvizpos.x() - xmin) - resolution.x() / 2.0),
                    -BOX_ALIGN_INDENT * (scale_y * (graphvizpos.y() - ymin) - resolution.y() / 2.0)
            ));
        }
    }
}
