package com.example.java_skat.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class WaitingRoomView extends BorderPane {
    private static final int REQUIRED_PLAYERS = 3;

    private final String titleText;
    private final String playerName;
    private final String host;
    private final int port;

    private final Label playersLabel = new Label();
    private final Label statusLabel = new Label();
    private final Button simulatePlayerButton = new Button("Symuluj dołączenie gracza");
    private final Button startGameButton = new Button("Rozpocznij prototyp");
    private final Button backButton = new Button("Wróć do menu");

    private int connectedPlayers = 1;
    private Runnable onStartGame;
    private Runnable onBack;

    public WaitingRoomView(String titleText, String playerName, String host, int port) {
        this.titleText = titleText;
        this.playerName = playerName;
        this.host = host;
        this.port = port;
        getStyleClass().add("skat-table");
        createLayout();
        updateState();
    }

    public void setOnStartGame(Runnable onStartGame) {
        this.onStartGame = onStartGame;
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    private void createLayout() {
        VBox box = new VBox(16);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));
        box.setMaxWidth(640);

        Label title = new Label(titleText);
        title.getStyleClass().add("menu-title");

        Label connectionInfo = new Label("Gracz: " + playerName + " | Host: " + host + " | Port: " + port);
        connectionInfo.getStyleClass().add("small-info-label");

        playersLabel.getStyleClass().add("status-label");
        statusLabel.getStyleClass().add("small-info-label");
        statusLabel.setWrapText(true);
        statusLabel.setAlignment(Pos.CENTER);

        HBox buttons = new HBox(10, simulatePlayerButton, startGameButton, backButton);
        buttons.setAlignment(Pos.CENTER);

        simulatePlayerButton.setOnAction(event -> {
            if (connectedPlayers < REQUIRED_PLAYERS) {
                connectedPlayers++;
                updateState();
            }
        });
        startGameButton.setOnAction(event -> {
            if (onStartGame != null) {
                onStartGame.run();
            }
        });
        backButton.setOnAction(event -> {
            if (onBack != null) {
                onBack.run();
            }
        });

        box.getChildren().addAll(title, connectionInfo, playersLabel, statusLabel, buttons);
        setCenter(box);
    }

    private void updateState() {
        playersLabel.setText("Oczekiwanie na graczy: " + connectedPlayers + "/" + REQUIRED_PLAYERS);
        boolean full = connectedPlayers >= REQUIRED_PLAYERS;
        startGameButton.setDisable(!full);
        simulatePlayerButton.setDisable(full);

        if (full) {
            statusLabel.setText("Komplet graczy. W prawdziwej wersji sieciowej tutaj serwer rozpocząłby rozdanie dla wszystkich klientów.");
        } else {
            statusLabel.setText("Poczekalnia jest przygotowana pod tryb sieciowy. Na razie przycisk symulacji pozwala sprawdzić przejście do gry bez implementowania pełnego lobby.");
        }
    }
}
