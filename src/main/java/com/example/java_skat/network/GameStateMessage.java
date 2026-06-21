package com.example.java_skat.network;

import com.example.java_skat.game.GamePhase;

import java.util.List;

public record GameStateMessage(
        List<CardDto> playerHand,
        List<CardDto> currentTrick,
        List<CardDto> skat,
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
        boolean canTakeSkatBeforeContract,
        boolean canChooseGameWithoutSkat,
        boolean declarationsRestrictedAfterSkat,
        boolean handRequired,
        boolean canBid,
        boolean canConfirmContract,
        boolean canDiscard,
        boolean canPlay,
        boolean canPass,
        boolean canNewDeal,
        String status,
        boolean finished,
        int dealNumber,
        int totalDeals,
        String playerPositionName,
        String positionSummary
) implements SkatMessage {
}
