package com.eggame;

import com.eggame.scene.SceneManager;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    private static final String GAME_TITLE = "EGGciting Hunt!";

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle(GAME_TITLE);

        SceneManager sceneManager = new SceneManager(primaryStage);
        sceneManager.switchToMainMenu();

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
