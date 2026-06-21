package com.example.java_skat.ui;

import com.example.java_skat.game.CardFormatter;
import com.example.java_skat.game.GameContract;
import com.example.java_skat.game.GamePhase;
import com.example.java_skat.game.GameSnapshot;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import pl.skat.core.Karta;
import pl.skat.core.Kolor;
import pl.skat.core.TypGry;

import java.util.List;
import java.util.function.Consumer;

public class SkatTableView extends BorderPane {
    private final PlayerHandView playerHandView = new PlayerHandView();
    private final HBox currentTrickBox = new HBox(12);
    private final HBox skatBox = new HBox(8);
    private final HBox contractControls = new HBox(8);

    private final Label topOpponentLabel = new Label();
    private final Label leftOpponentLabel = new Label();
    private final Label dealLabel = new Label();
    private final Label positionsLabel = new Label();
    private final Label bidLabel = new Label();
    private final Label contractLabel = new Label();
    private final Label statusLabel = new Label();
    private final Label collectedCardsLabel = new Label();

    private final Button bidButton = new Button("Licytuj");
    private final Button takeSkatButton = new Button("Weź skat");
    private final Button chooseGameWithoutSkatButton = new Button("Gra bez skata");
    private final Button confirmContractButton = new Button("Zatwierdź grę");
    private final Button discardCardButton = new Button("Odłóż kartę");
    private final Button playCardButton = new Button("Zagraj kartę");
    private final Button passButton = new Button("Pas");
    private final Button newDealButton = new Button("Nowe rozdanie");

    private final ComboBox<TypGry> gameTypeComboBox = new ComboBox<>();
    private final ComboBox<Kolor> colorComboBox = new ComboBox<>();
    private final CheckBox handCheckBox = new CheckBox("hand");
    private final CheckBox schneiderCheckBox = new CheckBox("krawiec zapowiedziany");
    private final CheckBox schwarzCheckBox = new CheckBox("szwarc zapowiedziany");
    private final CheckBox ouvertCheckBox = new CheckBox("ouvert");

    private Consumer<Karta> onPlayCard;
    private Consumer<Karta> onDiscardCard;
    private Consumer<GameContract> onConfirmContract;
    private Runnable onTakeSkat;
    private Runnable onChooseGameWithoutSkat;
    private Runnable onBid;
    private Runnable onPass;
    private Runnable onNewDeal;
    private GameSnapshot lastSnapshot;

    public SkatTableView() {
        getStyleClass().add("skat-table");
        configureContractControls();
        createTopArea();
        createLeftArea();
        createCenterArea();
        createBottomArea();
    }

    public void render(GameSnapshot snapshot) {
        lastSnapshot = snapshot;
        playerHandView.setCards(snapshot.playerHand());
        setCurrentTrick(snapshot.currentTrick());
        setSkat(snapshot.skat(), snapshot.skatVisible(), snapshot.phase());
        setOpponentCardCounts(snapshot.topOpponentCardCount(), snapshot.leftOpponentCardCount());
        setBidInfo(snapshot);
        dealLabel.setText("Rozdanie: " + snapshot.dealNumber() + "/" + snapshot.totalDeals()
                + " | Twoja pozycja: " + snapshot.playerPositionName());
        positionsLabel.setText(snapshot.positionSummary());
        contractLabel.setText("Gra: " + snapshot.contractName());
        statusLabel.setText(snapshot.status());
        collectedCardsLabel.setText("Karty zebrane przez gracza: " + snapshot.collectedCardCount());

        boolean bidding = snapshot.phase() == GamePhase.BIDDING;
        boolean contractSelection = snapshot.phase() == GamePhase.CONTRACT_SELECTION;
        boolean skatExchange = snapshot.phase() == GamePhase.SKAT_EXCHANGE;
        boolean playing = snapshot.phase() == GamePhase.PLAYING;

        contractControls.setVisible(contractSelection);
        contractControls.setManaged(contractSelection);

        bidButton.setText(bidding ? "Licytuj " + snapshot.nextBid() : "Licytacja zakończona");
        bidButton.setDisable(!snapshot.canBid() || snapshot.nextBid() == 0 || snapshot.finished());

        updateContractControls(snapshot);
        takeSkatButton.setDisable(!snapshot.canTakeSkatBeforeContract() || snapshot.finished());
        chooseGameWithoutSkatButton.setDisable(!snapshot.canChooseGameWithoutSkat() || snapshot.finished());
        confirmContractButton.setDisable(!snapshot.canConfirmContract() || snapshot.finished());

        discardCardButton.setText(skatExchange ? "Odłóż kartę (zostało: " + snapshot.cardsToDiscard() + ")" : "Odłóż kartę");
        discardCardButton.setDisable(!snapshot.canDiscard() || snapshot.finished() || snapshot.playerHand().isEmpty());

        playCardButton.setDisable(!snapshot.canPlay() || snapshot.finished() || snapshot.playerHand().isEmpty());
        passButton.setText(bidding ? "Pas" : "Poddaj rozdanie");
        passButton.setDisable(!snapshot.canPass() || snapshot.finished());
        newDealButton.setDisable(!snapshot.canNewDeal());
    }

