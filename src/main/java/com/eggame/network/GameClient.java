package com.eggame.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class GameClient implements Runnable {
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;
    private int playerId = -1;
    private volatile String latestGameState = null;
    private volatile boolean running = true;


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

    public void sendPlayerState(double posX, double posY, double velX, double velY) {
        try {
            String msg = PacketType.INPUT + "|" + playerId + "|" + posX + "|" + posY + "|" + velX + "|" + velY;
            sendMessage(msg);
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

    public void setPlayerId(int id) {
        this.playerId = id;
    }

    @Override
    public void run() {
        while (true) {
            try {
                String message = receiveMessage();
                if (message.startsWith(PacketType.GAME_STATE)) {
                    latestGameState = message;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}