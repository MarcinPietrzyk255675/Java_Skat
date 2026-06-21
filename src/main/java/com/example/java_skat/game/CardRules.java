package com.example.java_skat.game;

import pl.skat.core.Figura;
import pl.skat.core.Karta;
import pl.skat.core.Kolor;

public class CardRules {
	private CardRules() {
	}

	public static boolean isTrump(Karta card, Kolor trumpColor) {
		return card.figura() == Figura.JOPEK || card.kolor() == trumpColor;
	}

	public static int trumpStrength(Karta card, Kolor trumpColor) {
		if (!isTrump(card, trumpColor)) {
			return 0;
		}

		if (card.figura() == Figura.JOPEK) {
			return switch (card.kolor()) {
				case TREFL -> 100;
				case PIK -> 99;
				case SERCE -> 98;
				case DZWONEK -> 97;
			};
		}

		return normalSuitStrength(card);
	}

	public static int normalSuitStrength(Karta card) {
		return switch (card.figura()) {
			case AS -> 7;
			case DZIESIATKA -> 6;
			case KROL -> 5;
			case KROLOWA -> 4;
			case DZIEWIATKA -> 3;
			case OSEMKA -> 2;
			case SIODEMKA -> 1;
			case JOPEK -> 0;
		};
	}
}
