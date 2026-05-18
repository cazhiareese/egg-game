package com.eggame.network;

import java.net.*;
import java.util.*;
import com.eggame.entities.*;
import com.eggame.map.Farm;
import com.eggame.rules.Logic;
import com.eggame.scene.Game;

public class GameServer {
    private static final int PORT = 9876;
    private static final int WORLD_WIDTH = Game.WINDOW_WIDTH * 2;
    private static final int WORLD_HEIGHT = Game.WINDOW_HEIGHT * 2;
    private static final double TICK_RATE = 50.0; // ms per tick
    private static final double GAME_DURATION = 121.0;

    private DatagramSocket socket;
    private Map<Integer, InetSocketAddress> clients = new HashMap<>();
    /** Epoch millis of the last packet received from each client. */
    private Map<Integer, Long> lastHeard = new HashMap<>();
    private static final long CLIENT_TIMEOUT_MS = 5000; // 5 seconds
    private int nextPlayerId = 0;

    // Game state (shared between threads — be careful!)
    private ArrayList<Villager> villagers = new ArrayList<>();
    private ArrayList<Egg> eggs = new ArrayList<>();
    private ArrayList<Nest> nests = new ArrayList<>();
    private Farm farm;
    private double timeRemaining = GAME_DURATION;

    public static void main(String[] args) throws Exception {
        javafx.application.Platform.startup(() -> {
        }); // init JavaFX for image loading
        new GameServer().run();
    }

    public void run() throws Exception {
        socket = new DatagramSocket(PORT);
        System.out.println("Server listening on port " + PORT);

        // Initialize the farm and spawn eggs/nests
        farm = new Farm(WORLD_WIDTH, WORLD_HEIGHT);
        Logic.initRound(nests, eggs, farm, WORLD_WIDTH, WORLD_HEIGHT);
        System.out.println("World initialized: " + eggs.size() + " eggs, " + nests.size() + " nests");

        // Start game loop on a separate thread
        Thread gameLoopThread = new Thread(this::gameLoop);
        gameLoopThread.setDaemon(true);
        gameLoopThread.start();

        // Receive loop (runs on main thread)
        receiveLoop();
    }

    private void receiveLoop() throws Exception {
        byte[] buffer = new byte[4096];
        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            String message = new String(packet.getData(), 0, packet.getLength());
            String[] parts = message.split("\\|");
            String type = parts[0];
            if (type.equals(PacketType.JOIN)) {
                handleJoin(parts, packet);
            } else if (type.equals(PacketType.INPUT)) {
                handleInput(parts);
            } else if (type.equals(PacketType.CHAT)) {
                handleChat(parts);
            } else if (type.equals(PacketType.RESET)) {
                handleReset();
            }
        }
    }

    // handle rest request from client - clears game and reinitializes eggs/nests
    private void handleReset() {
        timeRemaining = GAME_DURATION;
        eggs.clear();
        nests.clear();
        for (Villager v : villagers) {
            v.getEggTray().getEggs().clear();
            v.setEggsReturned(0);
        }
        Logic.initRound(nests, eggs, farm, WORLD_WIDTH, WORLD_HEIGHT);
        System.out.println("Game was reset by a client.");
    }

    private void handleJoin(String[] parts, DatagramPacket packet) throws Exception {
        String playerName = parts[1];
        int id = nextPlayerId++;
        clients.put(id, new InetSocketAddress(packet.getAddress(), packet.getPort()));
        lastHeard.put(id, System.currentTimeMillis());
        Villager v = new Villager(playerName);
        v.setPlayerId(id);
        double preferredX = WORLD_WIDTH / 2.0 + id * 150;
        double preferredY = WORLD_HEIGHT / 2.0;
        double[] safe = Logic.findSafeSpawn(preferredX, preferredY, nests, farm);
        v.setPosition(safe[0], safe[1]);
        villagers.add(v);
        // Send back: JOIN_ACK|playerId|totalPlayers
        String ack = PacketType.JOIN_ACK + "|" + id + "|" + clients.size();
        byte[] ackData = ack.getBytes();
        socket.send(new DatagramPacket(ackData, ackData.length,
                packet.getAddress(), packet.getPort()));

        System.out.println(playerName + " joined as Player " + id);

    }

    private void handleInput(String[] parts) {
        int playerId = Integer.parseInt(parts[1]);
        if (playerId < 0 || playerId >= villagers.size())
            return;
        lastHeard.put(playerId, System.currentTimeMillis());
        double posX = Double.parseDouble(parts[2]);
        double posY = Double.parseDouble(parts[3]);
        double velX = Double.parseDouble(parts[4]);
        double velY = Double.parseDouble(parts[5]);

        Villager v = villagers.get(playerId);
        v.setPosition(posX, posY);
        v.setVelocity(velX, velY);

        // update avatar based on customized avatar index (head and hat)
        if (parts.length > 7) {
            int headIdx = Integer.parseInt(parts[6]);
            int hatIdx = Integer.parseInt(parts[7]);
            if (v.getHeadIndex() != headIdx || v.getHatIndex() != hatIdx) {
                v.setAvatar(headIdx, hatIdx);
            }
        }
    }

    private void handleChat(String[] parts) throws Exception {
        // Format: CHAT|playerId|text
        if (parts.length < 3)
            return;
        int senderId;
        try {
            senderId = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return;
        }
        String text = parts[2];

        // Resolve a display name for the sender
        String senderName;
        if (senderId >= 0 && senderId < villagers.size()) {
            senderName = villagers.get(senderId).getName();
        } else {
            senderName = "Player " + senderId;
        }

        // Broadcast to every client: CHAT|senderName|text
        broadcastChat(senderName, text);
        System.out.println("[CHAT] " + senderName + ": " + text);
    }

    private void broadcastChat(String senderName, String text) throws Exception {
        // Sanitize to keep the pipe-delimited protocol intact
        String safeName = senderName.replace("|", "");
        String safeText = text.replace("|", "");
        String message = PacketType.CHAT + "|" + safeName + "|" + safeText;
        byte[] data = message.getBytes();
        for (InetSocketAddress client : clients.values()) {
            socket.send(new DatagramPacket(data, data.length, client));
        }
    }

    private void gameLoop() {

        while (true) {

            if (timeRemaining > 0) {
                double deltaTime = 0.05;
                Logic.serverUpdate(deltaTime, villagers, eggs, nests, farm);
                timeRemaining -= deltaTime;
            }

            try {
                if (!clients.isEmpty()) {
                    broadcastGameState();
                }
                Thread.sleep(50);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void broadcastGameState() throws Exception {
        StringBuilder sb = new StringBuilder();

        sb.append(PacketType.GAME_STATE).append("|");
        sb.append(villagers.size()).append("|");
        sb.append(timeRemaining);

        for (Villager v : villagers) {
            sb.append("|").append(v.getPositionX())
                    .append("|").append(v.getPositionY())
                    .append("|").append(v.getVelocityX())
                    .append("|").append(v.getVelocityY())
                    .append("|").append(v.getEggsReturned())
                    .append("|").append(v.getHeadIndex())
                    .append("|").append(v.getHatIndex());
        }

        for (Egg egg : eggs) {
            sb.append("|").append(egg.isCollected() ? 1 : 0)
                    .append("|").append(egg.isReturnedToNest() ? 1 : 0);
        }

        String message = sb.toString();
        byte[] data = message.getBytes();

        for (InetSocketAddress client : clients.values()) {
            DatagramPacket packet = new DatagramPacket(data, data.length, client);
            socket.send(packet);
        }
    }
}
