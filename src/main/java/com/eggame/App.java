package com.eggame;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class App extends Application {

    private static final String GAME_TITLE = "EGGciting Hunt!";
    Stage stage;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;

        try {
            stage.setScene(Game.getScene());
            stage.setTitle(GAME_TITLE);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
