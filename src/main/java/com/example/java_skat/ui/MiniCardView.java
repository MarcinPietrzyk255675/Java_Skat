package com.example.java_skat.ui;

import com.example.java_skat.game.CardFormatter;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import pl.skat.core.Karta;

import java.io.InputStream;

final class MiniCardView extends StackPane {
    private static final double WIDTH = 76;
    private static final double HEIGHT = 114;

    private MiniCardView(Karta card, boolean back) {
        setPrefSize(WIDTH, HEIGHT);
        setMinSize(WIDTH, HEIGHT);
        setMaxSize(WIDTH, HEIGHT);
        setAlignment(Pos.CENTER);
        getStyleClass().add("card");
        if (back) {
            getStyleClass().add("card-back");
        }

        String path = back ? CardImagePath.back() : CardImagePath.forCard(card);
        Image image = loadImage(path);
        if (image == null) {
            Label label = new Label(back ? "" : CardFormatter.format(card));
            label.setWrapText(true);
            label.setAlignment(Pos.CENTER);
            label.getStyleClass().add("mini-card-fallback-label");
            getChildren().add(label);
        } else {
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(WIDTH);
            imageView.setFitHeight(HEIGHT);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(false);
            getChildren().add(imageView);
        }
    }

    static MiniCardView face(Karta card) {
        return new MiniCardView(card, false);
    }

    static MiniCardView back() {
        return new MiniCardView(null, true);
    }

    private Image loadImage(String path) {
        InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            return null;
        }
        return new Image(stream);
    }
}
