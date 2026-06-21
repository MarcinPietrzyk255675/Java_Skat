package com.example.java_skat.game;

import pl.skat.core.Karta;

public record PlayerCard(PlayerId playerId, Karta card) {
}
