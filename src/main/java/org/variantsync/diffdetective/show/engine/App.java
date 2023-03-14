package org.variantsync.diffdetective.show.engine;

import java.util.ArrayList;
import java.util.List;

public abstract class App implements Updateable {
    private final Window window;
    private World world;
    private final List<Updateable> updateables;
    private boolean initialized = false;

    protected App(Window window) {
        this.window = window;
        this.window.setApp(this);
        this.updateables = new ArrayList<>();
    }

    protected abstract void initialize(final World world);

    protected void start() {
        world = new World(this);
        initialize(world);
        initialized = true;
    }

    @Override
    public void update(double deltaSeconds) {
        for (Updateable u : updateables) {
            u.update(deltaSeconds);
        }
    }

    protected void render() {
        if (initialized) {
            window.render();
        }
    }

    public void addUpdateable(final Updateable updateable) {
        updateables.add(updateable);
    }

    public void removeUpdateable(final Updateable updateable) {
        updateables.remove(updateable);
    }

    public void setWorld(final World world) {
        this.world = world;
    }

    public World getWorld() {
        return world;
    }

    public Window getWindow() {
        return window;
    }
}
