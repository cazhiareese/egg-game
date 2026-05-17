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
        byte[] buffer = new byte[1024];
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
            }
        }
    }

    private void handleJoin(String[] parts, DatagramPacket packet) throws Exception {
        String playerName = parts[1];
        int id = nextPlayerId++;
        clients.put(id, new InetSocketAddress(packet.getAddress(), packet.getPort()));
        Villager v = new Villager(playerName);
        v.setPlayerId(id);
        v.setPosition(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0); // spawn at center
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

    private void gameLoop() {

        while (timeRemaining > 0) {
            double deltaTime = 0.05;

            Logic.serverUpdate(deltaTime, villagers, eggs, nests, farm);

            timeRemaining -= deltaTime;

            try {
                broadcastGameState();
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
