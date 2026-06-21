package com.example.java_skat.network;

import java.util.List;

public record WaitingRoomMessage(
        List<String> playerNames,
        int connectedPlayers,
        int requiredPlayers,
        boolean gameReady,
        String status
) implements SkatMessage {
}
