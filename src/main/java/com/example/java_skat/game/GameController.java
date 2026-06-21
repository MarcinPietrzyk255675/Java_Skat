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

		startBidding();
	}

	private void startBidding() {
		PlayerId forehand = playerWithPosition(PlayerPosition.FOREHAND);
		PlayerId middlehand = playerWithPosition(PlayerPosition.MIDDLEHAND);

		dealState.setBiddingStatus(BiddingStatus.IN_PROGRESS);
		dealState.setBiddingAsker(middlehand);
		dealState.setBiddingResponder(forehand);
		dealState.setCurrentBid(0);
		dealState.setHighestBidder(null);
		dealState.setRearhandJoinedBidding(false);
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

	private PlayerId playerWithPosition(PlayerPosition position) {
		for (PlayerId player : PlayerId.values()) {
			if (dealState.getPosition(player) == position) {
				return player;
			}
		}
		throw new IllegalStateException("No player with position " + position);
	}

	public void bid(PlayerId playerId) {
		if (dealState.getBiddingStatus() != BiddingStatus.IN_PROGRESS) {
			throw new IllegalStateException("Bidding is not in progress");
		}
		if (playerId != dealState.getBiddingAsker()) {
			throw new IllegalStateException("It is not your turn to ask a bid");
		}
		int nextBid = BidLadder.nextAfter(dealState.getCurrentBid()).orElseThrow(
				() -> new IllegalStateException("No more bids"));
		dealState.setCurrentBid(nextBid);
	}

	public void acceptBid(PlayerId playerId) {
		if (dealState.getBiddingStatus() != BiddingStatus.IN_PROGRESS) {
			throw new IllegalStateException("Bidding is not in progress");
		}
		if (playerId != dealState.getBiddingResponder()) {
			throw new IllegalStateException("It is not your turn to accept a bid");
		}
		if (dealState.getCurrentBid() == 0) {
			throw new IllegalStateException("There is no bid to accept");
		}

		dealState.setHighestBidder(playerId);
	}

	public void pass(PlayerId playerId) {
		if (dealState.getBiddingStatus() != BiddingStatus.IN_PROGRESS) {
			throw new IllegalStateException("Bidding is not in progress");
		}
		if (playerId != dealState.getBiddingResponder() && playerId != dealState.getBiddingAsker()) {
			throw new IllegalStateException("It is not your turn to pass");
		}

		PlayerId remainingPlayer = dealState.getBiddingResponder() == playerId ? dealState.getBiddingAsker() :
		                           dealState.getBiddingResponder();


		dealState.setHighestBidder(remainingPlayer);

		if (!dealState.isRearhandJoinedBidding()) {
			PlayerId rearhand = playerWithPosition(PlayerPosition.REARHAND);

			dealState.setBiddingResponder(remainingPlayer);
			dealState.setBiddingAsker(rearhand);
			dealState.setRearhandJoinedBidding(true);
			return;
		}

		dealState.setBiddingStatus(BiddingStatus.FINISHED);
	}

}
