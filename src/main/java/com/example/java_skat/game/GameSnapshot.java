package com.example.java_skat.game;

import pl.skat.core.Karta;

import java.util.List;

public record GameSnapshot(
        List<Karta> playerHand,
        List<Karta> currentTrick,
        List<Karta> skat,
        boolean skatVisible,
        int topOpponentCardCount,
        int leftOpponentCardCount,
        int collectedCardCount,
        GamePhase phase,
        int currentBid,
        int nextBid,
        String highestBidderName,
        String contractName,
        int cardsToDiscard,
        String status,
        boolean finished
) {
}
