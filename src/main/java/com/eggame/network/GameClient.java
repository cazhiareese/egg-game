package com.eggame.network;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

public class GameClient implements Runnable {
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;
    private int playerId = -1;
    private volatile String latestGameState = null;
    /** Incoming chat messages waiting to be displayed — thread-safe deque. */
    private final Deque<String> pendingChats = new ArrayDeque<>();

    public GameClient(String serverIP, int port) throws Exception {
        this.socket = new DatagramSocket();
        this.serverAddress = InetAddress.getByName(serverIP);
        this.serverPort = port;

    }

    public void sendMessage(String msg) throws Exception {
        byte[] data = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(
                data, data.length, serverAddress, serverPort);
        socket.send(packet);
    }

    public String receiveMessage() throws Exception {
        byte[] buffer = new byte[2048];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return new String(packet.getData(), 0, packet.getLength());
    }

    public int join(String playerName) throws Exception {
        sendMessage(PacketType.JOIN + "|" + playerName);
        String reply = receiveMessage();
        String[] parts = reply.split("\\|");
        this.playerId = Integer.parseInt(parts[1]);
        System.out.println("Joined as Player " + playerId);
        return playerId;
    }

    public void sendChat(String text) {
        try {
            // Sanitize: strip any pipe characters the user might type
            String safe = text.replace("|", "");
            sendMessage(PacketType.CHAT + "|" + playerId + "|" + safe);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized ArrayList<String> getLatestChatMessages() {
        ArrayList<String> result = new ArrayList<>(pendingChats);
        pendingChats.clear();
        return result;
    }

    public void sendPlayerState(double posX, double posY, double velX, double velY, int headIndex, int hatIndex) {
        try {
            String msg = PacketType.INPUT + "|" + playerId + "|" + posX + "|" + posY + "|" + velX + "|" + velY + "|"
                    + headIndex + "|" + hatIndex;
            sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // send reset request to server
    public void sendReset() {
        try {
            sendMessage(PacketType.RESET);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getLatestGameState() {
        String state = latestGameState;
        latestGameState = null; // consume it
        return state;
    }

    public int getPlayerId() {
        return playerId;
    }

    @Override
    public void run() {
        while (true) {
            try {
                String message = receiveMessage();
                if (message.startsWith(PacketType.GAME_STATE)) {
                    latestGameState = message;
                } else if (message.startsWith(PacketType.CHAT)) {
                    // Format: CHAT|playerName|text
                    String[] parts = message.split("\\|", 3);
                    if (parts.length == 3) {
                        synchronized (this) {
                            pendingChats.addLast(parts[1] + ": " + parts[2]);
                            // Keep at most 20 messages in the buffer
                            while (pendingChats.size() > 20) {
                                pendingChats.removeFirst();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}