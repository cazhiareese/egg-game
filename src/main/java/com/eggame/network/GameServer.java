package com.eggame.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.eggame.entities.Egg;
import com.eggame.entities.Nest;
import com.eggame.entities.Villager;
import com.eggame.map.Farm;
import com.eggame.rules.Logic;
import com.eggame.scene.Game;

public class GameServer {
    private static final int PORT = 9876;
    private static final int WORLD_WIDTH = Game.WINDOW_WIDTH * 2;
    private static final int WORLD_HEIGHT = Game.WINDOW_HEIGHT * 2;
    private static final double TICK_RATE = 50.0; // ms per tick
    private static final double GAME_DURATION = 121.0;
    private volatile boolean lobbyActive = true;
    private volatile boolean gameStarted = false;

    private DatagramSocket socket;
    private Map<Integer, InetSocketAddress> clients = new HashMap<>();
    private int nextPlayerId = 0;

    // Game state (shared between threads — be careful!)
    private ArrayList<Villager> villagers = new ArrayList<>();
    private ArrayList<Egg> eggs = new ArrayList<>();
    private ArrayList<Nest> nests = new ArrayList<>();
    private Farm farm;
    private double timeRemaining = GAME_DURATION;
    private volatile boolean running = true;

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
        while (running) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            String message = new String(packet.getData(), 0, packet.getLength());
            String[] parts = message.split("\\|");
            String type = parts[0];
            if (type.equals(PacketType.JOIN)) {
                handleJoin(parts, packet);
            } else if (type.equals(PacketType.INPUT)) {
                int playerId = Integer.parseInt(parts[1]);
                clients.put(playerId, new java.net.InetSocketAddress(packet.getAddress(), packet.getPort()));
                handleInput(parts, packet);
            } else if (type.equals(PacketType.START_GAME)) {
                handleStartGame(parts);
            } else if (type.equals(PacketType.CLIENT_LEFT)) {
                handleClientLeft(parts);
            }
        }
    }

    private void handleStartGame(String[] parts)throws Exception {
        int requesterId = Integer.parseInt(parts[1]);
        if (requesterId != 0) return; // only host (player 0) can start

        lobbyActive = false;
        gameStarted = true;

        // Notify all clients
        String msg = PacketType.START_GAME + "|go";
        byte[] data = msg.getBytes();
        for (InetSocketAddress client : clients.values()) {
            socket.send(new DatagramPacket(data, data.length, client));
        }
    }

    private void handleJoin(String[] parts, DatagramPacket packet) throws Exception {

        for (Map.Entry<Integer, InetSocketAddress> entry : clients.entrySet()) {
            InetSocketAddress address = new InetSocketAddress(packet.getAddress(), packet.getPort());
            if (entry.getValue().equals(address)) {
                // Already registered — just resend ACK
                String ack = PacketType.JOIN_ACK + "|" + entry.getKey() + "|" + clients.size();
                byte[] ackData = ack.getBytes();
                socket.send(new DatagramPacket(ackData, ackData.length,
                        packet.getAddress(), packet.getPort()));
                return;
            }
        }

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

    private void handleInput(String[] parts, DatagramPacket packet) {
        int playerId = Integer.parseInt(parts[1]);
        if (playerId < 0 || playerId >= villagers.size())
            return;

        clients.put(playerId, new InetSocketAddress(packet.getAddress(), packet.getPort()));

        double posX = Double.parseDouble(parts[2]);
        double posY = Double.parseDouble(parts[3]);
        double velX = Double.parseDouble(parts[4]);
        double velY = Double.parseDouble(parts[5]);

        Villager v = villagers.get(playerId);
        v.setPosition(posX, posY);
        v.setVelocity(velX, velY);
    }

    private void gameLoop() {

        while (lobbyActive) {
            try {
                broadcastLobbyState();
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        while (running && timeRemaining > 0) {
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

    private void handleClientLeft(String[] parts) {
        int playerId = Integer.parseInt(parts[1]);
        
        // Remove from the network clients map
        clients.remove(playerId);
        
        // Remove from the game state list
        // (Using removeIf is the safest way to remove an object by a specific property)
        villagers.removeIf(v -> v.getPlayerId() == playerId);
        
        System.out.println("Player " + playerId + " has left the lobby.");
    }

    private void broadcastLobbyState() throws Exception {
        StringBuilder sb = new StringBuilder();

        sb.append(PacketType.LOBBY_STATE).append("|").append(villagers.size());
            for (Villager v : villagers) {
                sb.append("|").append(v.getName()); // make sure getName() exists
            }
        String msg = sb.toString();
        byte[] data = msg.getBytes();
        for (InetSocketAddress client : clients.values()) {
            socket.send(new DatagramPacket(data, data.length, client));
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
                    .append("|").append(v.getEggsReturned());
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

    public void shutdown() {
        running = false;
        lobbyActive = false; // Add this line to break out of the lobby loop

        try {
            // Notify all clients
            String msg = PacketType.HOST_LEFT + "|Server closed by host";
            byte[] data = msg.getBytes();
            for (InetSocketAddress client : new ArrayList<>(clients.values())) {
                socket.send(new DatagramPacket(data, data.length, client));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Clear all state
            clients.clear();
            villagers.clear();
            eggs.clear();
            nests.clear();
            if (socket != null && !socket.isClosed()) socket.close();
        }
    }
}
