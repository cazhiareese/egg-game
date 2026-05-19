package com.eggame.scene;

import com.eggame.network.GameServer;
import com.eggame.network.Hashing;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class MainMenu {

    private boolean serverRunning = false;

    private Scene scene;
    private SceneManager sceneManager;

    public MainMenu(SceneManager sceneManager) {
        this.sceneManager = sceneManager;

        // main Container
        StackPane root = new StackPane();

        try {
            String bgUrl = getClass().getResource("/com/eggame/menu_bg.png").toExternalForm();
            root.setStyle("-fx-background-image: url('" + bgUrl + "'); " +
                    "-fx-background-size: cover; " +
                    "-fx-background-position: center center; " +
                    "-fx-background-repeat: no-repeat;");
        } catch (Exception e) {
            // simple error fallback
            System.err.println("Failed to load menu_bg.png");
            root.setStyle("-fx-background-color: #A9D08E;");
        }

        // clouds Layer
        Pane cloudLayer = new Pane();
        try {
            Image cloudImg = new Image(getClass().getResourceAsStream("/com/eggame/cloud.png"));

            // top left
            ImageView cloud1 = new ImageView(cloudImg);
            cloud1.setFitWidth(170);
            cloud1.setPreserveRatio(true);
            cloud1.setLayoutX(-10);
            cloud1.setLayoutY(20);
            animateCloud(cloud1, 3.0);

            // small top left
            ImageView cloud1small = new ImageView(cloudImg);
            cloud1small.setFitWidth(85);
            cloud1small.setPreserveRatio(true);
            cloud1small.setLayoutX(90);
            cloud1small.setLayoutY(90);
            animateCloud(cloud1small, 3.0);

            // top right
            ImageView cloud2 = new ImageView(cloudImg);
            cloud2.setFitWidth(140);
            cloud2.setPreserveRatio(true);
            cloud2.setLayoutX(400);
            cloud2.setLayoutY(80);
            animateCloud(cloud2, 4.0);

            // bottom left
            ImageView cloud3 = new ImageView(cloudImg);
            cloud3.setFitWidth(270);
            cloud3.setPreserveRatio(true);
            cloud3.setLayoutX(-50);
            cloud3.setLayoutY(460);
            animateCloud(cloud3, 3.5);

            // bottom right
            ImageView cloud4 = new ImageView(cloudImg);
            cloud4.setFitWidth(200);
            cloud4.setPreserveRatio(true);
            cloud4.setLayoutX(380);
            cloud4.setLayoutY(560);
            animateCloud(cloud4, 4.5);

            // small bottom right
            ImageView cloud4small = new ImageView(cloudImg);
            cloud4small.setFitWidth(133);
            cloud4small.setPreserveRatio(true);
            cloud4small.setLayoutX(350);
            cloud4small.setLayoutY(625);
            animateCloud(cloud4small, 4.5);

            cloudLayer.getChildren().addAll(cloud1, cloud1small, cloud2, cloud3, cloud4, cloud4small);
        } catch (Exception e) {
            System.err.println("Failed to load cloud.png");
        }

        VBox uiLayer = new VBox(30);
        uiLayer.setAlignment(Pos.CENTER);

        // title - Egg Hunt
        ImageView title = new ImageView();
        try {
            Image titleImage = new Image(getClass().getResourceAsStream("/com/eggame/gametitle.gif"));
            title.setImage(titleImage);
            title.setFitWidth(500);
            title.setPreserveRatio(true);
            title.setTranslateX(-340);
            title.setTranslateY(15);
        } catch (Exception e) {
            System.err.println("Failed to load title.gif");
        }

        // Buttons
        Button createServer = createMenuButton("Create Game"); // PLAY BUTTON
        createServer.setOnAction(e -> {
            // creating server instance
            GameServer server = new GameServer();
            try {
                java.net.DatagramSocket testSocket = new java.net.DatagramSocket(9876);
                testSocket.close();
            } catch (Exception ex) {
                // Port is in use! Show the error safely on the JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    showError("A server is already running on this machine.");
                });

                return;
            }

            Thread serverThread = new Thread(() -> {
                try {
                    server.run();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            serverThread.setDaemon(true);
            serverThread.start();

            sceneManager.setActiveServer(server, serverThread);

            // Small delay so server is ready before client connects
            new Thread(() -> {
                try {
                    Thread.sleep(300);
                    String localIp =  Hashing.getActualLanIP();

                    String roomCode = Hashing.ipToCode(localIp);
                    System.out.println("Room Code: " + roomCode);
                    javafx.application.Platform.runLater(() -> {
                        if (this.sceneManager != null) {
                            System.out.println(localIp);
                            this.sceneManager.switchToLobby("Host", true, localIp, roomCode);
                        }
                    });
                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> {
                        showError("Network Error");
                    });
                }
            }).start();
        });

        Button joinServer = createMenuButton("Join Game");
        joinServer.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog("");
            dialog.setTitle("Join Game");
            dialog.setHeaderText("Enter 4-Character Room Code:");

            dialog.showAndWait().ifPresent(code -> {
                if (code.isBlank() || code.length() != 4) {
                    showError("Codes must be exactly 4 characters.");
                    return;
                }

                String ip = Hashing.codeToIp(code);
                if (ip == null) {
                    showError("Invalid Room Code.");
                    return;
                }
                new Thread(() -> {
                    try (java.net.DatagramSocket testSocket = new java.net.DatagramSocket()) {
                        java.net.InetAddress address = java.net.InetAddress.getByName(ip);
                        
    
                        testSocket.setSoTimeout(5000); 
                        
                        byte[] sendData = "PING".getBytes();
                        java.net.DatagramPacket sendPacket = new java.net.DatagramPacket(
                                sendData, sendData.length, address, 9876);
                        testSocket.send(sendPacket);

                        byte[] recvData = new byte[1024];
                        java.net.DatagramPacket recvPacket = new java.net.DatagramPacket(recvData, recvData.length);
                        
                        // wait for up to 5 seconds before throwing an Exception
                        testSocket.receive(recvPacket);

                        String response = new String(recvPacket.getData(), 0, recvPacket.getLength());
                        if (response.equals("PONG")) {
                            javafx.application.Platform.runLater(() -> {
                                sceneManager.switchToLobby("Player", false, ip, code);
                            });
                        }
                    } catch (java.net.SocketTimeoutException er) {
                        // Specifically catch the timeout to give a better error message
                        javafx.application.Platform.runLater(() -> {
                            showError("Connection timed out. The server is taking too long to respond.");
                        });
                    } catch (Exception ex) {
                        javafx.application.Platform.runLater(() -> {
                            showError("No game found at code: " + code.toUpperCase());
                        });
                    }
                }).start();
            });
        });

        Button instructionsButton = createMenuButton("Instructions"); // INSTRUCTIONS BUTTON
        instructionsButton.setOnAction(e -> {
            if (this.sceneManager != null) {
                this.sceneManager.switchToInstructions();
            }
        });

        Button customizeButton = createMenuButton("Customize"); // CUSTOMIZE BUTTON
        customizeButton.setOnAction(e -> {
            if (this.sceneManager != null) {
                this.sceneManager.switchToCustomize();
            }
        });

        uiLayer.getChildren().addAll(title, createServer, joinServer, instructionsButton, customizeButton);

        // sample sprite layer
        Pane spriteLayer = new Pane();
        try {
            Image spriteImg = new Image(getClass().getResourceAsStream("/com/eggame/girlieprop.gif"));
            ImageView spriteView = new ImageView(spriteImg);
            spriteView.setFitWidth(150);
            spriteView.setPreserveRatio(true);
            spriteView.setLayoutX(835);
            spriteView.setLayoutY(220);
            spriteLayer.getChildren().add(spriteView);
        } catch (Exception e) {
            System.err.println("Failed to load girlieprop.gif");
        }

        root.getChildren().addAll(cloudLayer, spriteLayer, uiLayer);

        this.scene = new Scene(root, Game.WINDOW_WIDTH, Game.WINDOW_HEIGHT);
    }

    // cloud animation
    private void animateCloud(ImageView cloud, double durationSeconds) {
        TranslateTransition tt = new TranslateTransition(Duration.seconds(durationSeconds), cloud);
        tt.setByY(15);
        tt.setCycleCount(TranslateTransition.INDEFINITE);
        tt.setAutoReverse(true);
        tt.play();
    }

    // menu button style
    private Button createMenuButton(String text) {
        Button btn = new Button();
        Text btnText = new Text(text);

        try {
            Font customFont = Font.loadFont(getClass().getResourceAsStream("/com/eggame/fonts.ttf"), 32);
            if (customFont != null) {
                btnText.setFont(customFont);
            } else {
                btnText.setFont(Font.font("Quicksand", 24));
            }
        } catch (Exception e) {
            btnText.setFont(Font.font("Quicksand", 24));
        }

        btnText.setFill(javafx.scene.paint.Color.web("#FFF7D6"));
        btnText.setStroke(javafx.scene.paint.Color.web("#60312B"));
        btnText.setStrokeType(javafx.scene.shape.StrokeType.OUTSIDE);
        btnText.setStrokeWidth(3);

        btn.setGraphic(btnText);
        btn.setPrefWidth(250);
        btn.setPrefHeight(60);
        btn.setTranslateX(-340);
        btn.setTranslateY(-70);

        // button styles
        String defaultStyle = "-fx-background-color: #C48C47; -fx-background-radius: 15; -fx-border-color: #60312B; -fx-border-radius: 15; -fx-border-width: 4;";
        String hoverStyle = "-fx-background-color: #D69D58; -fx-background-radius: 15; -fx-border-color: #60312B; -fx-border-radius: 15; -fx-border-width: 4;";

        btn.setStyle(defaultStyle);
        btn.setOnMouseEntered(e -> {
            btn.setStyle(hoverStyle);
            ScaleTransition st = new ScaleTransition(Duration.millis(100), btn);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(defaultStyle);
            ScaleTransition st = new ScaleTransition(Duration.millis(100), btn);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        return btn;
    }

    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Connection Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Scene getScene() {
        return scene;
    }
}
