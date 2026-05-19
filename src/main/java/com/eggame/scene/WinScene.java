package com.eggame.scene;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.animation.ScaleTransition;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.geometry.Insets;

public class WinScene {

    private Scene scene;
    private SceneManager sceneManager;

    public WinScene(SceneManager sceneManager, String header, String scoreboard) {
        this.sceneManager = sceneManager;

        StackPane root = new StackPane();

        try {
            String bgUrl = getClass().getResource("/com/eggame/instructions.png").toExternalForm();
            root.setStyle("-fx-background-image: url('" + bgUrl + "'); " +
                    "-fx-background-size: cover; " +
                    "-fx-background-position: center center; " +
                    "-fx-background-repeat: no-repeat;");
        } catch (Exception e) {
            root.setStyle("-fx-background-color: #A9D08E;");
        }

        // back button - resets game
        Button backButton = createBackButton("<");
        backButton.setOnAction(e -> {
            if (this.sceneManager != null) {
                if (this.sceneManager.getGame() != null) {
                    this.sceneManager.getGame().resetGame();
                }
                this.sceneManager.switchToMainMenu();
            }
        });
        StackPane.setAlignment(backButton, Pos.TOP_LEFT);
        StackPane.setMargin(backButton, new Insets(30, 0, 0, 15));

        VBox contentBox = new VBox(30);
        contentBox.setAlignment(Pos.CENTER);
        StackPane.setMargin(contentBox, new Insets(0, 0, 50, 0));

        // title text
        Text titleText = new Text(header.toUpperCase());
        titleText.setFill(javafx.scene.paint.Color.web("#fff7d6"));
        titleText.setStroke(javafx.scene.paint.Color.web("#789f23"));
        titleText.setStrokeType(javafx.scene.shape.StrokeType.OUTSIDE);
        titleText.setStrokeWidth(4);

        try {
            Font customFontTitle = Font.loadFont(getClass().getResourceAsStream("/com/eggame/fonts.ttf"), 64);
            if (customFontTitle != null) {
                titleText.setFont(customFontTitle);
            } else {
                titleText.setFont(Font.font("Quicksand", 64));
            }
        } catch (Exception e) {
            titleText.setFont(Font.font("Quicksand", 64));
        }

        // scoreboard panel
        StackPane scoreboardPane = new StackPane();
        scoreboardPane.setMaxWidth(450);
        scoreboardPane.setMaxHeight(250);
        scoreboardPane.setStyle(
                "-fx-background-color: #9A5A41; " +
                        "-fx-background-radius: 25; " +
                        "-fx-border-color: #B97A5E; " +
                        "-fx-border-width: 8; " +
                        "-fx-border-radius: 20; " +
                        "-fx-padding: 30;");

        VBox scoreContent = new VBox(15);
        scoreContent.setAlignment(Pos.CENTER);

        Text scoreTitle = new Text("Scoreboard");
        scoreTitle.setFill(javafx.scene.paint.Color.web("#FFF7D6"));
        scoreTitle.setStroke(javafx.scene.paint.Color.web("#60312B"));
        scoreTitle.setStrokeType(javafx.scene.shape.StrokeType.OUTSIDE);
        scoreTitle.setStrokeWidth(3);

        try {
            Font customFontTitle = Font.loadFont(getClass().getResourceAsStream("/com/eggame/fonts.ttf"), 64);
            if (customFontTitle != null) {
                scoreTitle.setFont(customFontTitle);
            } else {
                scoreTitle.setFont(Font.font("Quicksand", 64));
            }
        } catch (Exception e) {
            scoreTitle.setFont(Font.font("Quicksand", 64));
        }

        // scoreboard text
        Text scoreText = new Text(scoreboard);
        scoreText.setFill(javafx.scene.paint.Color.web("#FFF7D6"));
        scoreText.setStroke(javafx.scene.paint.Color.web("#60312B"));
        scoreText.setStrokeType(javafx.scene.shape.StrokeType.OUTSIDE);
        scoreText.setStrokeWidth(2);
        scoreText.setTextAlignment(TextAlignment.CENTER);

        try {
            Font customFontScore = Font.loadFont(getClass().getResourceAsStream("/com/eggame/fonts.ttf"), 36);
            if (customFontScore != null) {
                scoreText.setFont(customFontScore);
            } else {
                scoreText.setFont(Font.font("Quicksand", 36));
            }
        } catch (Exception e) {
            scoreText.setFont(Font.font("Quicksand", 36));
        }

        // get the scoreboard from the game
        scoreContent.getChildren().addAll(scoreTitle, scoreText);
        scoreboardPane.getChildren().add(scoreContent);

        contentBox.getChildren().addAll(titleText, scoreboardPane);

        StackPane.setAlignment(contentBox, Pos.CENTER);

        root.getChildren().addAll(contentBox, backButton);

        this.scene = new Scene(root, Game.WINDOW_WIDTH, Game.WINDOW_HEIGHT);
    }

    // back button style
    private Button createBackButton(String text) {
        Button btn = new Button();
        Text btnText = new Text(text);

        try {
            Font customFont = Font.loadFont(getClass().getResourceAsStream("/com/eggame/fonts.ttf"), 48);
            if (customFont != null) {
                btnText.setFont(customFont);
            } else {
                btnText.setFont(Font.font("Quicksand", 36));
            }
        } catch (Exception e) {
            btnText.setFont(Font.font("Quicksand", 36));
        }

        btnText.setFill(javafx.scene.paint.Color.web("#FFF7D6"));
        btnText.setStroke(javafx.scene.paint.Color.web("#60312B"));
        btnText.setStrokeType(javafx.scene.shape.StrokeType.OUTSIDE);
        btnText.setStrokeWidth(3);

        btn.setGraphic(btnText);
        btn.setPrefWidth(80);
        btn.setPrefHeight(60);

        String defaultStyle = "-fx-background-color: #C48C47; -fx-background-radius: 15; -fx-border-color: #60312B; -fx-border-radius: 15; -fx-border-width: 4;";
        String hoverStyle = "-fx-background-color: #D69D58; -fx-background-radius: 15; -fx-border-color: #60312B; -fx-border-radius: 15; -fx-border-width: 4;";

        btn.setStyle(defaultStyle);
        btn.setOnMouseEntered(e -> {
            btn.setStyle(hoverStyle);
            ScaleTransition st = new ScaleTransition(javafx.util.Duration.millis(100), btn);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });
        
        btn.setOnMouseExited(e -> {
            btn.setStyle(defaultStyle);
            ScaleTransition st = new ScaleTransition(javafx.util.Duration.millis(100), btn);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        return btn;
    }

    public Scene getScene() {
        return scene;
    }
}
