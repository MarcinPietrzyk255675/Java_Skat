package com.example.java_skat.network;

public record PlayCardMessage(CardDto card) implements SkatMessage {
}
