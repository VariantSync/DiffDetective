package org.variantsync.diffdetective.show.engine;

import org.tinylog.Logger;

public abstract class App {
    private final Window window;
    private World world;
    private boolean initialized = false;

    protected App(Window window) {
        this.window = window;
        this.window.setApp(this);
    }

    protected abstract void initialize(final World world);

    protected void start() {
        world = new World(this);
        initialize(world);
        initialized = true;
        window.setVisible(true);
    }

    protected void update() {

    }

    protected void render() {
        if (initialized) {
            window.render();
        }
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
