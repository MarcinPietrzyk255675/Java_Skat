package com.example.java_skat.game;

import pl.skat.core.Figura;
import pl.skat.core.Karta;
import pl.skat.core.Kolor;

public final class CardFormatter {
    private CardFormatter() {
    }

    public static String format(Karta karta) {
        return rank(karta.figura()) + " " + formatSuit(karta.kolor());
    }

    private static String rank(Figura figura) {
        return switch (figura) {
            case AS -> "As";
            case KROL -> "Król";
            case KROLOWA -> "Dama";
            case JOPEK -> "Jopek";
            case DZIESIATKA -> "10";
            case DZIEWIATKA -> "9";
            case OSEMKA -> "8";
            case SIODEMKA -> "7";
        };
    }

    public static String formatSuit(Kolor kolor) {
        return switch (kolor) {
            case TREFL -> "trefl";
            case PIK -> "pik";
            case SERCE -> "serce";
            case DZWONEK -> "dzwonek";
        };
    }
}
