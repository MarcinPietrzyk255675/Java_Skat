package com.example.java_skat;

import com.example.java_skat.game.LocalGameController;
import com.example.java_skat.ui.MainMenuView;
import com.example.java_skat.ui.SkatTableView;
import com.example.java_skat.ui.WaitingRoomView;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class SkatApplication extends Application {
    private static final int WIDTH = 1100;
    private static final int HEIGHT = 720;

    private Stage stage;
    private LocalGameController gameController;
    private SkatTableView tableView;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("Java Skat");
        showMainMenu();
        stage.show();
    }

    private void showMainMenu() {
        MainMenuView menuView = new MainMenuView();
        menuView.setOnLocalGame(this::showLocalGame);
        menuView.setOnHostGame(() -> showWaitingRoom(
                "Poczekalnia gospodarza",
                menuView.playerName(),
                menuView.host(),
                menuView.port()
        ));
        menuView.setOnJoinGame(() -> showWaitingRoom(
                "Dołączanie do gry",
                menuView.playerName(),
                menuView.host(),
                menuView.port()
        ));
        setRoot(menuView);
    }

    private void showWaitingRoom(String title, String playerName, String host, int port) {
        WaitingRoomView waitingRoomView = new WaitingRoomView(title, playerName, host, port);
        waitingRoomView.setOnStartGame(this::showLocalGame);
        waitingRoomView.setOnBack(this::showMainMenu);
        setRoot(waitingRoomView);
    }

    private void showLocalGame() {
        gameController = new LocalGameController();
        tableView = new SkatTableView();

        configureActions();
        renderCurrentState();
        setRoot(tableView);
    }

    private void configureActions() {
        tableView.setOnPlayCard(card -> {
            gameController.playCard(card);
            renderCurrentState();
        });

        tableView.setOnDiscardCard(card -> {
            gameController.discardSelectedCardToSkat(card);
            renderCurrentState();
        });

        tableView.setOnConfirmContract(contract -> {
            gameController.confirmContract(contract);
            renderCurrentState();
        });

        tableView.setOnTakeSkat(() -> {
            gameController.takeSkatBeforeContract();
            renderCurrentState();
        });

        tableView.setOnChooseGameWithoutSkat(() -> {
            gameController.chooseGameWithoutTakingSkat();
            renderCurrentState();
        });

        tableView.setOnBid(() -> {
            gameController.bid();
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

    private void setRoot(Parent root) {
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        addStylesheet(scene);
        stage.setScene(scene);
    }

    private void addStylesheet(Scene scene) {
        URL stylesheet = getClass().getResource("/styles/skat.css");
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }
    }
}
