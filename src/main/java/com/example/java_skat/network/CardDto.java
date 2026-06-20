package com.example.java_skat.network;

import pl.skat.core.Figura;
import pl.skat.core.Karta;
import pl.skat.core.Kolor;

import java.io.Serializable;

public record CardDto(String kolor, String figura) implements Serializable {
    public static CardDto fromCore(Karta card) {
        return new CardDto(card.kolor().name(), card.figura().name());
    }

    public Karta toCore() {
        return new Karta(Kolor.valueOf(kolor), Figura.valueOf(figura));
    }
}
