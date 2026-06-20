package com.example.java_skat.network;

import java.util.List;

public record GameStateMessage(
        List<CardDto> playerHand,
        List<CardDto> currentTrick,
        List<CardDto> skat,
        boolean skatVisible,
        int topOpponentCardCount,
        int leftOpponentCardCount,
        int collectedCardCount,
        String status,
        boolean finished
) implements SkatMessage {
}
