package com.example.java_skat.ui;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import pl.skat.core.Karta;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class PlayerHandView extends HBox {
    private CardView selectedCardView;
    private Consumer<Karta> onCardSelected;

    public PlayerHandView() {
        setSpacing(6);
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("player-hand");
    }

    public void setCards(List<Karta> cards) {
        getChildren().clear();
        selectedCardView = null;

        for (Karta card : cards) {
            CardView cardView = new CardView(card);
            cardView.setOnMouseClicked(event -> select(cardView));
            getChildren().add(cardView);
        }
    }

    public Optional<Karta> selectedCard() {
        if (selectedCardView == null) {
            return Optional.empty();
        }
        return Optional.of(selectedCardView.getCard());
    }

    public void clearSelection() {
        if (selectedCardView != null) {
            selectedCardView.setSelected(false);
            selectedCardView = null;
        }
    }

    public void setOnCardSelected(Consumer<Karta> onCardSelected) {
        this.onCardSelected = onCardSelected;
    }

    private void select(CardView cardView) {
        if (selectedCardView != null) {
            selectedCardView.setSelected(false);
        }

        selectedCardView = cardView;
        selectedCardView.setSelected(true);

        if (onCardSelected != null) {
            onCardSelected.accept(cardView.getCard());
        }
    }
}
