package com.example.java_skat.game;

import pl.skat.core.Karta;

public record PlayedCard(PlayerSeat player, Karta card) {
}
