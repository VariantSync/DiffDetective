package org.variantsync.diffdetective.show.engine;

import org.tinylog.Logger;
import org.variantsync.diffdetective.util.Clock;

public class GameEngine {
    public static final int DEFAULT_TARGET_FPS = 60;

    private final App app;
    private final int targetFPS;
    private Thread appThread;

    public GameEngine(App app) {
        this(DEFAULT_TARGET_FPS, app);
    }
    public GameEngine(int targetFPS, App app) {
        this.app = app;
        this.targetFPS = targetFPS;
    }

    public void show() {
        appThread = new Thread(this::gameloop);
        appThread.start();
    }

    private void gameloop() {
        app.start();

        final Window window = app.getWindow();
        final Clock frametimeClock = new Clock();

        final double targetSecondsPerFrame = 1.0 / targetFPS;

        while (window.isShowing()) {
            frametimeClock.start();

            app.update();
            app.render();

            final double secondsPerFrame = frametimeClock.getPassedSeconds();
            final double secondsToWait = targetSecondsPerFrame - secondsPerFrame;

            if (secondsToWait > 0) {
                try {
                    final long millisecondsToWait = (long) (1000L * secondsToWait);
                    Thread.sleep(millisecondsToWait);
                } catch (InterruptedException e) {
                    Logger.error(e);
                }
            }
        }
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
