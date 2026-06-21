package com.example.java_skat.game;

import pl.skat.core.Karta;
import pl.skat.core.RodzajGry;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class DealState {
	private final Map<PlayerId, List<Karta>> hands = new EnumMap<>(PlayerId.class);
	private final Map<PlayerId, PlayerPosition> positions = new EnumMap<>(PlayerId.class);

	private final List<Karta> skat = new ArrayList<>();

	private BiddingStatus biddingStatus = BiddingStatus.NOT_STARTED;
	private PlayerId biddingAsker;
	private PlayerId biddingResponder;
	private PlayerId highestBidder;
	private int currentBid = 0;
	private boolean rearhandJoinedBidding = false;
	private GamePhase phase = GamePhase.BIDDING;
	private PlayerId declarer;
	private RodzajGry rodzajGry = new RodzajGry();


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

	public BiddingStatus getBiddingStatus() {
		return biddingStatus;
	}

	public void setBiddingStatus(BiddingStatus biddingStatus) {
		this.biddingStatus = biddingStatus;
	}

	public PlayerId getBiddingAsker() {
		return biddingAsker;
	}

	public void setBiddingAsker(PlayerId biddingAsker) {
		this.biddingAsker = biddingAsker;
	}

	public PlayerId getBiddingResponder() {
		return biddingResponder;
	}

	public void setBiddingResponder(PlayerId biddingResponder) {
		this.biddingResponder = biddingResponder;
	}

	public PlayerId getHighestBidder() {
		return highestBidder;
	}

	public void setHighestBidder(PlayerId highestBidder) {
		this.highestBidder = highestBidder;
	}

	public int getCurrentBid() {
		return currentBid;
	}

	public void setCurrentBid(int currentBid) {
		this.currentBid = currentBid;
	}

	public boolean isRearhandJoinedBidding() {
		return rearhandJoinedBidding;
	}

	public void setRearhandJoinedBidding(boolean rearhandJoinedBidding) {
		this.rearhandJoinedBidding = rearhandJoinedBidding;
	}

	public GamePhase getPhase() {
		return phase;
	}

	public void setPhase(GamePhase phase) {
		this.phase = phase;
	}

	public PlayerId getDeclarer() {
		return declarer;
	}

	public void setDeclarer(PlayerId declarer) {
		this.declarer = declarer;
	}

	public RodzajGry getRodzajGry() {
		return rodzajGry;
	}

	public void setRodzajGry(RodzajGry rodzajGry) {
		this.rodzajGry = rodzajGry;
	}
}
