package com.example.java_skat.ui;

import com.example.java_skat.network.WaitingRoomMessage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class WaitingRoomView extends BorderPane {
    private final String titleText;
    private final String playerName;
    private final String host;
    private final int port;

    private final Label playersLabel = new Label();
    private final Label playerListLabel = new Label();
    private final Label statusLabel = new Label();
    private final Button backButton = new Button("Wróć do menu");

    private Runnable onBack;

    public WaitingRoomView(String titleText, String playerName, String host, int port) {
        this.titleText = titleText;
        this.playerName = playerName;
        this.host = host;
        this.port = port;
        getStyleClass().add("skat-table");
        createLayout();
        setWaitingState("Łączenie z serwerem...");
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    public void render(WaitingRoomMessage message) {
        playersLabel.setText("Oczekiwanie na graczy: "
                + message.connectedPlayers() + "/" + message.requiredPlayers());
        playerListLabel.setText("Połączeni: " + String.join(", ", message.playerNames()));
        statusLabel.setText(message.status());
    }

    public void setWaitingState(String status) {
        playersLabel.setText("Oczekiwanie na graczy");
        playerListLabel.setText("Gracz: " + playerName);
        statusLabel.setText(status);
    }

    private void createLayout() {
        VBox box = new VBox(16);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));
        box.setMaxWidth(680);

        Label title = new Label(titleText);
        title.getStyleClass().add("menu-title");

        Label connectionInfo = new Label("Gracz: " + playerName + " | Host: " + host + " | Port: " + port);
        connectionInfo.getStyleClass().add("small-info-label");

        playersLabel.getStyleClass().add("status-label");
        playerListLabel.getStyleClass().add("small-info-label");
        playerListLabel.setWrapText(true);
        statusLabel.getStyleClass().add("small-info-label");
        statusLabel.setWrapText(true);
        statusLabel.setAlignment(Pos.CENTER);

        backButton.setOnAction(event -> {
            if (onBack != null) {
                onBack.run();
            }
        });

        box.getChildren().addAll(title, connectionInfo, playersLabel, playerListLabel, statusLabel, backButton);
        setCenter(box);
    }
}
