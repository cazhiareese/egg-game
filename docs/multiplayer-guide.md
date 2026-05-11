# UDP Multiplayer Implementation Guide

A step-by-step guide for adding networked multiplayer to EGGciting Hunt using Java UDP sockets. Work through each step in order — each one builds on the previous.

---

## Step 1: Create the Network Package (Scaffolding)

**Goal**: Get the folder structure and empty classes in place.

Create the directory `src/main/java/com/eggame/network/` and add these files:

### 1a. `PacketType.java` — Define your protocol constants

```java
package com.eggame.network;

public class PacketType {
    public static final String JOIN        = "JOIN";
    public static final String JOIN_ACK    = "JOIN_ACK";
    public static final String INPUT       = "INPUT";
    public static final String GAME_STATE  = "GAME_STATE";
    public static final String EGG_PICKUP  = "EGG_PICKUP";
    public static final String EGG_DELIVER = "EGG_DELIVER";
    public static final String ROUND_OVER  = "ROUND_OVER";
    
    public static final String DELIMITER = "|";
}
```

### 1b. `GameServer.java` — Empty skeleton

```java
package com.eggame.network;

import java.net.DatagramSocket;
import java.net.DatagramPacket;

public class GameServer {
    private DatagramSocket socket;
    private static final int PORT = 9876;
    
    public static void main(String[] args) {
        System.out.println("Server starting on port " + PORT);
        // TODO: implement
    }
}
```

### 1c. `GameClient.java` — Empty skeleton

```java
package com.eggame.network;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class GameClient {
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;
    
    public GameClient(String serverIP, int port) throws Exception {
        this.socket = new DatagramSocket();
        this.serverAddress = InetAddress.getByName(serverIP);
        this.serverPort = port;
    }
}
```

### 1d. Update `module-info.java`

Add these two lines:
```java
opens com.eggame.network to javafx.fxml;
exports com.eggame.network;
```

**✅ Checkpoint**: Run `mvn clean compile` — it should compile with no errors.

---

## Step 2: Learn UDP Basics (Sending & Receiving)

**Goal**: Get two programs talking over UDP before touching game logic.

Before wiring anything into the game, write a simple test. Add temporary `main()` methods:

### 2a. In `GameServer.java`, add a receive loop:

```java
public static void main(String[] args) throws Exception {
    DatagramSocket socket = new DatagramSocket(9876);
    byte[] buffer = new byte[1024];
    
    System.out.println("Server listening on port 9876...");
    
    while (true) {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet); // blocks until a packet arrives
        
        String message = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Received: " + message 
            + " from " + packet.getAddress() + ":" + packet.getPort());
        
        // Echo back
        String reply = "ACK|" + message;
        byte[] replyData = reply.getBytes();
        DatagramPacket replyPacket = new DatagramPacket(
            replyData, replyData.length, 
            packet.getAddress(), packet.getPort()
        );
        socket.send(replyPacket);
    }
}
```

### 2b. In `GameClient.java`, send a test message:

```java
public void sendMessage(String msg) throws Exception {
    byte[] data = msg.getBytes();
    DatagramPacket packet = new DatagramPacket(
        data, data.length, serverAddress, serverPort
    );
    socket.send(packet);
}

public String receiveMessage() throws Exception {
    byte[] buffer = new byte[1024];
    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
    socket.receive(packet);
    return new String(packet.getData(), 0, packet.getLength());
}

// Temporary test main
public static void main(String[] args) throws Exception {
    GameClient client = new GameClient("127.0.0.1", 9876);
    client.sendMessage("Hello from client!");
    String reply = client.receiveMessage();
    System.out.println("Server replied: " + reply);
}
```

### 2c. Test it:

```bash
# Terminal 1 — start server
mvn exec:java -Dexec.mainClass="com.eggame.network.GameServer"

# Terminal 2 — run client
mvn exec:java -Dexec.mainClass="com.eggame.network.GameClient"
```

**✅ Checkpoint**: You should see "Received: Hello from client!" on the server and "Server replied: ACK|Hello from client!" on the client.

---

## Step 3: Implement the JOIN Handshake

**Goal**: Client sends JOIN, server assigns a player ID and replies.

### 3a. Server side — track connected players

