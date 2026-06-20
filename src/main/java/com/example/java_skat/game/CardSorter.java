package com.example.java_skat.game;

import pl.skat.core.Figura;
import pl.skat.core.Karta;
import pl.skat.core.Kolor;
import pl.skat.core.RodzajGry;
import pl.skat.core.TypGry;

import java.util.Comparator;
import java.util.List;

public final class CardSorter {
    private CardSorter() {
    }

    public static void sort(List<Karta> cards, RodzajGry gameType) {
        cards.sort(comparator(gameType));
    }

    public static List<Karta> sortedCopy(List<Karta> cards, RodzajGry gameType) {
        return cards.stream()
                .sorted(comparator(gameType))
                .toList();
    }

    private static Comparator<Karta> comparator(RodzajGry gameType) {
        return Comparator
                .comparingInt((Karta card) -> group(card, gameType))
                .thenComparingInt(card -> orderInsideGroup(card, gameType))
                .thenComparingInt(card -> suitOrder(card.kolor()));
    }

    private static int group(Karta card, RodzajGry gameType) {
        if (TrickRules.isTrump(card, gameType)) {
            return 0;
        }
        return 1 + suitOrder(card.kolor());
    }

    private static int orderInsideGroup(Karta card, RodzajGry gameType) {
        if (TrickRules.isTrump(card, gameType)) {
            return trumpOrder(card, gameType);
        }
        return rankOrder(card.figura(), gameType);
    }

    private static int trumpOrder(Karta card, RodzajGry gameType) {
        if (card.figura() == Figura.JOPEK) {
            return switch (card.kolor()) {
                case TREFL -> 0;
                case PIK -> 1;
                case SERCE -> 2;
                case DZWONEK -> 3;
            };
        }

        if (gameType != null && gameType.typ == TypGry.KOLOROWA && card.kolor() == gameType.kolor) {
            return 4 + rankOrder(card.figura(), gameType);
        }

        return 99;
    }

    private static int rankOrder(Figura figure, RodzajGry gameType) {
        if (gameType != null && gameType.typ == TypGry.NULL) {
            return switch (figure) {
                case AS -> 0;
                case KROL -> 1;
                case KROLOWA -> 2;
                case JOPEK -> 3;
                case DZIESIATKA -> 4;
                case DZIEWIATKA -> 5;
                case OSEMKA -> 6;
                case SIODEMKA -> 7;
            };
        }

        return switch (figure) {
            case AS -> 0;
            case DZIESIATKA -> 1;
            case KROL -> 2;
            case KROLOWA -> 3;
            case JOPEK -> 4;
            case DZIEWIATKA -> 5;
            case OSEMKA -> 6;
            case SIODEMKA -> 7;
        };
    }

    private static int suitOrder(Kolor suit) {
        return switch (suit) {
            case TREFL -> 0;
            case PIK -> 1;
            case SERCE -> 2;
            case DZWONEK -> 3;
        };
    }
}
