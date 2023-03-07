package org.variantsync.diffdetective.show.engine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class Window extends JFrame {
    private final InputHandler inputHandler;
    private final Screen screen;
    private App app;

    public Window(String title, int resolutionWidth, int resolutionHeight) {
        super(title);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(true);
        setSize(resolutionWidth, resolutionHeight);
        setLocationRelativeTo(null);

        this.inputHandler = new InputHandler(this);
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

    public InputHandler getInputHandler() {
        return inputHandler;
    }

    public Screen getScreen() {
        return screen;
    }
}
