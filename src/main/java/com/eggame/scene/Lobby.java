package com.eggame.scene;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import com.eggame.network.PacketType;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class Lobby {
    private boolean startGameSent = false;

    private Scene scene;
    private SceneManager sceneManager;
    private DatagramSocket socket;
    private VBox playerListBox;
    private int localPlayerId = -1;
    private String code;
    private String ip;

   public Lobby(SceneManager sceneManager, String playerName, boolean isHost, String ip, String code) {
        this.sceneManager = sceneManager;
        this.ip = ip;
        this.code = code;

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #C48C47;"); // Apply background color to the entire screen

        VBox centerBox = new VBox(20);
        centerBox.setAlignment(Pos.CENTER);
         

        Label title = new Label("Waiting for Players to Join...");
        title.setFont(Font.loadFont(getClass().getResourceAsStream("/com/eggame/fonts.ttf"), 64));

        Label roomCode = new Label("Room Code:" + this.code);
        roomCode.setFont(Font.loadFont(getClass().getResourceAsStream("/com/eggame/fonts.ttf"), 32));

        // Player list
        playerListBox = new VBox(8);
        playerListBox.setAlignment(Pos.CENTER);

        Button startBtn = new Button("Start Game");
        startBtn.setVisible(isHost); // You can remove this if you want anyone to start!
        startBtn.setPrefWidth(200);
        startBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 20;");
        startBtn.setOnAction(e -> {
            if (!startGameSent) {
                startGameSent = true;
                startBtn.setDisable(true);
                sendStartGame();
            }
        });

        centerBox.getChildren().addAll(title, roomCode, playerListBox, startBtn);

        Button backButton = createBackButton("<");
        backButton.setOnAction(e -> {
            if (!isHost) {
                sendLeavePacket(); // Tell server we are out
            }
            cleanup();
            if (isHost) sceneManager.shutdownServer();
            sceneManager.switchToMainMenu();
        });

        StackPane.setAlignment(backButton, Pos.TOP_LEFT);
        StackPane.setMargin(backButton, new Insets(20, 0, 0, 20)); 

        root.getChildren().addAll(centerBox, backButton);
        
        this.scene = new Scene(root, Game.WINDOW_WIDTH, Game.WINDOW_HEIGHT);

        connectToServer(playerName);
    }

    private void connectToServer(String playerName) {
        try {
            socket = new DatagramSocket();
            InetAddress serverAddr = InetAddress.getByName(this.ip);
            socket.setSoTimeout(10000);
            // Send JOIN packet
            String joinMsg = PacketType.JOIN + "|" + playerName;
            byte[] data = joinMsg.getBytes();
            socket.send(new DatagramPacket(data, data.length, serverAddr, 9876));

            // Start listener thread
            Thread listener = new Thread(() -> {
                byte[] buf = new byte[1024];
                while (!socket.isClosed()) {
                    try {
                        DatagramPacket pkt = new DatagramPacket(buf, buf.length);
                        socket.receive(pkt);
                        String msg = new String(pkt.getData(), 0, pkt.getLength());
                        handlePacket(msg);
                    } catch (SocketTimeoutException e) {
                        // No packet received in 5 seconds — host likely gone
                        Platform.runLater(() -> {
                            cleanup();
                            showHostLeftDialog();
                        });
                        break;
                    } catch (Exception e) {
                        if (!socket.isClosed()) e.printStackTrace();
                    }
                }
            });
            listener.setDaemon(true);
            listener.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handlePacket(String msg) {
        String[] parts = msg.split("\\|");
        switch (parts[0]) {
            case PacketType.JOIN_ACK:
                localPlayerId = Integer.parseInt(parts[1]);
                System.out.println("Joined as player " + localPlayerId);
                break;

            case PacketType.LOBBY_STATE:
                // LOBBY_STATE|count|name1|name2|...
                int count = Integer.parseInt(parts[1]);
                List<String> names = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    names.add(parts[2 + i]);
                }
                Platform.runLater(() -> {
                    playerListBox.getChildren().clear();
                    Label header = new Label("Players in Lobby (" + count + "):");
                    header.setFont(Font.loadFont(getClass().getResourceAsStream("/com/eggame/fonts.ttf"), 14));
                    playerListBox.getChildren().add(header);
                    for (String name : names) {
                        Label lbl = new Label("• " + name);
                        lbl.setFont(Font.loadFont(getClass().getResourceAsStream("/com/eggame/fonts.ttf"), 14));
                        playerListBox.getChildren().add(lbl);
                    }
                });
                break;

            case PacketType.START_GAME:
                Platform.runLater(() -> {
                    // Switch to game first, then cleanup the lobby socket
                    this.sceneManager.switchToGame(this.ip, this.localPlayerId);
                    cleanup();
                });
                break;
            case PacketType.HOST_LEFT:
                Platform.runLater(() -> {
                    cleanup();
                    showHostLeftDialog();
                });
                break;
        }
    }

    private void sendStartGame() {
        if (socket == null || socket.isClosed()) return;
        try {
            String msg = PacketType.START_GAME + "|" + localPlayerId;
            byte[] data = msg.getBytes();
            socket.send(new DatagramPacket(data, data.length,
                    InetAddress.getByName(this.ip), 9876));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cleanup() {
        if (socket != null && !socket.isClosed()) socket.close();
    }

    private void showHostLeftDialog() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION
        );
        alert.setTitle("Disconnected");
        alert.setHeaderText(null);
        alert.setContentText("The host has closed the server.");
        alert.showAndWait().ifPresent(e -> sceneManager.switchToMainMenu());
    }

    private void sendLeavePacket() {
        if (localPlayerId == -1) return; // Haven't fully joined yet
        
        try {
            String msg = PacketType.CLIENT_LEFT + "|" + localPlayerId;
            byte[] data = msg.getBytes();
            // Use this.ip instead of "localhost" to ensure LAN works!
            socket.send(new DatagramPacket(data, data.length, InetAddress.getByName(this.ip), 9876));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Scene getScene() { return scene; }

        //back button function
    private Button createBackButton(String text) {
        Button btn = new Button();
        Text btnText = new Text(text);

        Font customFont =Font.loadFont(getClass().getResourceAsStream("/com/eggame/fonts.ttf"), 36);
        
        btnText.setFont(customFont);
    

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

}

