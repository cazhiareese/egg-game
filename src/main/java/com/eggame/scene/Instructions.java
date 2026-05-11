package com.eggame.scene;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.geometry.Insets;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

public class Instructions {

    private Scene scene;
    private SceneManager sceneManager;

    public Instructions(SceneManager sceneManager) {
        this.sceneManager = sceneManager;

        StackPane root = new StackPane();
        
        try {
            String bgUrl = getClass().getResource("/com/eggame/instructions.png").toExternalForm();
            root.setStyle("-fx-background-image: url('" + bgUrl + "'); " +
                          "-fx-background-size: cover; " +
                          "-fx-background-position: center center; " +
                          "-fx-background-repeat: no-repeat;");
        } catch (Exception e) {
            System.err.println("Failed to load instruction.png");
            root.setStyle("-fx-background-color: #A9D08E;"); 
        }

        //back button
        Button backButton = createBackButton("<");
        backButton.setOnAction(e -> {
            if (this.sceneManager != null) {
                this.sceneManager.switchToMainMenu();
            }
        });
        StackPane.setAlignment(backButton, Pos.TOP_LEFT);
        StackPane.setMargin(backButton, new Insets(30, 0, 0, 15));

        StackPane instructionBoxPane = new StackPane();
        
        //instruction image box - contains instructions text
        ImageView boxImgView = new ImageView();
        try {
            Image boxImg = new Image(getClass().getResourceAsStream("/com/eggame/instruction_box.png"));
            boxImgView.setImage(boxImg);
            boxImgView.setFitWidth(500);
            boxImgView.setTranslateY(50);
            boxImgView.setPreserveRatio(true);
        } catch (Exception e) {
            System.err.println("Failed to load instruction_box.png");
        }

        //title text - How to Play
        Text titleText = new Text("How to Play");
        titleText.setFill(javafx.scene.paint.Color.web("#FFF7D6"));
        titleText.setStroke(javafx.scene.paint.Color.web("#60312B"));
        titleText.setStrokeType(javafx.scene.shape.StrokeType.OUTSIDE);
        titleText.setStrokeWidth(3);
        
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

        StackPane.setAlignment(titleText, Pos.TOP_CENTER);
        StackPane.setMargin(titleText, new Insets(40, 0, 0, 0));
        
        instructionBoxPane.getChildren().addAll(boxImgView, titleText);
        
        StackPane.setAlignment(instructionBoxPane, Pos.CENTER);
        
        TranslateTransition tt = new TranslateTransition(Duration.seconds(3.5), boxImgView);
        tt.setByY(15);
        tt.setCycleCount(TranslateTransition.INDEFINITE);
        tt.setAutoReverse(true);
        tt.play();

        root.getChildren().addAll(instructionBoxPane, backButton);

        this.scene = new Scene(root, Game.WINDOW_WIDTH, Game.WINDOW_HEIGHT);
    }

    //back button function
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
        String hoverStyle   = "-fx-background-color: #D69D58; -fx-background-radius: 15; -fx-border-color: #60312B; -fx-border-radius: 15; -fx-border-width: 4;";
        
        btn.setStyle(defaultStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(defaultStyle));
        
        return btn;
    }

    public Scene getScene() {
        return scene;
    }
}
