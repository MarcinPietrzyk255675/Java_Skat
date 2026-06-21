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
                toDto(snapshot.ownTrickCard()),
                snapshot.ownTrickLabel(),
                toDto(snapshot.leftOpponentTrickCard()),
                snapshot.leftOpponentTrickLabel(),
                toDto(snapshot.rightOpponentTrickCard()),
                snapshot.rightOpponentTrickLabel(),
                toDto(snapshot.skat()),
                snapshot.skatVisible(),
                toDto(snapshot.topOpponentHand()),
                snapshot.topOpponentCardsVisible(),
                snapshot.topOpponentCardCount(),
                snapshot.topOpponentName(),
                toDto(snapshot.leftOpponentHand()),
                snapshot.leftOpponentCardsVisible(),
                snapshot.leftOpponentCardCount(),
                snapshot.leftOpponentName(),
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
                snapshot.bidActionText(),
                snapshot.passActionText(),
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
                toCore(message.ownTrickCard()),
                message.ownTrickLabel(),
                toCore(message.leftOpponentTrickCard()),
                message.leftOpponentTrickLabel(),
                toCore(message.rightOpponentTrickCard()),
                message.rightOpponentTrickLabel(),
                toCore(message.skat()),
                message.skatVisible(),
                toCore(message.topOpponentHand()),
                message.topOpponentCardsVisible(),
                message.topOpponentCardCount(),
                message.topOpponentName(),
                toCore(message.leftOpponentHand()),
                message.leftOpponentCardsVisible(),
                message.leftOpponentCardCount(),
                message.leftOpponentName(),
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
                message.bidActionText(),
                message.passActionText(),
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

    private static CardDto toDto(Karta card) {
        return card == null ? null : CardDto.fromCore(card);
    }

    private static List<Karta> toCore(List<CardDto> cards) {
        return cards.stream()
                .map(CardDto::toCore)
                .toList();
    }

    private static Karta toCore(CardDto card) {
        return card == null ? null : card.toCore();
    }
}
