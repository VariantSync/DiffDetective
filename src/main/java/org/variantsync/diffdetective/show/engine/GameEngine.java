package org.variantsync.diffdetective.show.engine;

import org.tinylog.Logger;
import org.variantsync.diffdetective.show.engine.windowlayout.Fullscreen;
import org.variantsync.diffdetective.show.engine.windowlayout.SideBySide;
import org.variantsync.diffdetective.show.engine.windowlayout.TwoRows;
import org.variantsync.diffdetective.util.Clock;

import java.util.Arrays;
import java.util.List;

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

    public App getApp() {
        return app;
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
        double secondsOfLastFrame = targetSecondsPerFrame;

        window.setVisible(true);

        while (window.isShowing()) {
            frametimeClock.start();

            app.update(targetSecondsPerFrame);
            app.render();

            secondsOfLastFrame = frametimeClock.getPassedSeconds();
            final double secondsToWait = targetSecondsPerFrame - secondsOfLastFrame;

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

    public Texture dontShowButRenderToTexture() {
        app.start();

        final Window w = app.getWindow();
        final Screen screen = w.getScreen();

        w.setMinimumSize(w.getSize());
        w.setUndecorated(true);
        w.pack();

        final Texture t = screen.screenshot();

        w.dispose();

        return t;
    }

    public static void showAndAwaitAll(GameEngine... engines) {
        WindowLayout layout;
        if (engines.length == 1) {
            layout = new Fullscreen();
        } else if (engines.length == 2) {
            layout = new SideBySide();
        } else {
            layout = new TwoRows();
        }

        showAndAwaitAll(layout, engines);
    }

    public static void showAndAwaitAll(WindowLayout windowLayout, GameEngine... engines) {
        final List<Window> windows = Arrays.stream(engines).map(GameEngine::getApp).map(App::getWindow).toList();
        windowLayout.layout(windows);

        for (final GameEngine e : engines) {
            e.show();
        }
        for (final GameEngine e : engines) {
            e.await();
        }
    }
}
