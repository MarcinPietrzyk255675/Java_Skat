package com.example.java_skat.ui;

import com.example.java_skat.game.GameController;

import com.example.java_skat.game.GamePhase;
import com.example.java_skat.game.PlayerId;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import pl.skat.core.Karta;


public class GameView extends BorderPane {
	private GameController gameController;
	private final HBox playerHandBox = new HBox(8);

	private final HBox biddingButtonsBox = new HBox(8);

	private final Button bidButton = new Button("Licytuj");
	private final Button acceptButton = new Button("Tak");
	private final Button askerPassButton = new Button("Pas pytającego");
	private final Button responderPassButton = new Button("Pas odpowiadającego");

	private final Button takeSkatButton = new Button("Weź skat");

	private Label title = new Label("Java Skat");
	private Label statusLabel = new Label("Kliknij \"Start\" aby rozpocząć");
	private Label dealLabel = new Label("Rozdanie: 1");
	private Label phaseLabel = new Label("Faza: Rozdawanie kart");
	private Label biddingLabel = new Label("Licytacja: -");
	private Label biddingPlayers = new Label("Pytający: -\tOdpowiadający: -");


	public GameView() {
		createLayout();
	}

	private void createLayout() {
		Button startButton = new Button("Start");
		startButton.setOnAction(event -> startNewDeal());

		bidButton.setOnAction(event -> currentAskerBid());
		acceptButton.setOnAction(event -> currentResponderAccept());
		responderPassButton.setOnAction(event -> currentResponderPass());
		askerPassButton.setOnAction(event -> currentAskerPass());

		takeSkatButton.setOnAction(event -> currentDeclarerTakeSkat());

		biddingButtonsBox.setAlignment(Pos.CENTER);
		biddingButtonsBox.getChildren().addAll(bidButton, acceptButton, responderPassButton, askerPassButton);

		VBox centerBox = new VBox(12);
		centerBox.setAlignment(Pos.CENTER);

		centerBox.getChildren().addAll(startButton, title, statusLabel, dealLabel, phaseLabel, biddingLabel,
		                               biddingPlayers, playerHandBox, biddingButtonsBox, takeSkatButton);

		playerHandBox.setAlignment(Pos.CENTER);

		setPadding(new Insets(12));
		setCenter(centerBox);

	}

	private void startNewDeal() {
		gameController = new GameController();
		gameController.dealCards();
		refreshView();
	}

	private void refreshView() {
		if (gameController == null || gameController.getDealState() == null) {
			statusLabel.setText("Gra nie została jeszcze rozpoczęta");
			dealLabel.setText("Rozdanie: -");
			phaseLabel.setText("Faza: -");
			biddingLabel.setText("Licytacja: -");
			refreshBiddingButtons();
			refreshTakeSkatButton();
			return;
		}
		statusLabel.setText("Gra rozpoczęta");
		dealLabel.setText("Rozdanie: " + gameController.getDealNumber() + " / " + GameController.getMaxDeals());
		phaseLabel.setText("Faza: " + gameController.getDealState().getPhase());
		biddingPlayers.setText(
				"Pytający: " + gameController.getDealState().getBiddingAsker().getDisplayName() + "\tOdpowiadający: " +
				gameController.getDealState().getBiddingResponder().getDisplayName());
		String highestBidderText = gameController.getDealState().getHighestBidder() == null ? "-" :
		                           gameController.getDealState().getHighestBidder().getDisplayName();

		biddingLabel.setText("Licytacja: " + gameController.getDealState().getBiddingStatus() + ", aktualna stawka: " +
		                     gameController.getDealState().getCurrentBid() + ", prowadzi: " + highestBidderText);

		refreshPlayerHand();
		refreshBiddingButtons();
		refreshTakeSkatButton();
	}

	private void refreshPlayerHand() {
		playerHandBox.getChildren().clear();

		if (gameController == null || gameController.getDealState() == null) {
			return;
		}

		for (Karta card : gameController.getDealState().getHand(PlayerId.PLAYER_1)) {
			Button cardButton = new Button(card.figura() + "\n" + card.kolor());
			cardButton.setMinSize(80, 90);
			playerHandBox.getChildren().add(cardButton);
		}
	}

	private void currentAskerBid() {
		try {
			gameController.bid(gameController.getDealState().getBiddingAsker());
			refreshView();
		} catch (RuntimeException e) {
			statusLabel.setText(e.getMessage());
		}
	}

	private void currentResponderAccept() {
		try {
			gameController.acceptBid(gameController.getDealState().getBiddingResponder());
			refreshView();
		} catch (RuntimeException e) {
			statusLabel.setText(e.getMessage());
		}
	}

	private void currentAskerPass() {
		try {
			gameController.pass(gameController.getDealState().getBiddingAsker());
			refreshView();
		} catch (RuntimeException e) {
			statusLabel.setText(e.getMessage());
		}
	}

	private void currentResponderPass() {
		try {
			gameController.pass(gameController.getDealState().getBiddingResponder());
			refreshView();
		} catch (RuntimeException e) {
			statusLabel.setText(e.getMessage());
		}
	}

	private void refreshBiddingButtons() {
		boolean bidding = gameController != null && gameController.getDealState() != null &&
		                  gameController.getDealState().getPhase() == GamePhase.BIDDING;
		bidButton.setDisable(!bidding);
		acceptButton.setDisable(!bidding || gameController.getDealState().getCurrentBid() == 0);
		askerPassButton.setDisable(!bidding);
		responderPassButton.setDisable(!bidding);
	}

	private void currentDeclarerTakeSkat() {
		try {
			gameController.takeSkat(gameController.getDealState().getDeclarer());
			refreshView();
		} catch (RuntimeException e) {
			statusLabel.setText(e.getMessage());
		}
	}

	private void refreshTakeSkatButton() {
		boolean takingSkat = gameController != null && gameController.getDealState() != null &&
		                     gameController.getDealState().getPhase() == GamePhase.TAKING_SKAT;

		takeSkatButton.setDisable(!takingSkat);
	}
}
