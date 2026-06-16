package com.example.java_skat;

import pl.skat.core.Figura;
import pl.skat.core.Karta;
import pl.skat.core.Kolor;

public class CardImagePath {

    public static String forCard(Karta karta) {
        return "/Cards/" + suitName(karta.kolor()) + "_" + rankName(karta.figura()) + ".png";
    }

    public static String back() {
        return "/Cards/card_back.png";
    }

    private static String suitName(Kolor kolor) {
        return switch (kolor) {
            case TREFL -> "card_clubs";
            case PIK -> "card_spades";
            case SERCE -> "card_hearts";
            case DZWONEK -> "card_diamonds";
        };
    }

    private static String rankName(Figura figura) {
        return switch (figura) {
            case AS -> "A";
            case KROL -> "K";
            case KROLOWA -> "Q";
            case JOPEK -> "J";
            case DZIESIATKA -> "10";
            case DZIEWIATKA -> "09";
            case OSEMKA -> "08";
            case SIODEMKA -> "07";
        };
    }
}