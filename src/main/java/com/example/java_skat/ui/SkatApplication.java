package com.example.java_skat.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SkatApplication extends Application {

	@Override
	public void start(Stage stage) {
		GameView gameView = new GameView();
		Scene scene = new Scene(gameView, 900, 600);
		stage.setTitle("Java Skat");
		stage.setScene(scene);
		stage.show();
	}
}
