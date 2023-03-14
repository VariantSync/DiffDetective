package org.variantsync.diffdetective.show.engine;

import org.variantsync.diffdetective.show.engine.geom.Vec2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class World {
    private final App app;
    private final Camera camera;

    private final List<Entity> entities;

    public World(App app) {
        this.app = app;

        entities = new ArrayList<>();
        camera = new Camera();
    }

    /** RENDERING **/

    public void spawn(Entity element) {
        entities.add(element);
    }

    public void despawn(Entity element) {
        entities.remove(element);
    }

    /** GET AND SET **/

    public App getApp() {
        return app;
    }

    public Camera getCamera() {
        return camera;
    }

    ///////////// MATH /////////////////

    public Vec2 worldToViewportCoord(final Vec2 pos) {
        return pos.transform(getApp().getWindow().getScreen().getViewTransform());
    }

    public Iterable<? extends Entity> getEntities() {
        return entities;
    }

    public void sortEntities() {
        entities.sort(Comparator.comparingDouble(Entity::getZ));
    }
}
