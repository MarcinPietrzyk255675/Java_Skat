package com.example.java_skat.game;

import pl.skat.core.Karta;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class DealState {
	private final Map<PlayerId, List<Karta>> hands = new EnumMap<>(PlayerId.class);
	private final Map<PlayerId, PlayerPosition> positions = new EnumMap<>(PlayerId.class);

	private final List<Karta> skat = new ArrayList<>();

	public DealState() {
		for (PlayerId player : PlayerId.values()) {
			hands.put(player, new ArrayList<>());
		}
	}

	public List<Karta> getHand(PlayerId player) {
		return hands.get(player);
	}

	public List<Karta> getSkat() {
		return skat;
	}

	public PlayerPosition getPosition(PlayerId player) {
		return positions.get(player);
	}

	public void setPosition(PlayerId player, PlayerPosition position) {
		positions.put(player, position);
	}

}
