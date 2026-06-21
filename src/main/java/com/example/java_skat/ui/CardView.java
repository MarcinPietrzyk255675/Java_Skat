package com.example.java_skat.ui;

import com.example.java_skat.game.CardFormatter;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import pl.skat.core.Karta;

import java.io.InputStream;

public class CardView extends StackPane {
    public static final double CARD_WIDTH = 96;
    public static final double CARD_HEIGHT = 144;

    private final Karta card;
    private boolean selected;

    public CardView(Karta card) {
        this.card = card;

        setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        setMinSize(CARD_WIDTH, CARD_HEIGHT);
        setMaxSize(CARD_WIDTH, CARD_HEIGHT);
        setAlignment(Pos.CENTER);
        getStyleClass().add("card");

        Image image = loadImage(CardImagePath.forCard(card));
        if (image == null) {
            getChildren().add(createFallbackLabel(CardFormatter.format(card)));
        } else {
            getChildren().add(createImageView(image));
        }
    }

    public Karta getCard() {
        return card;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        getStyleClass().remove("card-selected");

        if (selected) {
            getStyleClass().add("card-selected");
            setTranslateY(-10);
        } else {
            setTranslateY(0);
        }
    }

    public boolean isSelected() {
        return selected;
    }

    private Image loadImage(String path) {
        InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            return null;
        }
        return new Image(stream);
    }

    private ImageView createImageView(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(CARD_WIDTH);
        imageView.setFitHeight(CARD_HEIGHT);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(false);
        return imageView;
    }

    private Label createFallbackLabel(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        label.getStyleClass().add("card-fallback-label");
        return label;
    }
}
