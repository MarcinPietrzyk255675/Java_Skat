package com.example.java_skat;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pl.skat.core.Figura;
import pl.skat.core.Karta;
import pl.skat.core.Kolor;

import java.util.List;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) {
        SkatTableView tableView = new SkatTableView();

        tableView.setPlayerHand(List.of(
                new Karta(Kolor.TREFL, Figura.AS),
                new Karta(Kolor.TREFL, Figura.DZIESIATKA),
                new Karta(Kolor.PIK, Figura.KROL),
                new Karta(Kolor.SERCE, Figura.KROLOWA),
                new Karta(Kolor.DZWONEK, Figura.JOPEK)
        ));

        tableView.setSkatHidden();
        tableView.setCurrentTrick(List.of());
        tableView.setOpponentCardCounts(10, 10);

        tableView.setOnPlayCard(karta -> {
            System.out.println("Gracz chce zagrać kartę: " + karta);
        });

        Scene scene = new Scene(tableView, 1000, 700);

        stage.setTitle("Skat");
        stage.setScene(scene);
        stage.show();
    }
}