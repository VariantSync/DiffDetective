package org.variantsync.diffdetective.show.engine;

import java.util.ArrayList;
import java.util.List;

public class World {
    private final Window window;
    private final Camera camera;

    private final List<Entity> entities;

    public World(Window frame) {
        this.window = frame;

        entities = new ArrayList<>();
        camera = new Camera();
    }

    /** RENDERING **/

    public void spawn(Entity element) {
        entities.add(element);
        window.refresh();
    }

    public void despawn(Entity element) {
        entities.remove(element);
        window.refresh();
    }

    /** GET AND SET **/

    public Window getWindow() {
        return window;
    }

    public Camera getCamera() {
        return camera;
    }

    ///////////// MATH /////////////////

//    public Point2D screenToTextureCoord(int x, int y) {
//        return screenToTextureCoord((double)x, (double)y);
//    }
//
//    public Point2D screenToTextureCoord(double x, double y) {
//        try {
//            return mainImage.getAbsoluteTransform(viewTransform).inverseTransform(
//                    new Point2D.Double(x, y),
//                    null);
//        } catch (java.awt.geom.NoninvertibleTransformException e) {System.out.println(e);}
//        return null;
//    }

//    public Point2D textureToScreenCoord(int x, int y) {
//        return mainImage.getAbsoluteTransform(viewTransform).transform(new Point(x, y), null);
//    }

    public Iterable<? extends Entity> getEntities() {
        return entities;
    }
}
