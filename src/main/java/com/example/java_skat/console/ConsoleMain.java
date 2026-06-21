package com.example.java_skat.console;

import com.example.java_skat.game.BidLadder;
import com.example.java_skat.game.GameController;
import com.example.java_skat.game.PlayerId;
import pl.skat.core.Karta;

public class ConsoleMain {
	public static void main(String[] args) {
		System.out.println("Pierwsza licytacja po 0: " + BidLadder.nextAfter(0).orElseThrow());
		System.out.println("Następna po 18: " + BidLadder.nextAfter(18).orElseThrow());
		System.out.println("Następna po 20: " + BidLadder.nextAfter(20).orElseThrow());
		GameController gameController = new GameController();
		for (int i = 0; i < 3; i++) {


			gameController.dealCards();

			System.out.println("Deal " + gameController.getDealNumber() + "/" + GameController.getMaxDeals());
			for (PlayerId player : PlayerId.values()) {
				System.out.println(player.getDisplayName() + "-" +
				                   gameController.getDealState().getPosition(player).displayName() + ": ");
				gameController.getDealState().getHand(player).forEach(System.out::println);
			}

			System.out.println("Skat: ");
			gameController.getDealState().getSkat().forEach(System.out::println);


			System.out.println("Licytacja:");
			System.out.println("Pytający: " + gameController.getDealState().getBiddingAsker().getDisplayName());
			System.out.println(
					"Odpowiadający: " + gameController.getDealState().getBiddingResponder().getDisplayName());

			gameController.bid(gameController.getDealState().getBiddingAsker());
			System.out.println("Pytanie o: " + gameController.getDealState().getCurrentBid());

			gameController.acceptBid(gameController.getDealState().getBiddingResponder());
			System.out.println("Prowadzi: " + gameController.getDealState().getHighestBidder().getDisplayName());

			gameController.pass(gameController.getDealState().getBiddingAsker());

			System.out.println("Po pasie do licytacji wchodzi zadek:");
			System.out.println("Pytający: " + gameController.getDealState().getBiddingAsker().getDisplayName());
			System.out.println(
					"Odpowiadający: " + gameController.getDealState().getBiddingResponder().getDisplayName());

			gameController.bid(gameController.getDealState().getBiddingAsker());
			System.out.println("Pytanie o: " + gameController.getDealState().getCurrentBid());

			gameController.pass(gameController.getDealState().getBiddingResponder());

			System.out.println(
					"Zwycięzca licytacji: " + gameController.getDealState().getHighestBidder().getDisplayName());

			PlayerId declarer = gameController.getDealState().getDeclarer();

			System.out.println("Rozgrywający: " + declarer.getDisplayName());
			System.out.println("Faza po licytacji: " + gameController.getDealState().getPhase());

			gameController.takeSkat(declarer);

			System.out.println("Rozgrywający bierze skat.");
			System.out.println("Liczba kart rozgrywającego: " + gameController.getDealState().getHand(declarer).size());
			System.out.println("Liczba kart w skacie: " + gameController.getDealState().getSkat().size());
			System.out.println("Faza: " + gameController.getDealState().getPhase());

			Karta firstDiscard = gameController.getDealState().getHand(declarer).get(0);
			Karta secondDiscard = gameController.getDealState().getHand(declarer).get(1);

			gameController.discardToSkat(declarer, firstDiscard);
			gameController.discardToSkat(declarer, secondDiscard);

			System.out.println("Po odłożeniu 2 kart:");
			System.out.println("Liczba kart rozgrywającego: " + gameController.getDealState().getHand(declarer).size());
			System.out.println("Liczba kart w skacie: " + gameController.getDealState().getSkat().size());
			System.out.println("Faza: " + gameController.getDealState().getPhase());

		}
	}
}
