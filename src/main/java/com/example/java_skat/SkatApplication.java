package com.example.java_skat;

import com.example.java_skat.game.GameContract;
import com.example.java_skat.game.LocalGameController;
import com.example.java_skat.network.BidMessage;
import com.example.java_skat.network.ChooseGameWithoutSkatMessage;
import com.example.java_skat.network.ConfirmContractMessage;
import com.example.java_skat.network.DiscardCardMessage;
import com.example.java_skat.network.ErrorMessage;
import com.example.java_skat.network.GameContractDto;
import com.example.java_skat.network.GameStateMessage;
import com.example.java_skat.network.JoinGameMessage;
import com.example.java_skat.network.MessageMapper;
import com.example.java_skat.network.MultiplayerGameServer;
import com.example.java_skat.network.NewDealMessage;
import com.example.java_skat.network.PassMessage;
import com.example.java_skat.network.PlayCardMessage;
import com.example.java_skat.network.SkatClientConnection;
import com.example.java_skat.network.SkatMessage;
import com.example.java_skat.network.TakeSkatMessage;
import com.example.java_skat.network.WaitingRoomMessage;
import com.example.java_skat.network.CardDto;
import com.example.java_skat.ui.MainMenuView;
import com.example.java_skat.ui.SkatTableView;
import com.example.java_skat.ui.WaitingRoomView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pl.skat.core.Karta;

import java.io.IOException;
import java.net.URL;

public class SkatApplication extends Application {
    private static final int WIDTH = 1100;
    private static final int HEIGHT = 720;

    private Stage stage;
    private LocalGameController gameController;
    private SkatTableView tableView;
    private WaitingRoomView waitingRoomView;
    private SkatClientConnection networkClient;
    private MultiplayerGameServer embeddedServer;
    private boolean networkMode;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("Java Skat");
        showMainMenu();
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        closeNetworkResources();
        super.stop();
    }

    private void showMainMenu() {
        closeNetworkResources();
        networkMode = false;
        MainMenuView menuView = new MainMenuView();
        menuView.setOnLocalGame(this::showLocalGame);
        menuView.setOnHostGame(() -> hostNetworkGame(menuView.playerName(), menuView.port()));
        menuView.setOnJoinGame(() -> joinNetworkGame(menuView.playerName(), menuView.host(), menuView.port()));
        setRoot(menuView);
    }

    private void showLocalGame() {
        closeNetworkResources();
        networkMode = false;
        gameController = new LocalGameController();
        tableView = new SkatTableView();

        configureLocalActions();
        renderCurrentLocalState();
        setRoot(tableView);
    }

    private void hostNetworkGame(String playerName, int port) {
        closeNetworkResources();
        embeddedServer = new MultiplayerGameServer();
        try {
            embeddedServer.start(port);
            joinNetworkGame(playerName, "localhost", port);
        } catch (IOException | RuntimeException e) {
            showConnectionError("Nie udało się utworzyć gry na porcie " + port + ": " + e.getMessage());
        }
    }

    private void joinNetworkGame(String playerName, String host, int port) {
        networkMode = true;
        waitingRoomView = new WaitingRoomView("Poczekalnia gry sieciowej", playerName, host, port);
        waitingRoomView.setOnBack(this::showMainMenu);
        setRoot(waitingRoomView);

        networkClient = new SkatClientConnection();
        try {
            networkClient.connect(host, port, message -> Platform.runLater(() -> handleNetworkMessage(message)));
            networkClient.send(new JoinGameMessage(playerName));
        } catch (IOException e) {
            showConnectionError("Nie udało się połączyć z " + host + ":" + port + ". " + e.getMessage());
        }
    }

    private void handleNetworkMessage(SkatMessage message) {
        switch (message) {
            case WaitingRoomMessage waitingRoomMessage -> {
                if (waitingRoomView != null) {
                    waitingRoomView.render(waitingRoomMessage);
                }
            }
            case GameStateMessage gameStateMessage -> renderNetworkGame(gameStateMessage);
            case ErrorMessage errorMessage -> showConnectionError(errorMessage.message());
            default -> {
                // Pozostałe wiadomości są wysyłane od klienta do serwera.
            }
        }
    }

    private void renderNetworkGame(GameStateMessage message) {
        if (tableView == null || !networkMode) {
            tableView = new SkatTableView();
            configureNetworkActions();
            setRoot(tableView);
        }
        tableView.render(MessageMapper.toSnapshot(message));
    }

    private void configureLocalActions() {
        tableView.setOnPlayCard(card -> {
            gameController.playCard(card);
            renderCurrentLocalState();
        });

        tableView.setOnDiscardCard(card -> {
            gameController.discardSelectedCardToSkat(card);
            renderCurrentLocalState();
        });

        tableView.setOnConfirmContract(contract -> {
            gameController.confirmContract(contract);
            renderCurrentLocalState();
        });

        tableView.setOnTakeSkat(() -> {
            gameController.takeSkatBeforeContract();
            renderCurrentLocalState();
        });

        tableView.setOnChooseGameWithoutSkat(() -> {
            gameController.chooseGameWithoutTakingSkat();
            renderCurrentLocalState();
        });

        tableView.setOnBid(() -> {
            gameController.bid();
            renderCurrentLocalState();
        });

        tableView.setOnPass(() -> {
            gameController.pass();
            renderCurrentLocalState();
        });

        tableView.setOnNewDeal(() -> {
            gameController.startNewDeal();
            renderCurrentLocalState();
        });
    }

    private void configureNetworkActions() {
        tableView.setOnPlayCard(card -> sendNetworkMessage(new PlayCardMessage(CardDto.fromCore(card))));
        tableView.setOnDiscardCard(card -> sendNetworkMessage(new DiscardCardMessage(CardDto.fromCore(card))));
        tableView.setOnConfirmContract(this::sendContract);
        tableView.setOnTakeSkat(() -> sendNetworkMessage(new TakeSkatMessage()));
        tableView.setOnChooseGameWithoutSkat(() -> sendNetworkMessage(new ChooseGameWithoutSkatMessage()));
        tableView.setOnBid(() -> sendNetworkMessage(new BidMessage()));
        tableView.setOnPass(() -> sendNetworkMessage(new PassMessage()));
        tableView.setOnNewDeal(() -> sendNetworkMessage(new NewDealMessage()));
    }

    private void sendContract(GameContract contract) {
        sendNetworkMessage(new ConfirmContractMessage(GameContractDto.fromGameContract(contract)));
    }

    private void sendNetworkMessage(SkatMessage message) {
        if (networkClient == null) {
            showConnectionError("Brak połączenia z serwerem.");
            return;
        }
        try {
            networkClient.send(message);
        } catch (IOException e) {
            showConnectionError("Nie udało się wysłać wiadomości do serwera: " + e.getMessage());
        }
    }

    private void renderCurrentLocalState() {
        tableView.render(gameController.snapshot());
    }

    private void showConnectionError(String message) {
        if (waitingRoomView != null && networkMode) {
            waitingRoomView.setWaitingState(message);
            return;
        }
        System.err.println(message);
    }

    private void closeNetworkResources() {
        if (networkClient != null) {
            try {
                networkClient.close();
            } catch (IOException ignored) {
            }
            networkClient = null;
        }
        if (embeddedServer != null) {
            try {
                embeddedServer.close();
            } catch (IOException ignored) {
            }
            embeddedServer = null;
        }
        waitingRoomView = null;
        tableView = null;
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
