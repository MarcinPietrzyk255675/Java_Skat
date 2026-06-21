package com.example.java_skat.game;

import pl.skat.core.Karta;

import java.util.List;

public class GameController {

	private static final int HAND_SIZE = 10;
	private static final int SKAT_SIZE = 2;

	private DealState dealState;

	public void dealCards() {
		List<Karta> deck = DeckFactory.createShuffledDeck();
		dealState = new DealState();

		dealState.getHand(PlayerId.PLAYER_1).addAll(deck.subList(0, HAND_SIZE));
		dealState.getHand(PlayerId.PLAYER_2).addAll(deck.subList(HAND_SIZE, HAND_SIZE * 2));
		dealState.getHand(PlayerId.PLAYER_3).addAll(deck.subList(HAND_SIZE * 2, HAND_SIZE * 3));
		dealState.getSkat().addAll(deck.subList(HAND_SIZE * 3, HAND_SIZE * 3 + SKAT_SIZE));
	}

	public DealState getDealState() {
		return dealState;
	}
}
