package com.example.java_skat.network;

import com.example.java_skat.game.GamePhase;

import java.util.List;

public record GameStateMessage(
        List<CardDto> playerHand,
        List<CardDto> currentTrick,
        CardDto ownTrickCard,
        String ownTrickLabel,
        CardDto leftOpponentTrickCard,
        String leftOpponentTrickLabel,
        CardDto rightOpponentTrickCard,
        String rightOpponentTrickLabel,
        List<CardDto> skat,
        boolean skatVisible,
        List<CardDto> topOpponentHand,
        boolean topOpponentCardsVisible,
        int topOpponentCardCount,
        String topOpponentName,
        List<CardDto> leftOpponentHand,
        boolean leftOpponentCardsVisible,
        int leftOpponentCardCount,
        String leftOpponentName,
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
        String bidActionText,
        String passActionText,
        String status,
        boolean finished,
        int dealNumber,
        int totalDeals,
        String playerPositionName,
        String positionSummary
) implements SkatMessage {
}
