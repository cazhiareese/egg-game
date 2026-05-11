package com.eggame.network;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.net.DatagramPacket;

public class GameServer {
    private static final int PORT = 9876;
    private static Map<Integer, InetSocketAddress> clients = new HashMap<>();
    private static int nextPlayerId = 0;

    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(9876);
        byte[] buffer = new byte[1024];

        System.out.println("Server listening on port 9876...");

        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet); // blocks until a packet arrives

            String message = new String(packet.getData(), 0, packet.getLength());
            String[] parts = message.split("\\|");
            String type = parts[0];

            if (type.equals(PacketType.JOIN)) {
                String playerName = parts[1];
                int id = nextPlayerId++;
                clients.put(id, new InetSocketAddress(packet.getAddress(), packet.getPort()));

                // Send back: JOIN_ACK|playerId|totalPlayers
                String ack = PacketType.JOIN_ACK + "|" + id + "|" + clients.size();
                byte[] ackData = ack.getBytes();
                socket.send(new DatagramPacket(ackData, ackData.length,
                        packet.getAddress(), packet.getPort()));

                System.out.println(playerName + " joined as Player " + id);
            }
        }
    }

}
