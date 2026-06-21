package com.example.java_skat.console;

import com.example.java_skat.game.GameController;
import com.example.java_skat.game.PlayerId;

public class ConsoleMain {
	public static void main(String[] args) {
		GameController gameController = new GameController();
		for (int i = 0; i < 3; i++) {


			gameController.dealCards();

			System.out.println("Deal " + (i + 1) + "/" + GameController.getMaxDeals());
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