```java
// In GameServer, add fields:
private Map<Integer, InetSocketAddress> clients = new HashMap<>();
private int nextPlayerId = 0;
```

In your receive loop, parse the packet type:

```java
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
```

### 3b. Client side — join and receive ID

```java
public int join(String playerName) throws Exception {
    sendMessage(PacketType.JOIN + "|" + playerName);
    String reply = receiveMessage();
    String[] parts = reply.split("\\|");
    // parts[0] = "JOIN_ACK", parts[1] = playerId, parts[2] = totalPlayers
    this.playerId = Integer.parseInt(parts[1]);
    System.out.println("Joined as Player " + playerId);
    return playerId;
}
```

**✅ Checkpoint**: Start server, connect 2 clients. Server prints "Player 0" and "Player 1".

---

## Step 4: Refactor Logic.java for Per-Player Methods

**Goal**: Make `Logic` methods work with any specific `Villager`, not just `villagers.get(0)`.

This is crucial — the server needs to run game logic for each connected player.

### 4a. Change method signatures:

```java
// BEFORE:
private static void checkEggPickup(ArrayList<Villager> villagers, ArrayList<Egg> eggs) {
    Villager currentPlayer = villagers.get(0);
    ...
}

// AFTER:
public static void checkEggPickup(Villager player, ArrayList<Egg> eggs) {
    // same logic, but use 'player' instead of 'villagers.get(0)'
    ...
}
```

Do this for:
- `checkEggPickup(Villager player, ArrayList<Egg> eggs)`
- `checkNestDelivery(Villager player, ArrayList<Nest> nests)`
- `checkCollisions(double deltaTime, Villager player, Farm farm, ArrayList<Nest> nests)`
- `handleInput(double deltaTime, Villager player, ArrayList<String> input)`

### 4b. Update the `update()` caller to loop through all villagers:

```java
public static void update(double deltaTime, ArrayList<Villager> villagers, 
        ArrayList<Egg> eggs, ArrayList<Nest> nests, Farm farm, ArrayList<String> input) {
    
    // For single player, still works — loops once over villagers.get(0)
    for (Villager player : villagers) {
        handleInput(deltaTime, player, input);  // only relevant for local player
        checkEggPickup(player, eggs);
        checkNestDelivery(player, nests);
        checkCollisions(deltaTime, player, farm, nests);
    }
}
```

> **Note**: `handleInput` should only be called for the local player. On the server, you'll set positions directly from packets instead of calling `handleInput`.

**✅ Checkpoint**: Game still works exactly the same in single-player after refactoring. Run and test.

---

## Step 5: Add Network Identity to Entities

**Goal**: Each Villager and Egg gets an ID so they can be referenced in packets.

### 5a. `Villager.java` — add playerId

```java
private int playerId = -1;

public int getPlayerId() { return playerId; }
public void setPlayerId(int id) { this.playerId = id; }
```

### 5b. `Egg.java` — add eggIndex

```java
private int eggIndex = -1;

public int getEggIndex() { return eggIndex; }
public void setEggIndex(int idx) { this.eggIndex = idx; }
```

In `Logic.initRound`, set the index when creating eggs:

```java
egg.setEggIndex(eggs.size());  // before eggs.add(egg)
eggs.add(egg);
```

**✅ Checkpoint**: Compile, run, same behavior.

---

## Step 6: Build the Server Game Loop

**Goal**: The server runs the game and broadcasts state.

### 6a. Server architecture — two threads:

```
Thread 1 (Receive):  Listens for JOIN and INPUT packets
Thread 2 (Game Loop): Runs at ~20 ticks/sec, broadcasts GAME_STATE
```

### 6b. Pseudocode for the server game loop:

```java
// Server tick (runs every 50ms)
while (gameRunning) {
    double deltaTime = 0.05; // 50ms tick
    
    // For each villager: apply received position, run logic
    for (Villager v : villagers) {
        Logic.checkEggPickup(v, eggs);
        Logic.checkNestDelivery(v, nests);
        Logic.checkCollisions(deltaTime, v, farm, nests);
    }
    
    timeRemaining -= deltaTime;
    
    // Build state string
    StringBuilder sb = new StringBuilder(PacketType.GAME_STATE);
    sb.append("|").append(timeRemaining);
    
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
    
    // Broadcast to all clients
    byte[] data = sb.toString().getBytes();
    for (InetSocketAddress addr : clients.values()) {
        socket.send(new DatagramPacket(data, data.length, addr));
    }
    
    Thread.sleep(50); // ~20 ticks/sec
}
```

