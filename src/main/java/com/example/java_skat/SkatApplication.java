package com.example.java_skat;

import com.example.java_skat.game.LocalGameController;
import com.example.java_skat.ui.SkatTableView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class SkatApplication extends Application {
    private static final int WIDTH = 1100;
    private static final int HEIGHT = 720;

    private LocalGameController gameController;
    private SkatTableView tableView;

    @Override
    public void start(Stage stage) {
        gameController = new LocalGameController();
        tableView = new SkatTableView();

        configureActions();
        renderCurrentState();

        Scene scene = new Scene(tableView, WIDTH, HEIGHT);
        addStylesheet(scene);

        stage.setTitle("Java Skat");
        stage.setScene(scene);
        stage.show();
    }

    private void configureActions() {
        tableView.setOnPlayCard(card -> {
            gameController.playCard(card);
            renderCurrentState();
        });

        tableView.setOnBid(() -> {
            gameController.bid();
            renderCurrentState();
        });

        tableView.setOnShowSkat(() -> {
            gameController.showSkat();
            renderCurrentState();
        });

        tableView.setOnPass(() -> {
            gameController.pass();
            renderCurrentState();
        });

        tableView.setOnNewDeal(() -> {
            gameController.startNewDeal();
            renderCurrentState();
        });
    }

    private void renderCurrentState() {
        tableView.render(gameController.snapshot());
    }

    private void addStylesheet(Scene scene) {
        URL stylesheet = getClass().getResource("/styles/skat.css");
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }
    }
}
