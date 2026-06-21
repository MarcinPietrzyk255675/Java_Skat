package com.example.java_skat.game;

public enum HandPosition {
    FOREHAND("przodek"),
    MIDDLEHAND("środek"),
    REARHAND("zadek");

    private final String displayName;

    HandPosition(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
