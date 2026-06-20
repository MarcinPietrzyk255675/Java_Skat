package com.example.java_skat.game;

import pl.skat.core.Kolor;
import pl.skat.core.RodzajGry;
import pl.skat.core.TypGry;

import java.util.Objects;

public record GameContract(
        TypGry type,
        Kolor color,
        boolean hand,
        boolean schneiderAnnounced,
        boolean schwarzAnnounced,
        boolean ouvert
) {
    public GameContract {
        Objects.requireNonNull(type, "Typ gry nie może być pusty.");
        if (type == TypGry.KOLOROWA) {
            color = Objects.requireNonNull(color, "Gra kolorowa wymaga wybranego koloru.");
        } else if (color == null) {
            color = Kolor.TREFL;
        }
    }

    public static GameContract defaultGrand() {
        return new GameContract(TypGry.GRAND, Kolor.TREFL, false, false, false, false);
    }

    public RodzajGry toCore() {
        RodzajGry gameType = new RodzajGry();
        gameType.typ = type;
        if (type == TypGry.KOLOROWA) {
            gameType.kolor = color;
        }
        gameType.hand = hand;
        gameType.schneiderZapowiedziany = schneiderAnnounced;
        gameType.schwarzZapowiedziany = schwarzAnnounced;
        gameType.ouvert = ouvert;
        return gameType;
    }

    public String displayName() {
        StringBuilder builder = new StringBuilder();
        builder.append(switch (type) {
            case KOLOROWA -> "Kolor: " + CardFormatter.formatSuit(color);
            case GRAND -> "Grand";
            case NULL -> "Null";
        });

        if (hand) {
            builder.append(" hand");
        }
        if (schneiderAnnounced) {
            builder.append(", krawiec zapowiedziany");
        }
        if (schwarzAnnounced) {
            builder.append(", szwarc zapowiedziany");
        }
        if (ouvert) {
            builder.append(", ouvert");
        }

        return builder.toString();
    }
}
