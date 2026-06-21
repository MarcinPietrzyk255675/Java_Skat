package com.example.java_skat.network;

public record ConfirmContractMessage(GameContractDto contract) implements SkatMessage {
}
