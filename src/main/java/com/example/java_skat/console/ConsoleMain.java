package com.example.java_skat.console;

import com.example.java_skat.game.GameController;
import com.example.java_skat.game.PlayerId;

public class ConsoleMain {
	public static void main(String[] args){
		GameController gameController = new GameController();
		gameController.dealCards();

		for (PlayerId player : PlayerId.values()){
			System.out.println(player.getDisplayName() + ": ");
			gameController.getDealState().getHand(player).forEach(System.out::println);
		}

		System.out.println("Skat: ");
		gameController.getDealState().getSkat().forEach(System.out::println);
	}
}
