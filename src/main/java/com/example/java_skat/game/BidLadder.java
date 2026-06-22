package com.example.java_skat.game;

import pl.skat.core.Kolor;

import java.util.OptionalInt;
import java.util.TreeSet;

public final class BidLadder {
	private static final int MIN_MULTIPLIER = 2;
	private static final int MAX_MULTIPLIER = 18;

	private static final int[] BIDS = createBids();

	private BidLadder() {
	}

	private static int[] createBids() {
		TreeSet<Integer> bids = new TreeSet<>();

		for (Kolor color : Kolor.values()) {
			int baseValue = CoreSkat.baseValueForColor(color);

			for (int multiplier = MIN_MULTIPLIER; multiplier <= MAX_MULTIPLIER; multiplier++) {
				bids.add(baseValue * multiplier);
			}
		}

		return bids.stream().mapToInt(Integer::intValue).toArray();
	}

	public static OptionalInt nextAfter(int currentBid) {
		for (int bid : BIDS) {
			if (bid > currentBid) {
				return OptionalInt.of(bid);
			}
		}

		return OptionalInt.empty();
	}

	public static int[] getBids() {
		return BIDS.clone();
	}
}