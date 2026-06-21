package com.example.java_skat.game;

import pl.skat.core.Figura;
import pl.skat.core.Karta;
import pl.skat.core.Kolor;

import java.util.List;

public final class CardRules {
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

	public static boolean hasTrump(List<Karta> hand, Kolor trumpColor) {
		return hand.stream().anyMatch(card -> isTrump(card, trumpColor));
	}

	public static boolean hasColorWithoutTrumps(List<Karta> hand, Kolor color, Kolor trumpColor) {
		return hand.stream().anyMatch(card -> card.kolor() == color && !isTrump(card, trumpColor));
	}

	public static boolean canFollow(Karta cardToPlay, List<Karta> hand, List<PlayerCard> currentTrick,
	                                Kolor trumpColor) {
		if (currentTrick.isEmpty()) {
			return true;
		}

		Karta leadCard = currentTrick.getFirst().card();

		if (isTrump(leadCard, trumpColor)) {
			if (!hasTrump(hand, trumpColor)) {
				return true;
			}

			return isTrump(cardToPlay, trumpColor);
		}

		Kolor leadColor = leadCard.kolor();

		if (!hasColorWithoutTrumps(hand, leadColor, trumpColor)) {
			return true;
		}

		return cardToPlay.kolor() == leadColor && !isTrump(cardToPlay, trumpColor);
	}
}
