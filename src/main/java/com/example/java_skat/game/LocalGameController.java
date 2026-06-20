package com.example.java_skat.game;

import pl.skat.core.Gracz;
import pl.skat.core.Karta;
import pl.skat.core.RodzajGry;
import pl.skat.core.Rozdanie;
import pl.skat.core.Skat;
import pl.skat.core.TypGry;
import pl.skat.core.WynikGry;

import java.util.ArrayList;
import java.util.List;

public class LocalGameController {
    private static final int HAND_SIZE = 10;
    private static final int SKAT_SIZE = 2;
    private static final int DEFAULT_BID = 18;

    private final Gracz declarer = new Gracz();
    private final Gracz opponentOne = new Gracz();
    private final Gracz opponentTwo = new Gracz();

    private Rozdanie round;
    private RodzajGry gameType;
    private final List<Karta> initialPlayerHand = new ArrayList<>();
    private final List<Karta> playerHand = new ArrayList<>();
    private final List<Karta> opponentOneHand = new ArrayList<>();
    private final List<Karta> opponentTwoHand = new ArrayList<>();
    private final List<Karta> skatCards = new ArrayList<>();
    private final List<Karta> currentTrick = new ArrayList<>();
    private final List<Karta> collectedCards = new ArrayList<>();

    private boolean skatVisible;
    private boolean finished;
    private String status;

    public LocalGameController() {
        startNewDeal();
    }

    public final void startNewDeal() {
        List<Karta> deck = DeckFactory.shuffledDeck();

        initialPlayerHand.clear();
        playerHand.clear();
        opponentOneHand.clear();
        opponentTwoHand.clear();
        skatCards.clear();
        currentTrick.clear();
        collectedCards.clear();

        playerHand.addAll(deck.subList(0, HAND_SIZE));
        initialPlayerHand.addAll(playerHand);
        opponentOneHand.addAll(deck.subList(HAND_SIZE, HAND_SIZE * 2));
        opponentTwoHand.addAll(deck.subList(HAND_SIZE * 2, HAND_SIZE * 3));
        skatCards.addAll(deck.subList(HAND_SIZE * 3, HAND_SIZE * 3 + SKAT_SIZE));

        skatVisible = false;
        finished = false;
        status = "Nowe rozdanie. Wybierz kartę i zagraj.";

        configureCoreRound();
    }

    public void playCard(Karta card) {
        if (finished) {
            status = "Rozdanie jest zakończone. Kliknij \"Nowe rozdanie\".";
            return;
        }

        if (currentTrick.size() == 3) {
            collectCurrentTrickForDemo();
        }

        if (!playerHand.contains(card)) {
            status = "Nie możesz zagrać tej karty, bo nie ma jej już w ręce.";
            return;
        }

        if (!TrickRules.canPlay(card, playerHand, currentTrick, gameType)) {
            status = TrickRules.illegalMoveMessage(card, playerHand, currentTrick, gameType);
            return;
        }

        playerHand.remove(card);
        currentTrick.add(card);
        playAutomaticOpponentCard(opponentOneHand);
        playAutomaticOpponentCard(opponentTwoHand);

        status = "Zagrano: " + CardFormatter.format(card) + ". Przeciwnicy wykonali ruch automatyczny.";

        if (playerHand.isEmpty()) {
            collectCurrentTrickForDemo();
            finishRound();
        }
    }

    public void showSkat() {
        if (finished) {
            status = "Rozdanie jest zakończone.";
            return;
        }

        skatVisible = true;
        status = "Skat odkryty. W tym prototypie nie dokładam go do ręki, bo skat-core wymaga 10 kart początkowych gracza.";
    }

    public void pass() {
        if (finished) {
            return;
        }

        finished = true;
        status = "Spasowałeś. Kliknij \"Nowe rozdanie\", aby zacząć od nowa.";
    }

    public GameSnapshot snapshot() {
        return new GameSnapshot(
                List.copyOf(playerHand),
                List.copyOf(currentTrick),
                List.copyOf(skatCards),
                skatVisible,
                opponentOneHand.size(),
                opponentTwoHand.size(),
                collectedCards.size(),
                status,
                finished
        );
    }

    private void playAutomaticOpponentCard(List<Karta> opponentHand) {
        if (opponentHand.isEmpty()) {
            return;
        }

        Karta card = TrickRules.firstLegalCard(opponentHand, currentTrick, gameType)
                .orElse(opponentHand.get(0));
        opponentHand.remove(card);
        currentTrick.add(card);
    }

    private void collectCurrentTrickForDemo() {
        collectedCards.addAll(currentTrick);
        currentTrick.clear();
    }

    private void finishRound() {
        declarer.ustawZebraneKarty(new ArrayList<>(collectedCards));
        WynikGry result = round.obliczWynik();
        finished = true;

        String outcome = result.wygrana ? "wygrana" : "przegrana";
        status = "Koniec rozdania. Wynik z Projekt_PIO: " + outcome + ", wartość/oczka = " + result.wynik + ".";
    }

    private void configureCoreRound() {
        declarer.ustawPosiadaneKarty(new ArrayList<>(initialPlayerHand));

        Skat skat = new Skat();
        skat.ustawKarta1(skatCards.get(0));
        skat.ustawKarta2(skatCards.get(1));

        gameType = new RodzajGry();
        gameType.typ = TypGry.GRAND;

        round = new Rozdanie(declarer, opponentOne, opponentTwo);
        round.ustawSkat(skat);
        round.ustawRodzajGry(gameType);
        round.ustawWartoscLicytacji(DEFAULT_BID);
    }
}
