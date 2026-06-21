package com.example.java_skat.game;

import pl.skat.core.Kolor;

import java.util.List;

public final class TrickWinner {
	private TrickWinner() {
	}

	public static PlayerId findWinner(List<PlayerCard> trick, Kolor trumpColor) {
		if (trick.size() != 3) {
			throw new IllegalArgumentException("A completed trick must contain exactly 3 cards");
		}

		boolean hasTrump = trick.stream().anyMatch(playerCard -> CardRules.isTrump(playerCard.card(), trumpColor));

		PlayerCard bestCard = trick.getFirst();

		for (PlayerCard playedCard : trick) {
			if (isBetter(playedCard, bestCard, trick.getFirst(), trumpColor, hasTrump)) {
				bestCard = playedCard;
			}
		}

		return bestCard.playerId();
	}

	private static boolean isBetter(PlayerCard candidate, PlayerCard currentBest, PlayerCard firstPlayedCard,
	                                Kolor trumpColor, boolean hasTrump) {
		if (hasTrump) {
			if (!CardRules.isTrump(candidate.card(), trumpColor)) {
				return false;
			}

			if (!CardRules.isTrump(currentBest.card(), trumpColor)) {
				return true;
			}

			return CardRules.trumpStrength(candidate.card(), trumpColor) >
			       CardRules.trumpStrength(currentBest.card(), trumpColor);
		}

		Kolor leadColor = firstPlayedCard.card().kolor();

		if (candidate.card().kolor() != leadColor) {
			return false;
		}

		if (currentBest.card().kolor() != leadColor) {
			return true;
		}

		return CardRules.normalSuitStrength(candidate.card()) > CardRules.normalSuitStrength(currentBest.card());
	}
}