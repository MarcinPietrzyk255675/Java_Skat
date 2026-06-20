package com.example.java_skat;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pl.skat.core.Figura;
import pl.skat.core.Karta;
import pl.skat.core.Kolor;

import java.util.List;

public class SkatApplication extends Application {

	public static final int WIDTH = 1000;
	public static final int HEIGHT = 700;
	public static final int STARTING_CARDS_NUMBER = 10;

	@Override
	public void start(Stage stage) {
		SkatTableView tableView = new SkatTableView();

		tableView.setPlayerHand(List.of(

				new Karta(Kolor.TREFL, Figura.AS), new Karta(Kolor.TREFL, Figura.DZIESIATKA),
				new Karta(Kolor.TREFL, Figura.KROL), new Karta(Kolor.PIK, Figura.KROL),
				new Karta(Kolor.PIK, Figura.DZIESIATKA), new Karta(Kolor.PIK, Figura.DZIEWIATKA),
				new Karta(Kolor.SERCE, Figura.KROLOWA), new Karta(Kolor.SERCE, Figura.OSEMKA),
				new Karta(Kolor.DZWONEK, Figura.JOPEK), new Karta(Kolor.DZWONEK, Figura.SIODEMKA)));


		tableView.setSkatHidden();
		tableView.setCurrentTrick(List.of());
		tableView.setOpponentCardCounts(STARTING_CARDS_NUMBER, STARTING_CARDS_NUMBER);

		tableView.setOnPlayCard(karta -> System.out.println("Gracz chce zagrać kartę: " + karta));

		Scene scene = new Scene(tableView, WIDTH, HEIGHT);

		stage.setTitle("Skat");
		stage.setScene(scene);
		stage.show();
	}
}