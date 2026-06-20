package com.example.java_skat.game;

import java.util.List;
import java.util.OptionalInt;

public final class BidLadder {
    private static final List<Integer> VALUES = List.of(
            18, 20, 22, 23, 24, 27, 30, 33, 35, 36,
            40, 44, 45, 46, 48, 50, 55, 59, 60, 63,
            66, 70, 72, 77, 80, 81, 84, 88, 90, 96,
            99, 100, 108, 110, 117, 120
    );

    private BidLadder() {
    }

    public static OptionalInt nextAfter(int currentBid) {
        return VALUES.stream()
                .mapToInt(Integer::intValue)
                .filter(value -> value > currentBid)
                .findFirst();
    }

    public static int highestNotGreaterThan(int maximum) {
        int result = 0;
        for (int value : VALUES) {
            if (value > maximum) {
                break;
            }
            result = value;
        }
        return result;
    }
}
