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
                snapshot.status(),
                snapshot.finished()
        );
    }

    private static List<CardDto> toDto(List<Karta> cards) {
        return cards.stream()
                .map(CardDto::fromCore)
                .toList();
    }
}