### 6c. Handle INPUT packets (on receive thread):

When you receive `INPUT|playerId|posX|posY|velX|velY`:

```java
if (type.equals(PacketType.INPUT)) {
    int id = Integer.parseInt(parts[1]);
    double px = Double.parseDouble(parts[2]);
    double py = Double.parseDouble(parts[3]);
    double vx = Double.parseDouble(parts[4]);
    double vy = Double.parseDouble(parts[5]);
    
    Villager v = villagers.get(id);
    v.setPosition(px, py);
    v.setVelocity(vx, vy);
}
```

**✅ Checkpoint**: Server compiles. Start it, verify it prints state to console.

---

## Step 7: Integrate GameClient into Game.java

**Goal**: The game sends input and renders based on server state.

### 7a. In `Game.java` constructor, accept connection info:

```java
private GameClient client;
private int localPlayerId;
```

### 7b. In `start()`, connect to server:

```java
client = new GameClient("127.0.0.1", 9876);
localPlayerId = client.join("Player");

// Start receive thread
Thread receiveThread = new Thread(client);
receiveThread.setDaemon(true);
receiveThread.start();
```

### 7c. In `update()`, send your position:

```java
Villager localPlayer = villagers.get(localPlayerId);
Logic.handleInput(deltaTime, localPlayer, input);
client.sendPlayerState(
    localPlayer.getPositionX(), localPlayer.getPositionY(),
    localPlayer.getVelocityX(), localPlayer.getVelocityY()
);
```

### 7d. Client receive thread updates other villagers:

When you receive `GAME_STATE`, parse it and update all villager positions + egg states. Use `Platform.runLater()` since you're modifying JavaFX state from a non-FX thread:

```java
Platform.runLater(() -> {
    // update positions of all villagers except local player
    // update egg collected/returned states
    // update timer
});
```

**✅ Checkpoint**: Two game windows on localhost, both players visible and moving.

---

## Step 8: Wire Up App.java Launch Modes

**Goal**: Choose between server and client mode at startup.

Simple approach — check command-line args:

```java
public static void main(String[] args) {
    if (args.length > 0 && args[0].equals("--server")) {
        // Start server only (no GUI)
        GameServer.main(args);
    } else {
        // Start game client (with GUI)
        launch(args);
    }
}
```

Or add a JavaFX dialog in `start()` asking "Host" or "Join" with an IP text field.

---

## Suggested Order of Work

| # | Task | Estimated Time |
|---|---|---|
| 1 | Create `network/` package + empty classes | 15 min |
| 2 | UDP echo test (send/receive) | 30 min |
| 3 | JOIN handshake | 30 min |
| 4 | Refactor `Logic.java` per-player methods | 45 min |
| 5 | Add `playerId` / `eggIndex` fields | 15 min |
| 6 | Server game loop + broadcast | 1–2 hours |
| 7 | Client integration in `Game.java` | 1–2 hours |
| 8 | Launch modes in `App.java` | 30 min |

**Total: ~5–7 hours of focused work**

---

## Common Pitfalls to Watch Out For

1. **Thread safety**: JavaFX UI can only be updated from the FX Application Thread. Wrap any state updates from the network thread in `Platform.runLater()`.

2. **Packet size**: Keep packets under 1400 bytes to avoid fragmentation. With 20 eggs and 4 players, your GAME_STATE packet will be ~400 bytes — you're fine.

3. **Stale packets**: UDP packets can arrive out of order. Add a sequence number to GAME_STATE and discard older ones.

4. **Blocking `socket.receive()`**: This blocks the thread. Make sure your receive loop runs on a **separate daemon thread**, never on the JavaFX thread.

5. **Don't run game logic on both sides**: The server is the authority. Clients only handle local input + rendering. If you run `checkEggPickup` on both client and server, you'll get desyncs.

6. **`split("\\|")` gotcha**: The pipe `|` is a regex special character. Always escape it as `\\|`.
