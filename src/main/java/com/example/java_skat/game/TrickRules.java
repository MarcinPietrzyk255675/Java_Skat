package com.example.java_skat.game;

import pl.skat.core.Figura;
import pl.skat.core.Karta;
import pl.skat.core.Kolor;
import pl.skat.core.RodzajGry;
import pl.skat.core.TypGry;

import java.util.List;
import java.util.Optional;

public final class TrickRules {
    private TrickRules() {
    }

    public static boolean canPlay(Karta card, List<Karta> hand, List<Karta> currentTrick, RodzajGry gameType) {
        if (currentTrick.isEmpty()) {
            return true;
        }

        Karta leadCard = currentTrick.get(0);

        if (isTrump(leadCard, gameType)) {
            return !hasTrump(hand, gameType) || isTrump(card, gameType);
        }

        return !hasNonTrumpSuit(hand, leadCard.kolor(), gameType) || isNonTrumpSuit(card, leadCard.kolor(), gameType);
    }

    public static Optional<Karta> firstLegalCard(List<Karta> hand, List<Karta> currentTrick, RodzajGry gameType) {
        return hand.stream()
                .filter(card -> canPlay(card, hand, currentTrick, gameType))
                .findFirst();
    }

    public static String illegalMoveMessage(Karta card, List<Karta> hand, List<Karta> currentTrick, RodzajGry gameType) {
        if (currentTrick.isEmpty()) {
            return "Ta karta może zostać zagrana.";
        }

        Karta leadCard = currentTrick.get(0);

        if (isTrump(leadCard, gameType)) {
            return "Pierwsza karta w lewie jest atutem, więc musisz dołożyć atut, jeśli go masz.";
        }

        return "Musisz dołożyć do koloru " + CardFormatter.formatSuit(leadCard.kolor()) + ", jeśli masz taki kolor.";
    }

    public static boolean isTrump(Karta card, RodzajGry gameType) {
        if (gameType == null || gameType.typ == TypGry.NULL) {
            return false;
        }

        if (card.figura() == Figura.JOPEK) {
            return true;
        }

        return gameType.typ == TypGry.KOLOROWA && card.kolor() == gameType.kolor;
    }

    private static boolean hasTrump(List<Karta> hand, RodzajGry gameType) {
        return hand.stream().anyMatch(card -> isTrump(card, gameType));
    }

    private static boolean hasNonTrumpSuit(List<Karta> hand, Kolor suit, RodzajGry gameType) {
        return hand.stream().anyMatch(card -> isNonTrumpSuit(card, suit, gameType));
    }

    private static boolean isNonTrumpSuit(Karta card, Kolor suit, RodzajGry gameType) {
        return card.kolor() == suit && !isTrump(card, gameType);
    }
}
