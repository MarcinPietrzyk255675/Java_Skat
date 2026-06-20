package com.example.java_skat.game;

import pl.skat.core.Figura;
import pl.skat.core.Karta;
import pl.skat.core.Kolor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

final class DeckFactory {
    private DeckFactory() {
    }

    static List<Karta> shuffledDeck() {
        List<Karta> deck = new ArrayList<>();

        for (Kolor kolor : Kolor.values()) {
            for (Figura figura : Figura.values()) {
                deck.add(new Karta(kolor, figura));
            }
        }

        Collections.shuffle(deck, new Random());
        return deck;
    }
}
