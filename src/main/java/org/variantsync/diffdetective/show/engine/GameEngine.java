package org.variantsync.diffdetective.show.engine;

import org.tinylog.Logger;

public class GameEngine {
    private final App app;
    private Thread appThread;
    public GameEngine(App app) {
        this.app = app;
    }

    public void show() {
        appThread = new Thread(app::gameloop);
        appThread.start();
    }

    public void await() {
        try {
            appThread.join();
        } catch (InterruptedException e) {
            Logger.error(e);
        }
    }

    public void showAndAwait() {
        show();
        await();
    }

    public static void showAndAwaitAll(GameEngine... engines) {
        for (final GameEngine e : engines) {
            e.show();
        }
        for (final GameEngine e : engines) {
            e.await();
        }
    }
}
