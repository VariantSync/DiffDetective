package org.variantsync.diffdetective.show.engine;

import javax.swing.*;
import java.awt.*;

public class Window extends JFrame {
    private final Screen screen;
    private App app;

    public Window(String title, int resolutionWidth, int resolutionHeight) {
        super(title);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(true);
        setSize(resolutionWidth, resolutionHeight);
        setLocationRelativeTo(null);

        this.screen = new Screen(this);

        Container contentPane = getContentPane();
        contentPane.add(screen);
    }

    public void setApp(App app) {
        this.app = app;
    }

    public App getApp() {
        return app;
    }

    public void render() {
        screen.repaint();
    }

    public Screen getScreen() {
        return screen;
    }

    public void addInputListener(final InputListener inputListener) {
        inputListener.setWindow(this);
        screen.addMouseListener(inputListener);
        screen.addMouseMotionListener(inputListener);
        screen.addMouseWheelListener(inputListener);
    }

    public void removeInputListener(final InputListener inputListener) {
        screen.removeMouseListener(inputListener);
        screen.removeMouseMotionListener(inputListener);
        screen.removeMouseWheelListener(inputListener);
        inputListener.setWindow(null);
    }
}
