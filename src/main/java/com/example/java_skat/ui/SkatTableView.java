package com.example.java_skat.ui;

import com.example.java_skat.game.GamePhase;
import com.example.java_skat.game.GameSnapshot;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import pl.skat.core.Karta;

import java.util.List;
import java.util.function.Consumer;

public class SkatTableView extends BorderPane {
    private final PlayerHandView playerHandView = new PlayerHandView();
    private final HBox currentTrickBox = new HBox(12);
    private final HBox skatBox = new HBox(8);

    private final Label topOpponentLabel = new Label();
    private final Label leftOpponentLabel = new Label();
    private final Label bidLabel = new Label();
    private final Label statusLabel = new Label();
    private final Label collectedCardsLabel = new Label();

    private final Button bidButton = new Button("Licytuj");
    private final Button playCardButton = new Button("Zagraj kartę");
    private final Button showSkatButton = new Button("Pokaż skat");
    private final Button passButton = new Button("Pas");
    private final Button newDealButton = new Button("Nowe rozdanie");

    private Consumer<Karta> onPlayCard;
    private Runnable onBid;
    private Runnable onShowSkat;
    private Runnable onPass;
    private Runnable onNewDeal;

    public SkatTableView() {
        getStyleClass().add("skat-table");
        createTopArea();
        createLeftArea();
        createCenterArea();
        createBottomArea();
    }

    public void render(GameSnapshot snapshot) {
        playerHandView.setCards(snapshot.playerHand());
        setCurrentTrick(snapshot.currentTrick());
        setSkat(snapshot.skat(), snapshot.skatVisible());
        setOpponentCardCounts(snapshot.topOpponentCardCount(), snapshot.leftOpponentCardCount());
        setBidInfo(snapshot);
        statusLabel.setText(snapshot.status());
        collectedCardsLabel.setText("Karty zebrane przez gracza: " + snapshot.collectedCardCount());

        boolean bidding = snapshot.phase() == GamePhase.BIDDING;
        boolean playing = snapshot.phase() == GamePhase.PLAYING;

        bidButton.setText(bidding ? "Licytuj " + snapshot.nextBid() : "Licytacja zakończona");
        bidButton.setDisable(!bidding || snapshot.nextBid() == 0 || snapshot.finished());

        playCardButton.setDisable(!playing || snapshot.finished() || snapshot.playerHand().isEmpty());
        showSkatButton.setDisable(!playing || snapshot.finished() || snapshot.skatVisible());
        passButton.setText(bidding ? "Pas" : "Poddaj rozdanie");
        passButton.setDisable(snapshot.finished());
    }

    public void setOnPlayCard(Consumer<Karta> onPlayCard) {
        this.onPlayCard = onPlayCard;
    }

    public void setOnBid(Runnable onBid) {
        this.onBid = onBid;
    }

    public void setOnShowSkat(Runnable onShowSkat) {
        this.onShowSkat = onShowSkat;
    }

    public void setOnPass(Runnable onPass) {
        this.onPass = onPass;
    }

    public void setOnNewDeal(Runnable onNewDeal) {
        this.onNewDeal = onNewDeal;
    }

    private void createTopArea() {
        VBox topArea = new VBox(8);
        topArea.setAlignment(Pos.CENTER);
        topArea.setPadding(new Insets(12));

        topOpponentLabel.getStyleClass().add("opponent-label");
        bidLabel.getStyleClass().add("small-info-label");
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setWrapText(true);

        topArea.getChildren().addAll(topOpponentLabel, bidLabel, statusLabel);
        setTop(topArea);
    }

    private void createLeftArea() {
        VBox leftArea = new VBox(12);
        leftArea.setAlignment(Pos.CENTER);
        leftArea.setPadding(new Insets(12));

        leftOpponentLabel.getStyleClass().add("opponent-label");
        collectedCardsLabel.getStyleClass().add("small-info-label");

        leftArea.getChildren().addAll(leftOpponentLabel, collectedCardsLabel);
        setLeft(leftArea);
    }

    private void createCenterArea() {
        StackPane centerArea = new StackPane();
        centerArea.setPadding(new Insets(20));
        centerArea.getStyleClass().add("table-center");

        currentTrickBox.setAlignment(Pos.CENTER);
        currentTrickBox.getStyleClass().add("current-trick");

        skatBox.setAlignment(Pos.TOP_RIGHT);
        skatBox.getStyleClass().add("skat-box");

        Label trickLabel = sectionLabel("Aktualna lewa");
        VBox trickArea = new VBox(10, trickLabel, currentTrickBox);
        trickArea.setAlignment(Pos.CENTER);

        Label skatLabel = sectionLabel("Skat");
        VBox skatArea = new VBox(6, skatLabel, skatBox);
        skatArea.setAlignment(Pos.TOP_RIGHT);
        StackPane.setAlignment(skatArea, Pos.TOP_RIGHT);

        centerArea.getChildren().addAll(trickArea, skatArea);
        setCenter(centerArea);
    }

    private void createBottomArea() {
        VBox bottomArea = new VBox(12);
        bottomArea.setAlignment(Pos.CENTER);
        bottomArea.setPadding(new Insets(12));
        VBox.setVgrow(playerHandView, Priority.NEVER);

        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.getChildren().addAll(bidButton, playCardButton, showSkatButton, passButton, newDealButton);

        bidButton.setOnAction(event -> {
            if (onBid != null) {
                onBid.run();
            }
        });

        playCardButton.setOnAction(event -> playerHandView.selectedCard().ifPresentOrElse(
                card -> {
                    if (onPlayCard != null) {
                        onPlayCard.accept(card);
                    }
                },
                () -> statusLabel.setText("Najpierw wybierz kartę z ręki.")
        ));

        showSkatButton.setOnAction(event -> {
            if (onShowSkat != null) {
                onShowSkat.run();
            }
        });

        passButton.setOnAction(event -> {
            if (onPass != null) {
                onPass.run();
            }
        });

        newDealButton.setOnAction(event -> {
            if (onNewDeal != null) {
                onNewDeal.run();
            }
        });

        bottomArea.getChildren().addAll(playerHandView, actionButtons);
        setBottom(bottomArea);
    }

    private void setCurrentTrick(List<Karta> cardsOnTable) {
        currentTrickBox.getChildren().clear();
        for (Karta card : cardsOnTable) {
            currentTrickBox.getChildren().add(new CardView(card));
        }
    }

    private void setSkat(List<Karta> skatCards, boolean visible) {
        skatBox.getChildren().clear();

        if (visible) {
            for (Karta card : skatCards) {
                skatBox.getChildren().add(new CardView(card));
            }
            return;
        }

        skatBox.getChildren().add(new CardBackView());
        skatBox.getChildren().add(new CardBackView());
    }

    private void setOpponentCardCounts(int topOpponentCards, int leftOpponentCards) {
        topOpponentLabel.setText("Przeciwnik 1: " + topOpponentCards + " kart");
        leftOpponentLabel.setText("Przeciwnik 2: " + leftOpponentCards + " kart");
    }

    private void setBidInfo(GameSnapshot snapshot) {
        if (snapshot.currentBid() == 0) {
            bidLabel.setText("Licytacja: brak ofert");
            return;
        }

        bidLabel.setText("Licytacja: " + snapshot.currentBid()
                + ", prowadzi: " + snapshot.highestBidderName());
    }

    private Label sectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-label");
        return label;
    }
}
