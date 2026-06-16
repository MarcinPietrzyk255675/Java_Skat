package com.example.java_skat;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import pl.skat.core.Karta;

import java.util.Objects;

public class CardView extends StackPane {

    private static final double CARD_WIDTH = 70;
    private static final double CARD_HEIGHT = 105;

    private final Karta karta;
    private boolean selected = false;

    public CardView(Karta karta) {
        this.karta = karta;

        setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        setMinSize(CARD_WIDTH, CARD_HEIGHT);
        setMaxSize(CARD_WIDTH, CARD_HEIGHT);

        getStyleClass().add("card");
        setAlignment(Pos.CENTER);

        Image image = loadImage(CardImagePath.forCard(karta));

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(CARD_WIDTH);
        imageView.setFitHeight(CARD_HEIGHT);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(false);

        getChildren().add(imageView);
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
            setTranslateY(-15);
        } else {
            getStyleClass().remove("card-selected");
            setTranslateY(0);
        }
    }

    public boolean isSelected() {
        return selected;
    }

    private Image loadImage(String path) {
        return new Image(
                Objects.requireNonNull(
                        getClass().getResourceAsStream(path),
                        "Nie znaleziono obrazka karty: " + path
                )
        );
    }
}