package com.example.java_skat.game;

import pl.skat.core.Figura;
import pl.skat.core.Karta;

import java.util.List;

public final class BiddingStrength {
    private BiddingStrength() {
    }

    public static int estimateMaximumBid(List<Karta> hand) {
        int points = hand.stream()
                .mapToInt(card -> card.figura().getKod())
                .sum();
        long jacks = hand.stream()
                .filter(card -> card.figura() == Figura.JOPEK)
                .count();
        long aces = hand.stream()
                .filter(card -> card.figura() == Figura.AS)
                .count();

        int roughMaximum = 18
                + Math.toIntExact(jacks) * 5
                + Math.toIntExact(aces) * 3
                + points / 12;

        return BidLadder.highestNotGreaterThan(Math.min(roughMaximum, 48));
    }
}
