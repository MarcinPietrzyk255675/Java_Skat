package com.example.java_skat.game;

import pl.skat.core.*;

import java.util.ArrayList;
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


		dealState.setPhase(GamePhase.BIDDING);
		dealState.setBiddingStatus(BiddingStatus.IN_PROGRESS);
		dealState.setBiddingAsker(middlehand);
		dealState.setBiddingResponder(forehand);
		dealState.setCurrentBid(0);
		dealState.setHighestBidder(null);
		dealState.setRearhandJoinedBidding(false);
	}

	private void finishBidding(PlayerId biddingWinner) {
		dealState.setHighestBidder(biddingWinner);
		dealState.setDeclarer(biddingWinner);
		dealState.setBiddingStatus(BiddingStatus.FINISHED);
		dealState.setPhase(GamePhase.DECLARER_DECISION);
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

		finishBidding(remainingPlayer);
	}

	public void takeSkat(PlayerId playerId) {
		if (dealState.getPhase() != GamePhase.DECLARER_DECISION) {
			throw new IllegalStateException("It is not the time to take the skat");
		}
		if (playerId != dealState.getDeclarer()) {
			throw new IllegalStateException("Only the declarer can take the skat");
		}

		dealState.getHand(playerId).addAll(dealState.getSkat());
		dealState.getSkat().clear();

		dealState.getRodzajGry().hand = false;
		dealState.setPhase(GamePhase.SKAT_EXCHANGE);
	}

	public void chooseHandGame(PlayerId playerId) {
		if (dealState.getPhase() != GamePhase.DECLARER_DECISION) {
			throw new IllegalStateException("It is not the time to declare hand game");
		}
		if (playerId != dealState.getDeclarer()) {
			throw new IllegalStateException("Only the declarer can declare hand game");
		}

		dealState.getRodzajGry().hand = true;
		dealState.setPhase(GamePhase.CONTRACT_SELECTION);
	}

	public void discardToSkat(PlayerId playerId, Karta card) {
		if (dealState.getPhase() != GamePhase.SKAT_EXCHANGE) {
			throw new IllegalStateException("It is not the time to discard to skat");
		}
		if (playerId != dealState.getDeclarer()) {
			throw new IllegalStateException("Only the declarer can discard to skat");
		}
		if (!dealState.getHand(playerId).remove(card)) {
			throw new IllegalStateException("Declarer does not have this card in hand");
		}

		dealState.getSkat().add(card);

		if (dealState.getSkat().size() == 2) {
			dealState.setPhase(GamePhase.CONTRACT_SELECTION);
		}
	}

	public void declareColorGame(PlayerId playerId, Kolor trumpColor) {
		if (dealState.getPhase() != GamePhase.CONTRACT_SELECTION) {
			throw new IllegalStateException("It is not the time to declare color game");
		}
		if (playerId != dealState.getDeclarer()) {
			throw new IllegalStateException("Only the declarer can declare color game");
		}

		dealState.getRodzajGry().typ = TypGry.KOLOROWA;
		dealState.getRodzajGry().kolor = trumpColor;

		dealState.getDeclarerStartingHand().clear();
		dealState.getDeclarerStartingHand().addAll(dealState.getHand(playerId));

		dealState.setCurrentPlayer(playerWithPosition(PlayerPosition.FOREHAND));
		dealState.setPhase(GamePhase.PLAYING);
	}

	private PlayerId nextPlayerAfter(PlayerId playerId) {
		PlayerPosition position = dealState.getPosition(playerId);
		PlayerPosition nextPosition = switch (position) {
			case FOREHAND -> PlayerPosition.MIDDLEHAND;
			case MIDDLEHAND -> PlayerPosition.REARHAND;
			case REARHAND -> PlayerPosition.FOREHAND;
		};
		return playerWithPosition(nextPosition);
	}

	public void playCard(PlayerId playerId, Karta card) {
		if (dealState.getPhase() != GamePhase.PLAYING) {
			throw new IllegalStateException("It is not the time to play a card");
		}
		if (playerId != dealState.getCurrentPlayer()) {
			throw new IllegalStateException("It is not your turn to play a card");
		}
		List<Karta> hand = dealState.getHand(playerId);

		if (!hand.contains(card)) {
			throw new IllegalStateException("Player does not have this card in hand");
		}

		if (!CardRules.canFollow(card, hand, dealState.getCurrentTrick(), dealState.getRodzajGry().kolor)) {
			throw new IllegalStateException("Player must follow suit or trump");
		}
		hand.remove(card);

		dealState.getCurrentTrick().add(new PlayerCard(playerId, card));
		if (dealState.getCurrentTrick().size() == 3) {
			PlayerId winner = TrickWinner.findWinner(dealState.getCurrentTrick(), dealState.getRodzajGry().kolor);

			dealState.getLastCompletedTrick().clear();
			dealState.getLastCompletedTrick().addAll(dealState.getCurrentTrick());

			for (PlayerCard playerCard : dealState.getCurrentTrick()) {
				dealState.getWonCards(winner).add(playerCard.card());
			}

			dealState.getCurrentTrick().clear();

			dealState.incrementCompletedTrickCount();

			dealState.setLastTrickWinner(winner);
			if (dealState.getCompletedTrickCount() == 10) {
				dealState.setCurrentPlayer(null);
				dealState.setPhase(GamePhase.DEAL_FINISHED);
				return;
			}
			dealState.setCurrentPlayer(winner);
			return;
		}
		dealState.setCurrentPlayer(nextPlayerAfter(playerId));
	}

	public WynikGry calculateCoreResult() {
        if (dealState.getPhase() != GamePhase.DEAL_FINISHED) {
                throw new IllegalStateException("Deal is not finished yet");
        }

        if (dealState.getDeclarerStartingHand().size() != 10) {
                throw new IllegalStateException("Declarer must have exactly 10 cards for scoring");
        }

        if (dealState.getSkat().size() != 2) {
                throw new IllegalStateException("Skat must contain exactly 2 cards for scoring");
        }

        Gracz coreDeclarer = new Gracz();
        Gracz coreOpponentOne = new Gracz();
        Gracz coreOpponentTwo = new Gracz();

        coreDeclarer.ustawPosiadaneKarty(new ArrayList<>(dealState.getDeclarerStartingHand()));
        coreDeclarer.ustawZebraneKarty(new ArrayList<>(dealState.getWonCards(dealState.getDeclarer())));

        Skat coreSkat = new Skat();
        coreSkat.ustawKarta1(dealState.getSkat().get(0));
        coreSkat.ustawKarta2(dealState.getSkat().get(1));

        Rozdanie coreDeal = new Rozdanie(coreDeclarer, coreOpponentOne, coreOpponentTwo);
        coreDeal.ustawRodzajGry(dealState.getRodzajGry());
        coreDeal.ustawWartoscLicytacji(dealState.getCurrentBid());
        coreDeal.ustawSkat(coreSkat);

        return coreDeal.obliczWynik();
}

}
