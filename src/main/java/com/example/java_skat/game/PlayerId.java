package com.example.java_skat.game;

public enum PlayerId {
	PLAYER_1("Gracz 1"),
	PLAYER_2("Gracz 2"),
	PLAYER_3("Gracz 3");

	private final String displayName;

	PlayerId(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