    public void setOnPlayCard(Consumer<Karta> onPlayCard) {
        this.onPlayCard = onPlayCard;
    }

    public void setOnDiscardCard(Consumer<Karta> onDiscardCard) {
        this.onDiscardCard = onDiscardCard;
    }

    public void setOnConfirmContract(Consumer<GameContract> onConfirmContract) {
        this.onConfirmContract = onConfirmContract;
    }

    public void setOnTakeSkat(Runnable onTakeSkat) {
        this.onTakeSkat = onTakeSkat;
    }

    public void setOnChooseGameWithoutSkat(Runnable onChooseGameWithoutSkat) {
        this.onChooseGameWithoutSkat = onChooseGameWithoutSkat;
    }

    public void setOnBid(Runnable onBid) {
        this.onBid = onBid;
    }

    public void setOnPass(Runnable onPass) {
        this.onPass = onPass;
    }

    public void setOnNewDeal(Runnable onNewDeal) {
        this.onNewDeal = onNewDeal;
    }

    private void configureContractControls() {
        gameTypeComboBox.getItems().setAll(TypGry.KOLOROWA, TypGry.GRAND, TypGry.NULL);
        gameTypeComboBox.setValue(TypGry.GRAND);
        gameTypeComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(TypGry type) {
                if (type == null) {
                    return "";
                }
                return switch (type) {
                    case KOLOROWA -> "Kolor";
                    case GRAND -> "Grand";
                    case NULL -> "Null";
                };
            }

            @Override
            public TypGry fromString(String string) {
                return switch (string) {
                    case "Kolor" -> TypGry.KOLOROWA;
                    case "Null" -> TypGry.NULL;
                    default -> TypGry.GRAND;
                };
            }
        });

        colorComboBox.getItems().setAll(Kolor.TREFL, Kolor.PIK, Kolor.SERCE, Kolor.DZWONEK);
        colorComboBox.setValue(Kolor.TREFL);
        colorComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Kolor color) {
                return color == null ? "" : CardFormatter.formatSuit(color);
            }

            @Override
            public Kolor fromString(String string) {
                return switch (string) {
                    case "pik" -> Kolor.PIK;
                    case "serce" -> Kolor.SERCE;
                    case "dzwonek" -> Kolor.DZWONEK;
                    default -> Kolor.TREFL;
                };
            }
        });

        gameTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            colorComboBox.setDisable(newValue != TypGry.KOLOROWA || gameTypeComboBox.isDisabled());
            if (newValue == TypGry.NULL) {
                schneiderCheckBox.setSelected(false);
                schwarzCheckBox.setSelected(false);
            } else if (ouvertCheckBox.isDisabled()) {
                ouvertCheckBox.setSelected(false);
            }
            if (lastSnapshot != null) {
                updateContractControls(lastSnapshot);
            }
        });
    }

    private void createTopArea() {
        VBox topArea = new VBox(8);
        topArea.setAlignment(Pos.CENTER);
        topArea.setPadding(new Insets(12));

        topOpponentLabel.getStyleClass().add("opponent-label");
        dealLabel.getStyleClass().add("small-info-label");
        positionsLabel.getStyleClass().add("small-info-label");
        positionsLabel.setWrapText(true);
        bidLabel.getStyleClass().add("small-info-label");
        contractLabel.getStyleClass().add("small-info-label");
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setWrapText(true);

        topArea.getChildren().addAll(topOpponentLabel, dealLabel, positionsLabel, bidLabel, contractLabel, statusLabel);
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

        contractControls.setAlignment(Pos.CENTER);
        contractControls.getChildren().addAll(
                sectionLabel("Rodzaj gry:"),
                gameTypeComboBox,
                sectionLabel("Kolor:"),
                colorComboBox,
                handCheckBox,
                schneiderCheckBox,
                schwarzCheckBox,
                ouvertCheckBox,
                confirmContractButton
        );

        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.getChildren().addAll(
                bidButton,
                takeSkatButton,
                chooseGameWithoutSkatButton,
                discardCardButton,
                playCardButton,
                passButton,
                newDealButton
        );

        bidButton.setOnAction(event -> {
            if (onBid != null) {
                onBid.run();
            }
        });

        takeSkatButton.setOnAction(event -> {
            if (onTakeSkat != null) {
                onTakeSkat.run();
            }
        });

        chooseGameWithoutSkatButton.setOnAction(event -> {
            if (onChooseGameWithoutSkat != null) {
                onChooseGameWithoutSkat.run();
            }
        });

        confirmContractButton.setOnAction(event -> {
            if (onConfirmContract != null) {
                onConfirmContract.accept(selectedContract());
            }
        });

        discardCardButton.setOnAction(event -> playerHandView.selectedCard().ifPresentOrElse(
                card -> {
                    if (onDiscardCard != null) {
                        onDiscardCard.accept(card);
                    }
                },
                () -> statusLabel.setText("Najpierw wybierz kartę do odłożenia.")
        ));

        playCardButton.setOnAction(event -> playerHandView.selectedCard().ifPresentOrElse(
                card -> {
                    if (onPlayCard != null) {
                        onPlayCard.accept(card);
                    }
                },
                () -> statusLabel.setText("Najpierw wybierz kartę z ręki.")
        ));

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

        bottomArea.getChildren().addAll(playerHandView, contractControls, actionButtons);
        setBottom(bottomArea);
    }

    private GameContract selectedContract() {
        return new GameContract(
                gameTypeComboBox.getValue(),
                colorComboBox.getValue(),
                handCheckBox.isSelected(),
                schneiderCheckBox.isSelected(),
                schwarzCheckBox.isSelected(),
                ouvertCheckBox.isSelected()
        );
    }

    private void updateContractControls(GameSnapshot snapshot) {
        boolean disabled = !snapshot.canConfirmContract() || snapshot.finished();
        boolean restrictedAfterSkat = snapshot.declarationsRestrictedAfterSkat();
        boolean handRequired = snapshot.handRequired();
        TypGry selectedType = gameTypeComboBox.getValue();

        if (restrictedAfterSkat) {
            handCheckBox.setSelected(false);
            schneiderCheckBox.setSelected(false);
            schwarzCheckBox.setSelected(false);
            if (selectedType != TypGry.NULL) {
                ouvertCheckBox.setSelected(false);
            }
        }

        if (handRequired) {
            handCheckBox.setSelected(true);
        }

        gameTypeComboBox.setDisable(disabled);
        colorComboBox.setDisable(disabled || selectedType != TypGry.KOLOROWA);
        handCheckBox.setDisable(disabled || restrictedAfterSkat || handRequired);
        schneiderCheckBox.setDisable(disabled || restrictedAfterSkat || selectedType == TypGry.NULL);
        schwarzCheckBox.setDisable(disabled || restrictedAfterSkat || selectedType == TypGry.NULL);
        ouvertCheckBox.setDisable(disabled || (restrictedAfterSkat && selectedType != TypGry.NULL));
    }

    private void setCurrentTrick(List<Karta> cardsOnTable) {
        currentTrickBox.getChildren().clear();
        for (Karta card : cardsOnTable) {
            currentTrickBox.getChildren().add(new CardView(card));
        }
    }

    private void setSkat(List<Karta> skatCards, boolean visible, GamePhase phase) {
        skatBox.getChildren().clear();

        if (visible) {
            for (Karta card : skatCards) {
                skatBox.getChildren().add(new CardView(card));
            }
            if (skatCards.isEmpty()) {
                String labelText = phase == GamePhase.SKAT_EXCHANGE ? "Odłóż 2 karty" : "Skat w ręce";
                Label emptyLabel = new Label(labelText);
                emptyLabel.getStyleClass().add("small-info-label");
                skatBox.getChildren().add(emptyLabel);
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
