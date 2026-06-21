package com.example.java_skat.game;

import pl.skat.core.Karta;

import java.util.List;

public class GameController {

	private static final int HAND_SIZE = 10;
	private static final int SKAT_SIZE = 2;
	private static final int MAX_DEALS = 12;

	private DealState dealState;


	private int dealNumber = 0;

	public void dealCards() {
		if (dealNumber >= MAX_DEALS) {
			throw new IllegalStateException("Max deals reached");
		}

		dealNumber++;
		List<Karta> deck = DeckFactory.createShuffledDeck();
		dealState = new DealState();

		assignPositions();

		dealState.getHand(PlayerId.PLAYER_1).addAll(deck.subList(0, HAND_SIZE));
		dealState.getHand(PlayerId.PLAYER_2).addAll(deck.subList(HAND_SIZE, HAND_SIZE * 2));
		dealState.getHand(PlayerId.PLAYER_3).addAll(deck.subList(HAND_SIZE * 2, HAND_SIZE * 3));
		dealState.getSkat().addAll(deck.subList(HAND_SIZE * 3, HAND_SIZE * 3 + SKAT_SIZE));
	}

	public DealState getDealState() {
		return dealState;
	}

	public int getDealNumber() {
		return dealNumber;
	}

	public static int getMaxDeals() {
		return MAX_DEALS;
	}

	private void assignPositions() {
		PlayerId[] players = PlayerId.values();
		int shift = (dealNumber - 1) % players.length;
		dealState.setPosition(players[shift], PlayerPosition.FOREHAND);
		dealState.setPosition(players[(shift + 1) % players.length], PlayerPosition.MIDDLEHAND);
		dealState.setPosition(players[(shift + 2) % players.length], PlayerPosition.REARHAND);
	}

}
