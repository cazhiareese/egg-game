package com.eggame.scene;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;

public class Game {
    private static Scene gameScene;

    protected Group root;

    protected Canvas canvas;

    protected Canvas bg;
    public final static int WINDOW_WIDTH = 1200;
    public final static int WINDOW_HEIGHT = 700;

    public Game() {
        this.root = new Group();
        Game.gameScene = new Scene(root);
        this.canvas = new Canvas(Game.WINDOW_WIDTH, Game.WINDOW_HEIGHT);
        this.bg = new Canvas(Game.WINDOW_WIDTH, Game.WINDOW_HEIGHT);

        this.root.getChildren().addAll(this.bg, this.canvas);
    }

    public static Scene getScene() {
        return gameScene;
    }
}
