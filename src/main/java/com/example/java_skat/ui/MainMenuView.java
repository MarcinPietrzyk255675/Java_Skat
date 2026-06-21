package com.example.java_skat.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class MainMenuView extends BorderPane {
    private final TextField playerNameField = new TextField("Gracz");
    private final TextField hostField = new TextField("localhost");
    private final TextField portField = new TextField("8080");

    private final Button hostGameButton = new Button("Utwórz grę na localhost");
    private final Button joinGameButton = new Button("Dołącz do gry");

    private Runnable onHostGame;
    private Runnable onJoinGame;

    public MainMenuView() {
        getStyleClass().add("skat-table");
        createLayout();
    }

    public String playerName() {
        String name = playerNameField.getText();
        if (name == null || name.isBlank()) {
            return "Gracz";
        }
        return name.trim();
    }

    public String host() {
        String host = hostField.getText();
        if (host == null || host.isBlank()) {
            return "localhost";
        }
        return host.trim();
    }

    public int port() {
        try {
            return Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            return 8080;
        }
    }

    public void setOnHostGame(Runnable onHostGame) {
        this.onHostGame = onHostGame;
    }

    public void setOnJoinGame(Runnable onJoinGame) {
        this.onJoinGame = onJoinGame;
    }

    private void createLayout() {
        VBox menu = new VBox(16);
        menu.setAlignment(Pos.CENTER);
        menu.setPadding(new Insets(40));
        menu.setMaxWidth(560);

        Label title = new Label("Java Skat");
        title.getStyleClass().add("menu-title");

        Label subtitle = new Label("Tryb uproszczony: tylko 3 graczy przez localhost");
        subtitle.getStyleClass().add("status-label");
        subtitle.setWrapText(true);

        VBox fields = new VBox(8);
        fields.setAlignment(Pos.CENTER_LEFT);
        fields.getChildren().addAll(
                labeledField("Nazwa gracza", playerNameField),
                labeledField("Host", hostField),
                labeledField("Port", portField)
        );

        HBox buttons = new HBox(10, hostGameButton, joinGameButton);
        buttons.setAlignment(Pos.CENTER);

        hostGameButton.setOnAction(event -> {
            if (onHostGame != null) {
                onHostGame.run();
            }
        });
        joinGameButton.setOnAction(event -> {
            if (onJoinGame != null) {
                onJoinGame.run();
            }
        });

        Label info = new Label("Uruchom 3 instancje aplikacji. Pierwsza tworzy grę, dwie pozostałe dołączają na ten sam host i port.");
        info.getStyleClass().add("small-info-label");
        info.setWrapText(true);
        info.setAlignment(Pos.CENTER);

        menu.getChildren().addAll(title, subtitle, fields, buttons, info);
        setCenter(menu);
    }

    private VBox labeledField(String labelText, TextField field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("section-label");
        field.setMaxWidth(Double.MAX_VALUE);
        return new VBox(4, label, field);
    }
}
