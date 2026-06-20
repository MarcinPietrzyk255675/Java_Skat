package com.example.java_skat.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.io.InputStream;

final class CardBackView extends StackPane {
    CardBackView() {
        setPrefSize(CardView.CARD_WIDTH, CardView.CARD_HEIGHT);
        setMinSize(CardView.CARD_WIDTH, CardView.CARD_HEIGHT);
        setMaxSize(CardView.CARD_WIDTH, CardView.CARD_HEIGHT);
        setAlignment(Pos.CENTER);
        getStyleClass().addAll("card", "card-back");

        Image image = loadImage(CardImagePath.back());
        if (image == null) {
            Label label = new Label("SKAT");
            label.getStyleClass().add("card-fallback-label");
            getChildren().add(label);
        } else {
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(CardView.CARD_WIDTH);
            imageView.setFitHeight(CardView.CARD_HEIGHT);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(false);
            getChildren().add(imageView);
        }
    }

    private Image loadImage(String path) {
        InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            return null;
        }
        return new Image(stream);
    }
}
