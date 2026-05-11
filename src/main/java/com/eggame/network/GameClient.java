package com.eggame.network;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class GameClient {
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;
    private int playerId = -1;

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
        byte[] buffer = new byte[1024];
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

    // Temporary test main
    public static void main(String[] args) throws Exception {
        String name = args.length > 0 ? args[0] : "Player";
        GameClient client = new GameClient("127.0.0.1", 9876);
        client.join(name);
    }
}