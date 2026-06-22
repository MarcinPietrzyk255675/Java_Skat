package com.example.java_skat.game;

import pl.skat.core.Gracz;
import pl.skat.core.Karta;
import pl.skat.core.Kolor;
import pl.skat.core.RodzajGry;
import pl.skat.core.Rozdanie;
import pl.skat.core.Skat;
import pl.skat.core.TypGry;
import pl.skat.core.WynikGry;

import java.util.ArrayList;
import java.util.List;

public final class CoreSkat {
        private CoreSkat() {
        }

        public static RodzajGry colorGame(Kolor trumpColor, boolean hand) {
                RodzajGry game = new RodzajGry();
                game.typ = TypGry.KOLOROWA;
                game.kolor = trumpColor;
                game.hand = hand;
                return game;
        }

        public static Skat skatFrom(List<Karta> cards) {
                if (cards.size() != 2) {
                        throw new IllegalArgumentException("Skat must contain exactly 2 cards");
                }

                Skat skat = new Skat();
                skat.ustawKarta1(cards.get(0));
                skat.ustawKarta2(cards.get(1));
                return skat;
        }

        public static Gracz declarerFrom(List<Karta> cardsForScore, List<Karta> wonCards) {
                Gracz declarer = new Gracz();
                declarer.ustawPosiadaneKarty(new ArrayList<>(cardsForScore));
                declarer.ustawZebraneKarty(new ArrayList<>(wonCards));
                return declarer;
        }

        public static Rozdanie scoringDeal(DealState dealState) {
                if (dealState.getDeclarerStartingHand().size() != 10) {
                        throw new IllegalStateException("Declarer must have exactly 10 cards for scoring");
                }

                if (dealState.getSkat().size() != 2) {
                        throw new IllegalStateException("Skat must contain exactly 2 cards for scoring");
                }

                Gracz declarer = declarerFrom(
                        dealState.getDeclarerStartingHand(),
                        dealState.getWonCards(dealState.getDeclarer())
                );

                Rozdanie deal = new Rozdanie(declarer, new Gracz(), new Gracz());
                deal.ustawRodzajGry(dealState.getRodzajGry());
                deal.ustawWartoscLicytacji(dealState.getCurrentBid());
                deal.ustawSkat(skatFrom(dealState.getSkat()));

                return deal;
        }

        public static WynikGry calculateResult(DealState dealState) {
                return scoringDeal(dealState).obliczWynik();
        }

        public static int calculateGameValue(DealState dealState) {
                return scoringDeal(dealState).obliczWartoscGry();
        }

        public static int baseValueForColor(Kolor color) {
                Rozdanie deal = new Rozdanie(new Gracz(), new Gracz(), new Gracz());
                deal.ustawRodzajGry(colorGame(color, false));
                return deal.obliczWartoscBazowaGry();
        }

        public static int cardPoints(Karta card) {
                return card.figura().getKod();
        }
}