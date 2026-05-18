package com.eggame.network;

public class PacketType {
    public static final String JOIN = "JOIN";
    public static final String JOIN_ACK = "JOIN_ACK";
    public static final String INPUT = "INPUT";
    public static final String GAME_STATE = "GAME_STATE";
    public static final String EGG_PICKUP = "EGG_PICKUP";
    public static final String EGG_DELIVER = "EGG_DELIVER";
    public static final String ROUND_OVER = "ROUND_OVER";
    public static final String CHAT = "CHAT";
    public static final String RESET = "RESET";

    public static final String DELIMITER = "|";
}
