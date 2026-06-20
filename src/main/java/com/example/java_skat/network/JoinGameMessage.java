package com.example.java_skat.network;

public record JoinGameMessage(String playerName) implements SkatMessage {
}
