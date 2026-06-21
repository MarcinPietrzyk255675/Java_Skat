package com.example.java_skat.network;

import com.example.java_skat.game.GameSnapshot;
import pl.skat.core.Karta;

import java.util.List;

public final class MessageMapper {
    private MessageMapper() {
    }

    public static GameStateMessage toMessage(GameSnapshot snapshot) {
        return new GameStateMessage(
                toDto(snapshot.playerHand()),
                toDto(snapshot.currentTrick()),
                toDto(snapshot.skat()),
                snapshot.skatVisible(),
                snapshot.topOpponentCardCount(),
                snapshot.leftOpponentCardCount(),
                snapshot.collectedCardCount(),
                snapshot.phase(),
                snapshot.currentBid(),
                snapshot.nextBid(),
                snapshot.highestBidderName(),
                snapshot.contractName(),
                snapshot.cardsToDiscard(),
                snapshot.canTakeSkatBeforeContract(),
                snapshot.canChooseGameWithoutSkat(),
                snapshot.declarationsRestrictedAfterSkat(),
                snapshot.handRequired(),
                snapshot.canBid(),
                snapshot.canConfirmContract(),
                snapshot.canDiscard(),
                snapshot.canPlay(),
                snapshot.canPass(),
                snapshot.canNewDeal(),
                snapshot.status(),
                snapshot.finished(),
                snapshot.dealNumber(),
                snapshot.totalDeals(),
                snapshot.playerPositionName(),
                snapshot.positionSummary()
        );
    }

    public static GameSnapshot toSnapshot(GameStateMessage message) {
        return new GameSnapshot(
                toCore(message.playerHand()),
                toCore(message.currentTrick()),
                toCore(message.skat()),
                message.skatVisible(),
                message.topOpponentCardCount(),
                message.leftOpponentCardCount(),
                message.collectedCardCount(),
                message.phase(),
                message.currentBid(),
                message.nextBid(),
                message.highestBidderName(),
                message.contractName(),
                message.cardsToDiscard(),
                message.canTakeSkatBeforeContract(),
                message.canChooseGameWithoutSkat(),
                message.declarationsRestrictedAfterSkat(),
                message.handRequired(),
                message.canBid(),
                message.canConfirmContract(),
                message.canDiscard(),
                message.canPlay(),
                message.canPass(),
                message.canNewDeal(),
                message.status(),
                message.finished(),
                message.dealNumber(),
                message.totalDeals(),
                message.playerPositionName(),
                message.positionSummary()
        );
    }

    private static List<CardDto> toDto(List<Karta> cards) {
        return cards.stream()
                .map(CardDto::fromCore)
                .toList();
    }

    private static List<Karta> toCore(List<CardDto> cards) {
        return cards.stream()
                .map(CardDto::toCore)
                .toList();
    }
}
