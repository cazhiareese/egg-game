package com.eggame.network;

import java.net.InetAddress;

public class Hashing {

    // base 36 para alphanumeric
    public static String ipToCode(String ip) {
        String[] parts = ip.split("\\.");
        // last parts nung mismong ip
        int x = Integer.parseInt(parts[2]);
        int y = Integer.parseInt(parts[3]);
        // may simple formula lang
        int combined = (x * 256) + y;
        
        String code = Integer.toString(combined, 36).toUpperCase();
        // Pad with zeros to ensure it is always 4 chars
        return String.format("%4s", code).replace(' ', '0');
    }

    // Converts a 4-char code back to a full local IP
    public static String codeToIp(String code) {
        try {
            int combined = Integer.parseInt(code.trim().toLowerCase(), 36);
            int x = combined / 256;
            int y = combined % 256;
            
            // since same lang naman ung prefix nila kukunin ung first two ung nagiiba lang ung last
            String localIp = InetAddress.getLocalHost().getHostAddress();
            String[] parts = localIp.split("\\.");
            
            return parts[0] + "." + parts[1] + "." + x + "." + y;
        } catch (Exception e) {
            return null;
        }
    }
}