package com.eggame.scene;

import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class CustomizeMenu {

    private final SceneManager sceneManager;
    private final Scene scene;

    private int selectedHeadIndex;
    private int selectedHatIndex;
    private boolean isHeadTabActive = true;

    // Layers for preview avatar head and hat
    private final StackPane previewPane;
    private final ImageView headView;
    private final ImageView hatView;

    // Selection grid components
    private final GridPane optionsGrid;
    private final HBox tabHeader;
    private final Button headTabBtn;
    private final Button hatTabBtn;

    // Stuling for buttons and grid customization boxes
    private static final String TAB_ACTIVE_STYLE = "-fx-background-color: #94C843; -fx-background-radius: 24px; -fx-border-color: #60312B; -fx-border-width: 4px; -fx-border-radius: 20px; -fx-cursor: hand;";
    private static final String TAB_INACTIVE_STYLE = "-fx-background-color: #4E2C22; -fx-background-radius: 24px; -fx-border-color: #60312B; -fx-border-width: 4px; -fx-border-radius: 20px; -fx-cursor: hand;";

    private static final String GRID_CELL_STYLE = "-fx-background-color: #9D9D7C; -fx-background-radius: 20px; -fx-border-color: #60312B; -fx-border-width: 4px; -fx-border-radius: 16px; -fx-cursor: hand;";
    private static final String GRID_CELL_SELECTED = "-fx-background-color: #B497F8; -fx-background-radius: 20px; -fx-border-color: #60312B; -fx-border-width: 4px; -fx-border-radius: 16px; -fx-cursor: hand;";

    private Font customFont;

    public CustomizeMenu(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
        this.selectedHeadIndex = sceneManager.getAvatarState().getHeadIndex();
        this.selectedHatIndex = sceneManager.getAvatarState().getHatIndex();

        // Root Container
        StackPane root = new StackPane();

        // Background image
        String bgUrl = getClass().getResource("/com/eggame/customization/customize_bg.png").toExternalForm();
        root.setStyle("-fx-background-image: url('" + bgUrl + "'); " +
                "-fx-background-size: cover; " +
                "-fx-background-position: center center; " +
                "-fx-background-repeat: no-repeat;");

        // Font
        try {
            customFont = Font.loadFont(getClass().getResourceAsStream("/com/eggame/fonts.ttf"), 32);
        } catch (Exception e) {
            customFont = Font.font("Quicksand", 32);
        }

        // Layout Division (Left - Preview, Right - Options & Done button)
        HBox mainSplit = new HBox(80);
        mainSplit.setAlignment(Pos.CENTER);
        mainSplit.setPadding(new Insets(40));

        // Left Panel - Character Preview
        VBox leftPanel = new VBox(20);
        leftPanel.setAlignment(Pos.TOP_LEFT);
        leftPanel.setPrefWidth(500);

        previewPane = new StackPane();
        previewPane.setPrefSize(300, 300);
        previewPane.setTranslateX(-80);
        previewPane.setTranslateY(165);

        // Custom Avatar Head Preview
        headView = new ImageView();
        headView.setFitWidth(230);
        headView.setPreserveRatio(true);
        headView.setTranslateX(30);
        headView.setTranslateY(30);

        // Custom Hat Preview
        hatView = new ImageView();
        hatView.setFitWidth(190);
        hatView.setPreserveRatio(true);
        hatView.setTranslateX(30);
        hatView.setTranslateY(-100);

        previewPane.getChildren().addAll(headView, hatView);
        leftPanel.getChildren().add(previewPane);

        // Right Panel - Controls, Tabs & Done button
        VBox rightPanel = new VBox(25);
        rightPanel.setAlignment(Pos.CENTER);

        // Tab Bar
        tabHeader = new HBox(20);
        tabHeader.setAlignment(Pos.CENTER_LEFT);

        // customization tabs for avatar head and hats
        headTabBtn = createTabButton("/com/eggame/customization/head_tab.png", true);
        hatTabBtn = createTabButton("/com/eggame/customization/hat_tab.png", false);

        tabHeader.getChildren().addAll(headTabBtn, hatTabBtn);

        // Grid (2x2)
        optionsGrid = new GridPane();
        optionsGrid.setHgap(40);
        optionsGrid.setVgap(40);
        optionsGrid.setPadding(new Insets(10));

        // Done button to save avatar customization and go back to main menu
        Button doneBtn = createDoneButton();
        doneBtn.setOnAction(e -> {
            sceneManager.getAvatarState().setHeadIndex(selectedHeadIndex);
            sceneManager.getAvatarState().setHatIndex(selectedHatIndex);
            sceneManager.switchToMainMenu();
        });

        rightPanel.getChildren().addAll(tabHeader, optionsGrid, doneBtn);

        mainSplit.getChildren().addAll(leftPanel, rightPanel);
        root.getChildren().add(mainSplit);

        this.scene = new Scene(root, Game.WINDOW_WIDTH, Game.WINDOW_HEIGHT);

        // updates tabs and preview
        updateTabs();
        updatePreview();
    }

    // tab button for head and hat
    private Button createTabButton(String iconPath, boolean isHead) {
        Button btn = new Button();
        btn.setPrefSize(120, 20);
        btn.setTranslateX(70);

        try {
            ImageView icon = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
            icon.setFitWidth(55);
            icon.setPreserveRatio(true);
            icon.setTranslateY(0);
            btn.setGraphic(icon);
        } catch (Exception e) {
            btn.setText(isHead ? "HEAD" : "HAT");
        }

        btn.setOnAction(e -> {
            this.isHeadTabActive = isHead;
            updateTabs();
        });

        // hover scale effects
        applyHoverEffects(btn, 1.05);

        return btn;
    }

    // done button graphic
    private Button createDoneButton() {
        Button btn = new Button();
        btn.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-cursor: hand;");

        try {
            ImageView imgView = new ImageView(
                    new Image(getClass().getResourceAsStream("/com/eggame/customization/done_button.png")));
            imgView.setFitWidth(220);
            imgView.setPreserveRatio(true);
            btn.setGraphic(imgView);
        } catch (Exception e) {
            Text btnText = new Text("DONE");
            btnText.setFont(customFont);
            btnText.setFill(Color.web("#FFF7D6"));
            btnText.setStroke(Color.web("#60312B"));
            btnText.setStrokeWidth(2);
            btn.setGraphic(btnText);
            btn.setStyle(
                    "-fx-background-color: linear-gradient(from 0% 0% to 100% 0%, #94C843, #4E8A2B); -fx-background-radius: 15px; -fx-border-color: #60312B; -fx-border-width: 4px; -fx-border-radius: 12px; -fx-cursor: hand;");
            btn.setPrefSize(220, 55);
        }

        btn.setTranslateX(110);
        applyHoverEffects(btn, 1.08);

        return btn;
    }

    // update tabs and display options grid
    private void updateTabs() {
        optionsGrid.getChildren().clear();

        if (isHeadTabActive) {
            headTabBtn.setStyle(TAB_ACTIVE_STYLE);
            hatTabBtn.setStyle(TAB_INACTIVE_STYLE);
        } else {
            headTabBtn.setStyle(TAB_INACTIVE_STYLE);
            hatTabBtn.setStyle(TAB_ACTIVE_STYLE);
        }

        // Selection Grid for customization options
        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 2; c++) {
                int index = r * 2 + c;
                StackPane itemCell = new StackPane();
                itemCell.setPrefSize(170, 170);

                // change styling if selected
                int currentSelection = isHeadTabActive ? selectedHeadIndex : selectedHatIndex;
                if (index == currentSelection) {
                    itemCell.setStyle(GRID_CELL_SELECTED);
                } else {
                    itemCell.setStyle(GRID_CELL_STYLE);
                }

                // inner graphics for customization cells - avatar head and hats
                try {
                    String path = isHeadTabActive ? "/com/eggame/customization/head_" + index + ".png"
                            : "/com/eggame/customization/hat_" + index + ".png";
                    ImageView itemImg = new ImageView(new Image(getClass().getResourceAsStream(path)));
                    itemImg.setFitWidth(100);
                    itemImg.setPreserveRatio(true);

                    if (isHeadTabActive) {
                        itemImg.setTranslateY(8);
                    } else {
                        itemImg.setTranslateY(15);
                    }
                    itemCell.getChildren().add(itemImg);
                } catch (Exception e) {
                    System.err.println("Could not load grid thumbnail index " + index);
                }

                // update selection and preview when clicked
                itemCell.setOnMouseClicked(e -> {
                    if (isHeadTabActive) {
                        selectedHeadIndex = index;
                    } else {
                        selectedHatIndex = index;
                    }
                    updateTabs();
                    updatePreview();
                });

                // scale when hovered
                itemCell.setOnMouseEntered(e -> {
                    ScaleTransition st = new ScaleTransition(Duration.millis(120), itemCell);
                    st.setToX(1.05);
                    st.setToY(1.05);
                    st.play();
                });
                itemCell.setOnMouseExited(e -> {
                    ScaleTransition st = new ScaleTransition(Duration.millis(120), itemCell);
                    st.setToX(1.0);
                    st.setToY(1.0);
                    st.play();
                });

                optionsGrid.add(itemCell, c, r);
            }
        }
    }

    // updates the preview avatar based on selections in the customization grid
    private void updatePreview() {
        try {
            String headPath = "/com/eggame/customization/head_" + selectedHeadIndex + ".png";
            String hatPath = "/com/eggame/customization/hat_" + selectedHatIndex + ".png";

            headView.setImage(new Image(getClass().getResourceAsStream(headPath)));
            hatView.setImage(new Image(getClass().getResourceAsStream(hatPath)));
        } catch (Exception e) {
            System.err.println("Failed to update preview avatars: " + e.getMessage());
        }
    }

    // button hover effects
    private void applyHoverEffects(Button btn, double scaleVal) {
        btn.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), btn);
            st.setToX(scaleVal);
            st.setToY(scaleVal);
            st.play();
        });

        btn.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), btn);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }

    public Scene getScene() {
        return this.scene;
    }
}
