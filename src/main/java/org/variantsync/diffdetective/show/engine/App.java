package org.variantsync.diffdetective.show.engine;

public abstract class App {
    private final Window window;
    private World world;
    private boolean initialized = false;

    protected App(Window window) {
        this.window = window;
        this.window.setApp(this);
    }

    protected abstract void initialize(final World world);

    public final void run() {
        world = new World(this);
        initialize(world);
        initialized = true;
        window.setVisible(true);
        refresh();
    }

    public void refresh() {
        if (initialized) {
            window.refresh();
        }
    }

    public void setWorld(final World world) {
        this.world = world;
        refresh();
    }

    public World getWorld() {
        return world;
    }

    public Window getWindow() {
        return window;
    }
}
