package com.eggame;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        var label = new Label("Egg Game - Multiplayer");
        var scene = new Scene(new StackPane(label), 640, 480);
        stage.setScene(scene);
        stage.setTitle("Egg Game");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
