package com.example.java_skat.console;

import com.example.java_skat.game.BidLadder;
import com.example.java_skat.game.CardRules;
import com.example.java_skat.game.GameController;
import com.example.java_skat.game.GamePhase;
import com.example.java_skat.game.PlayerId;
import pl.skat.core.Karta;
import pl.skat.core.Kolor;
import pl.skat.core.WynikGry;

public class ConsoleMain {
	public static void main(String[] args) {
		System.out.println("Pierwsza licytacja po 0: " + BidLadder.nextAfter(0).orElseThrow());
		System.out.println("Następna po 18: " + BidLadder.nextAfter(18).orElseThrow());
		System.out.println("Następna po 20: " + BidLadder.nextAfter(20).orElseThrow());
		System.out.println();

		GameController gameController = new GameController();

		for (int i = 0; i < GameController.getMaxDeals(); i++) {
			gameController.dealCards();

			simulateBidding(gameController);
			simulateSkatAndDeclaration(gameController);
			playAllTricks(gameController);

			WynikGry wynik = gameController.finishDealAndSaveScore();

			System.out.println("Rozdanie " + gameController.getDealNumber() + " | rozgrywający: " +
			                   gameController.getDealState().getDeclarer().getDisplayName() + " | wygrana: " +
			                   wynik.wygrana + " | wynik: " + wynik.wynik + " | wartość gry: " +
			                   gameController.calculateCoreGameValue() + " | schneider: " +
			                   gameController.getDealState().getRodzajGry().schneider + " | schwarz: " +
			                   gameController.getDealState().getRodzajGry().schwarz);
		}

		System.out.println();
		System.out.println("Koniec gry.");
		System.out.println("Czy gra skończona: " + gameController.isGameFinished());
		System.out.println("Tabela wyników:");

		for (PlayerId player : PlayerId.values()) {
			System.out.println(player.getDisplayName() + " | suma: " + gameController.getPlayerScore(player) +
			                   " | liczba rozgrywek: " + gameController.getPlayerResults(player).size());
		}
	}

	private static void simulateBidding(GameController gameController) {
		gameController.bid(gameController.getDealState().getBiddingAsker());
		gameController.acceptBid(gameController.getDealState().getBiddingResponder());
		gameController.pass(gameController.getDealState().getBiddingAsker());

		gameController.bid(gameController.getDealState().getBiddingAsker());
		gameController.pass(gameController.getDealState().getBiddingResponder());
	}

	private static void simulateSkatAndDeclaration(GameController gameController) {
		PlayerId declarer = gameController.getDealState().getDeclarer();

		gameController.takeSkat(declarer);

		Karta firstDiscard = gameController.getDealState().getHand(declarer).get(0);
		Karta secondDiscard = gameController.getDealState().getHand(declarer).get(1);

		gameController.discardToSkat(declarer, firstDiscard);
		gameController.discardToSkat(declarer, secondDiscard);

		gameController.declareColorGame(declarer, Kolor.SERCE);
	}

	private static void playAllTricks(GameController gameController) {
		while (gameController.getDealState().getPhase() == GamePhase.PLAYING) {
			for (int cardNumber = 0; cardNumber < 3; cardNumber++) {
				PlayerId currentPlayer = gameController.getDealState().getCurrentPlayer();
				Karta card = firstLegalCard(gameController, currentPlayer);

				gameController.playCard(currentPlayer, card);
			}
		}
	}

	private static Karta firstLegalCard(GameController gameController, PlayerId playerId) {
		return gameController.getDealState().getHand(playerId).stream().filter(
				card -> CardRules.canFollow(card, gameController.getDealState().getHand(playerId),
				                            gameController.getDealState().getCurrentTrick(),
				                            gameController.getDealState().getRodzajGry().kolor)).findFirst().orElseThrow();
	}
}