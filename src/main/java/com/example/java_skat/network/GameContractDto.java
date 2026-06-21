package com.example.java_skat.network;

import com.example.java_skat.game.GameContract;
import pl.skat.core.Kolor;
import pl.skat.core.TypGry;

import java.io.Serializable;

public record GameContractDto(
        String type,
        String color,
        boolean hand,
        boolean schneiderAnnounced,
        boolean schwarzAnnounced,
        boolean ouvert
) implements Serializable {
    public static GameContractDto fromGameContract(GameContract contract) {
        return new GameContractDto(
                contract.type().name(),
                contract.color() == null ? Kolor.TREFL.name() : contract.color().name(),
                contract.hand(),
                contract.schneiderAnnounced(),
                contract.schwarzAnnounced(),
                contract.ouvert()
        );
    }

    public GameContract toGameContract() {
        return new GameContract(
                TypGry.valueOf(type),
                color == null ? Kolor.TREFL : Kolor.valueOf(color),
                hand,
                schneiderAnnounced,
                schwarzAnnounced,
                ouvert
        );
    }
}
