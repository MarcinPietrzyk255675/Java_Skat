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
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
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
    private final ScrollPane playerHandScrollPane = new ScrollPane(playerHandView);
    private final StackPane trickTable = new StackPane();
    private final VBox ownTrickSlot = new VBox(2);
    private final VBox leftTrickSlot = new VBox(2);
    private final VBox rightTrickSlot = new VBox(2);
    private final HBox skatBox = new HBox(2);
    private final FlowPane rightOpponentCardsBox = new FlowPane(1, 1);
    private final FlowPane leftOpponentCardsBox = new FlowPane(1, 1);
    private final FlowPane contractControls = new FlowPane(8, 8);

    private final Label rightOpponentLabel = new Label();
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
    private final Button newDealButton = new Button("Następne rozdanie");

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
        createRightArea();
        createCenterArea();
        createBottomArea();
    }

    public void render(GameSnapshot snapshot) {
        lastSnapshot = snapshot;
        playerHandView.setCards(snapshot.playerHand());
        setCurrentTrick(snapshot);
        setSkat(snapshot.skat(), snapshot.skatVisible(), snapshot.phase());
        setOpponentCards(snapshot);
        setBidInfo(snapshot);

        dealLabel.setText("Rozdanie: " + snapshot.dealNumber() + "/" + snapshot.totalDeals()
                + " | Twoja pozycja: " + snapshot.playerPositionName());
        positionsLabel.setText(snapshot.positionSummary());
        contractLabel.setText("Gra: " + snapshot.contractName());
        statusLabel.setText(snapshot.status());
        collectedCardsLabel.setText("Zebrane karty: " + snapshot.collectedCardCount());

        boolean contractSelection = snapshot.phase() == GamePhase.CONTRACT_SELECTION;
        boolean skatExchange = snapshot.phase() == GamePhase.SKAT_EXCHANGE;

        contractControls.setVisible(contractSelection);
        contractControls.setManaged(contractSelection);

        bidButton.setText(snapshot.bidActionText());
        bidButton.setDisable(!snapshot.canBid() || snapshot.finished());

        updateContractControls(snapshot);
        takeSkatButton.setDisable(!snapshot.canTakeSkatBeforeContract() || snapshot.finished());
        chooseGameWithoutSkatButton.setDisable(!snapshot.canChooseGameWithoutSkat() || snapshot.finished());
        confirmContractButton.setDisable(!snapshot.canConfirmContract() || snapshot.finished());

        discardCardButton.setText(skatExchange ? "Odłóż kartę (zostało: " + snapshot.cardsToDiscard() + ")" : "Odłóż kartę");
        discardCardButton.setDisable(!snapshot.canDiscard() || snapshot.finished() || snapshot.playerHand().isEmpty());

        playCardButton.setDisable(!snapshot.canPlay() || snapshot.finished() || snapshot.playerHand().isEmpty());
        passButton.setText(snapshot.passActionText());
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
        VBox topArea = new VBox(4);
        topArea.setAlignment(Pos.CENTER);
        topArea.setPadding(new Insets(8, 12, 6, 12));
        topArea.getStyleClass().add("top-info-bar");

        dealLabel.getStyleClass().add("small-info-label");
        positionsLabel.getStyleClass().add("small-info-label");
        positionsLabel.setWrapText(true);
        bidLabel.getStyleClass().add("small-info-label");
        contractLabel.getStyleClass().add("small-info-label");
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(Double.MAX_VALUE);

        topArea.getChildren().addAll(dealLabel, positionsLabel, bidLabel, contractLabel, statusLabel);
        setTop(topArea);
    }

    private void createRightArea() {
        VBox rightArea = sideArea();
        rightOpponentLabel.getStyleClass().add("opponent-label");
        rightOpponentCardsBox.setAlignment(Pos.CENTER);
        rightOpponentCardsBox.setPrefWrapLength(88);
        rightArea.getChildren().addAll(rightOpponentLabel, rightOpponentCardsBox);
        setRight(rightArea);
    }

    private void createLeftArea() {
        VBox leftArea = sideArea();
        leftOpponentLabel.getStyleClass().add("opponent-label");
        leftOpponentCardsBox.setAlignment(Pos.CENTER);
        leftOpponentCardsBox.setPrefWrapLength(88);
        collectedCardsLabel.getStyleClass().add("small-info-label");
        collectedCardsLabel.setWrapText(true);
        leftArea.getChildren().addAll(leftOpponentLabel, leftOpponentCardsBox, collectedCardsLabel);
        setLeft(leftArea);
    }

    private VBox sideArea() {
        VBox area = new VBox(8);
        area.setAlignment(Pos.TOP_CENTER);
        area.setPadding(new Insets(6));
        area.setPrefWidth(104);
        area.setMinWidth(96);
        area.setMaxWidth(112);
        area.getStyleClass().add("side-player-area");
        return area;
    }

    private void createCenterArea() {
        StackPane centerArea = new StackPane();
        centerArea.setPadding(new Insets(6));
        centerArea.getStyleClass().add("table-center");
        centerArea.setMinHeight(210);
        centerArea.setPrefHeight(240);

        trickTable.setMinSize(330, 190);
        trickTable.setPrefSize(380, 210);
        trickTable.setMaxSize(430, 230);
        trickTable.getStyleClass().add("trick-table");

        configureTrickSlot(ownTrickSlot, Pos.CENTER);
        configureTrickSlot(leftTrickSlot, Pos.CENTER);
        configureTrickSlot(rightTrickSlot, Pos.CENTER);

        StackPane.setAlignment(ownTrickSlot, Pos.BOTTOM_CENTER);
        StackPane.setAlignment(leftTrickSlot, Pos.CENTER_LEFT);
        StackPane.setAlignment(rightTrickSlot, Pos.CENTER_RIGHT);
        trickTable.getChildren().addAll(leftTrickSlot, rightTrickSlot, ownTrickSlot);

        skatBox.setAlignment(Pos.TOP_RIGHT);
        skatBox.getStyleClass().add("skat-box");

        Label skatLabel = sectionLabel("Skat");
        VBox skatArea = new VBox(2, skatLabel, skatBox);
        skatArea.setAlignment(Pos.TOP_RIGHT);
        StackPane.setAlignment(skatArea, Pos.TOP_RIGHT);

        centerArea.getChildren().addAll(trickTable, skatArea);
        setCenter(centerArea);
    }

    private void configureTrickSlot(VBox slot, Pos alignment) {
        slot.setAlignment(alignment);
        slot.setMinSize(108, 150);
        slot.setPrefSize(112, 152);
        slot.setMaxSize(118, 158);
        slot.getStyleClass().add("trick-slot");
    }

    private void createBottomArea() {
        VBox bottomArea = new VBox(6);
        bottomArea.setAlignment(Pos.CENTER);
        bottomArea.setPadding(new Insets(4, 8, 8, 8));
        bottomArea.getStyleClass().add("bottom-player-area");

        playerHandScrollPane.setFitToHeight(true);
        playerHandScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        playerHandScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        playerHandScrollPane.setPannable(true);
        playerHandScrollPane.setMinHeight(CardView.CARD_HEIGHT + 18);
        playerHandScrollPane.setPrefHeight(CardView.CARD_HEIGHT + 22);
        playerHandScrollPane.setMaxHeight(CardView.CARD_HEIGHT + 30);
        VBox.setVgrow(playerHandScrollPane, Priority.NEVER);

        contractControls.setAlignment(Pos.CENTER);
        contractControls.setMaxWidth(Double.MAX_VALUE);
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

        FlowPane actionButtons = new FlowPane(6, 6);
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.setMaxWidth(Double.MAX_VALUE);
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

        bottomArea.getChildren().addAll(playerHandScrollPane, contractControls, actionButtons);
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

    private void setCurrentTrick(GameSnapshot snapshot) {
        fillTrickSlot(ownTrickSlot, snapshot.ownTrickCard(), snapshot.ownTrickLabel());
        fillTrickSlot(leftTrickSlot, snapshot.leftOpponentTrickCard(), snapshot.leftOpponentTrickLabel());
        fillTrickSlot(rightTrickSlot, snapshot.rightOpponentTrickCard(), snapshot.rightOpponentTrickLabel());
    }

    private void fillTrickSlot(VBox slot, Karta card, String labelText) {
        slot.getChildren().clear();
        if (card == null) {
            Label emptyLabel = new Label(" ");
            emptyLabel.getStyleClass().add("small-info-label");
            slot.getChildren().add(emptyLabel);
            return;
        }

        Label label = new Label(labelText == null ? "" : labelText);
        label.setWrapText(true);
        label.getStyleClass().add("trick-card-label");
        slot.getChildren().addAll(label, new CardView(card));
    }

    private void setSkat(List<Karta> skatCards, boolean visible, GamePhase phase) {
        skatBox.getChildren().clear();

        if (visible) {
            for (Karta card : skatCards) {
                skatBox.getChildren().add(MiniCardView.face(card));
            }
            if (skatCards.isEmpty()) {
                String labelText = phase == GamePhase.SKAT_EXCHANGE ? "Odłóż 2 karty" : "Skat w ręce";
                Label emptyLabel = new Label(labelText);
                emptyLabel.getStyleClass().add("small-info-label");
                skatBox.getChildren().add(emptyLabel);
            }
            return;
        }

        skatBox.getChildren().add(MiniCardView.back());
        skatBox.getChildren().add(MiniCardView.back());
    }

    private void setOpponentCards(GameSnapshot snapshot) {
        rightOpponentLabel.setText(snapshot.topOpponentName() + ": " + snapshot.topOpponentCardCount()
                + " kart" + (snapshot.topOpponentCardsVisible() ? " (otwarte)" : ""));
        leftOpponentLabel.setText(snapshot.leftOpponentName() + ": " + snapshot.leftOpponentCardCount()
                + " kart" + (snapshot.leftOpponentCardsVisible() ? " (otwarte)" : ""));

        fillMiniCards(rightOpponentCardsBox, snapshot.topOpponentHand(), snapshot.topOpponentCardCount(),
                snapshot.topOpponentCardsVisible());
        fillMiniCards(leftOpponentCardsBox, snapshot.leftOpponentHand(), snapshot.leftOpponentCardCount(),
                snapshot.leftOpponentCardsVisible());
    }

    private void fillMiniCards(javafx.scene.layout.Pane container, List<Karta> visibleCards, int hiddenCount, boolean visible) {
        container.getChildren().clear();
        if (visible) {
            for (Karta card : visibleCards) {
                container.getChildren().add(MiniCardView.face(card));
            }
            return;
        }

        for (int i = 0; i < hiddenCount; i++) {
            container.getChildren().add(MiniCardView.back());
        }
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
