package com.example.java_skat.network;

public record DiscardCardMessage(CardDto card) implements SkatMessage {
}
