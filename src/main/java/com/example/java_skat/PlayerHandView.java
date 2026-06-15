package com.example.java_skat;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
//import com.example.java_skat.model.Karta;

import pl.skat.core.Karta;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class PlayerHandView extends HBox {

    private CardView selectedCardView;
    private Consumer<Karta> onCardSelected;

    public PlayerHandView() {
        setSpacing(8);
        setAlignment(Pos.CENTER);
        getStyleClass().add("player-hand");
    }

    public void setCards(List<Karta> cards) {
        getChildren().clear();
        selectedCardView = null;

        for (Karta karta : cards) {
            CardView cardView = new CardView(karta);

            cardView.setOnMouseClicked(event -> {
                selectCard(cardView);

                if (onCardSelected != null) {
                    onCardSelected.accept(karta);
                }
            });

            getChildren().add(cardView);
        }
    }

    public Optional<Karta> getSelectedCard() {
        if (selectedCardView == null) {
            return Optional.empty();
        }

        return Optional.of(selectedCardView.getKarta());
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

    private void selectCard(CardView cardView) {
        if (selectedCardView != null) {
            selectedCardView.setSelected(false);
        }

        selectedCardView = cardView;
        selectedCardView.setSelected(true);
    }
}