package com.example.java_skat.game;

public enum PlayerPosition {
	FOREHAND("Przodek"),
	MIDDLEHAND("Środek"),
	REARHAND("Zadek");

	private final String displayName;

	PlayerPosition(String displayName) {
		this.displayName = displayName;
	}

	public String displayName() {
		return displayName;
	}
}
