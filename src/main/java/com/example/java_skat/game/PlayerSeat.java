package com.example.java_skat.game;

public enum PlayerSeat {
    DECLARER("Gracz"),
    OPPONENT_ONE("Przeciwnik 1"),
    OPPONENT_TWO("Przeciwnik 2");

    private final String displayName;

    PlayerSeat(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public PlayerSeat nextClockwise() {
        return switch (this) {
            case DECLARER -> OPPONENT_ONE;
            case OPPONENT_ONE -> OPPONENT_TWO;
            case OPPONENT_TWO -> DECLARER;
        };
    }
}
