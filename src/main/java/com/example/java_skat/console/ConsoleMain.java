package com.example.java_skat.console;

import com.example.java_skat.game.BidLadder;
import com.example.java_skat.game.GameController;
import com.example.java_skat.game.PlayerId;

public class ConsoleMain {
	public static void main(String[] args) {
		System.out.println("Pierwsza licytacja po 0: " + BidLadder.nextAfter(0).orElseThrow());
		System.out.println("Następna po 18: " + BidLadder.nextAfter(18).orElseThrow());
		System.out.println("Następna po 20: " + BidLadder.nextAfter(20).orElseThrow());
		GameController gameController = new GameController();
		for (int i = 0; i < 3; i++) {


			gameController.dealCards();

			System.out.println("Deal " + GameController.getDealNumber() + "/" + GameController.getMaxDeals());
			for (PlayerId player : PlayerId.values()) {
				System.out.println(player.getDisplayName() + "-" +
				                   gameController.getDealState().getPosition(player).displayName() + ": ");
				gameController.getDealState().getHand(player).forEach(System.out::println);
			}

			System.out.println("Skat: ");
			gameController.getDealState().getSkat().forEach(System.out::println);
		}
	}
}
