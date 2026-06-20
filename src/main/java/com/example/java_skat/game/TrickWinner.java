package com.example.java_skat.game;

import pl.skat.core.Figura;
import pl.skat.core.Karta;
import pl.skat.core.Kolor;
import pl.skat.core.RodzajGry;
import pl.skat.core.TypGry;

import java.util.Comparator;
import java.util.List;

public final class TrickWinner {
    private TrickWinner() {
    }

    public static PlayedCard strongestCard(List<PlayedCard> trick, RodzajGry gameType) {
        if (trick.isEmpty()) {
            throw new IllegalArgumentException("Nie można wyznaczyć zwycięzcy pustej lewy.");
        }

        boolean trumpWasPlayed = trick.stream().anyMatch(played -> TrickRules.isTrump(played.card(), gameType));
        if (trumpWasPlayed) {
            return trick.stream()
                    .filter(played -> TrickRules.isTrump(played.card(), gameType))
                    .max(Comparator.comparingInt(played -> trumpStrength(played.card(), gameType)))
                    .orElseThrow();
        }

        Kolor leadSuit = trick.get(0).card().kolor();
        return trick.stream()
                .filter(played -> played.card().kolor() == leadSuit)
                .max(Comparator.comparingInt(played -> nonTrumpStrength(played.card(), gameType)))
                .orElseThrow();
    }

    private static int trumpStrength(Karta card, RodzajGry gameType) {
        if (card.figura() == Figura.JOPEK) {
            return switch (card.kolor()) {
                case TREFL -> 100;
                case PIK -> 99;
                case SERCE -> 98;
                case DZWONEK -> 97;
            };
        }

        if (gameType != null && gameType.typ == TypGry.KOLOROWA && card.kolor() == gameType.kolor) {
            return rankStrength(card.figura(), gameType);
        }

        return 0;
    }

    private static int nonTrumpStrength(Karta card, RodzajGry gameType) {
        return rankStrength(card.figura(), gameType);
    }

    private static int rankStrength(Figura figure, RodzajGry gameType) {
        if (gameType != null && gameType.typ == TypGry.NULL) {
            return switch (figure) {
                case AS -> 8;
                case KROL -> 7;
                case KROLOWA -> 6;
                case JOPEK -> 5;
                case DZIESIATKA -> 4;
                case DZIEWIATKA -> 3;
                case OSEMKA -> 2;
                case SIODEMKA -> 1;
            };
        }

        return switch (figure) {
            case AS -> 8;
            case DZIESIATKA -> 7;
            case KROL -> 6;
            case KROLOWA -> 5;
            case JOPEK -> 4;
            case DZIEWIATKA -> 3;
            case OSEMKA -> 2;
            case SIODEMKA -> 1;
        };
    }
}
