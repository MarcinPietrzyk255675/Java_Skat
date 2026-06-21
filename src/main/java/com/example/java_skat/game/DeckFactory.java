package com.example.java_skat.game;

import pl.skat.core.Karta;
import pl.skat.core.Kolor;
import pl.skat.core.Figura;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DeckFactory {
	private DeckFactory() {
	}

	public static List<Karta> createShuffledDeck() {
		List<Karta> deck = new ArrayList<>();

		for (Kolor kolor : Kolor.values()) {
			for (Figura figura : Figura.values()) {
				deck.add(new Karta(kolor, figura));
			}
		}

		Collections.shuffle(deck);
		return deck;
	}
}
