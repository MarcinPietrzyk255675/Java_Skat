package com.example.java_skat;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import pl.skat.core.Karta;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

import java.util.List;
import java.util.function.Consumer;

public class SkatTableView extends BorderPane {

	private final PlayerHandView playerHandView = new PlayerHandView();

	private final HBox currentTrickBox = new HBox(12);
	private final HBox skatBox = new HBox(8);

	private final Label topOpponentLabel = new Label("Przeciwnik 1: 10 kart");
	private final Label leftOpponentLabel = new Label("Przeciwnik 2: 10 kart");
	private final Label gameStatusLabel = new Label("Twoja tura");

	private final Button playCardButton = new Button("Zagraj kartę");
	private final Button takeSkatButton = new Button("Weź skat");
	private final Button passButton = new Button("Pas");

	private Consumer<Karta> onPlayCard;

	public SkatTableView() {
		getStyleClass().add("skat-table");

		createTopArea();
		createCenterArea();
		createBottomArea();
		createLeftArea();
	}

	private void createTopArea() {
		VBox topArea = new VBox(8);
		topArea.setAlignment(Pos.CENTER);
		topArea.setPadding(new Insets(12));

		topOpponentLabel.getStyleClass().add("opponent-label");
		gameStatusLabel.getStyleClass().add("status-label");

		topArea.getChildren().addAll(topOpponentLabel, gameStatusLabel);

		setTop(topArea);
	}

	private void createCenterArea() {
		StackPane centerArea = new StackPane();
		centerArea.setPadding(new Insets(20));
		centerArea.getStyleClass().add("table-center");

		currentTrickBox.setAlignment(Pos.CENTER);
		currentTrickBox.getStyleClass().add("current-trick");

		skatBox.setAlignment(Pos.TOP_RIGHT);
		skatBox.getStyleClass().add("skat-box");

		Label trickLabel = new Label("Aktualna lewa");
		trickLabel.getStyleClass().add("section-label");

		VBox trickArea = new VBox(10, trickLabel, currentTrickBox);
		trickArea.setAlignment(Pos.CENTER);

		VBox skatArea = new VBox(6, new Label("Skat"), skatBox);
		skatArea.setAlignment(Pos.TOP_RIGHT);
		StackPane.setAlignment(skatArea, Pos.TOP_RIGHT);

		centerArea.getChildren().addAll(trickArea, skatArea);

		setCenter(centerArea);
	}

	private void createBottomArea() {
		VBox bottomArea = new VBox(12);
		bottomArea.setAlignment(Pos.CENTER);
		bottomArea.setPadding(new Insets(12));

		HBox actionButtons = new HBox(10);
		actionButtons.setAlignment(Pos.CENTER);
		actionButtons.getChildren().addAll(playCardButton, takeSkatButton, passButton);

		playCardButton.setOnAction(event -> {
			playerHandView.getSelectedCard().ifPresent(karta -> {
				if (onPlayCard != null) {
					onPlayCard.accept(karta);
				}
			});
		});

		bottomArea.getChildren().addAll(playerHandView, actionButtons);

		setBottom(bottomArea);
	}

	private void createLeftArea() {
		VBox leftArea = new VBox();
		leftArea.setAlignment(Pos.CENTER);
		leftArea.setPadding(new Insets(12));

		leftOpponentLabel.getStyleClass().add("opponent-label");

		leftArea.getChildren().add(leftOpponentLabel);

		setLeft(leftArea);
	}

	public void setPlayerHand(List<Karta> cards) {
		playerHandView.setCards(cards);
	}

	public void setCurrentTrick(List<Karta> cardsOnTable) {
		currentTrickBox.getChildren().clear();

		for (Karta karta : cardsOnTable) {
			currentTrickBox.getChildren().add(new CardView(karta));
		}
	}

	public void setSkatHidden() {
		skatBox.getChildren().clear();

		skatBox.getChildren().add(createHiddenCard());
		skatBox.getChildren().add(createHiddenCard());
	}

	public void setSkatVisible(List<Karta> skatCards) {
		skatBox.getChildren().clear();

		for (Karta karta : skatCards) {
			skatBox.getChildren().add(new CardView(karta));
		}
	}

	public void setOpponentCardCounts(int topOpponentCards, int leftOpponentCards) {
		topOpponentLabel.setText("Przeciwnik 1: " + topOpponentCards + " kart");
		leftOpponentLabel.setText("Przeciwnik 2: " + leftOpponentCards + " kart");
	}

	public void setGameStatus(String status) {
		gameStatusLabel.setText(status);
	}

	public void setOnPlayCard(Consumer<Karta> onPlayCard) {
		this.onPlayCard = onPlayCard;
	}

	private StackPane createHiddenCard() {
		StackPane cardBack = new StackPane();
		cardBack.setPrefSize(70, 105);
		cardBack.setMinSize(70, 105);
		cardBack.setMaxSize(70, 105);
		cardBack.getStyleClass().add("card-back");

		Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(CardImagePath.back()),
		                                               "Nie znaleziono obrazka rewersu karty: " +
		                                               CardImagePath.back()));

		ImageView imageView = new ImageView(image);
		imageView.setFitWidth(70);
		imageView.setFitHeight(105);
		imageView.setPreserveRatio(true);
		imageView.setSmooth(false);

		cardBack.getChildren().add(imageView);

		return cardBack;
	}


}