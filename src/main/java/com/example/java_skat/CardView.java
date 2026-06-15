package com.example.java_skat;//package pl.twojprojekt.skat.ui.view;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
//import pl.twojprojekt.skat.model.Karta;

import pl.skat.core.*;

public class CardView extends StackPane {

    private final Karta karta;
    private boolean selected = false;

    public CardView(Karta karta) {
        this.karta = karta;

        setPrefSize(70, 105);
        setMinSize(70, 105);
        setMaxSize(70, 105);

        getStyleClass().add("card");

        Label label = new Label(formatCard(karta));
        label.getStyleClass().add("card-label");

        setAlignment(Pos.CENTER);
        getChildren().add(label);

        setOnMouseClicked(event -> toggleSelected());
    }

    public Karta getKarta() {
        return karta;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;

        if (selected) {
            if (!getStyleClass().contains("card-selected")) {
                getStyleClass().add("card-selected");
            }
        } else {
            getStyleClass().remove("card-selected");
        }
    }

    public boolean isSelected() {
        return selected;
    }

    private void toggleSelected() {
        setSelected(!selected);
    }

    private String formatCard(Karta karta) {
        return karta.figura() + "\n" + karta.kolor();
    }
}