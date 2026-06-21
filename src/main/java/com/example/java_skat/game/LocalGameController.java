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
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.OptionalInt;

public class LocalGameController {
    private static final int HAND_SIZE = 10;
    private static final int SKAT_SIZE = 2;

    private final Gracz declarer = new Gracz();
    private final Gracz opponentOne = new Gracz();
    private final Gracz opponentTwo = new Gracz();

    private Rozdanie round;
    private GameContract contract;
    private RodzajGry gameType;
    private final List<Karta> playerHand = new ArrayList<>();
    private final List<Karta> opponentOneHand = new ArrayList<>();
    private final List<Karta> opponentTwoHand = new ArrayList<>();
    private final List<Karta> skatCards = new ArrayList<>();
    private final List<PlayedCard> currentTrick = new ArrayList<>();
    private final List<Karta> declarerCollectedCards = new ArrayList<>();
    private final List<Karta> opponentsCollectedCards = new ArrayList<>();
    private final EnumSet<PlayerSeat> passedBidders = EnumSet.noneOf(PlayerSeat.class);
    private final EnumMap<PlayerSeat, Integer> opponentMaximumBids = new EnumMap<>(PlayerSeat.class);

    private GamePhase phase;
    private PlayerSeat nextPlayer;
    private PlayerSeat highestBidder;
    private int currentBid;
    private int declarerWonTricks;
    private int opponentsWonTricks;
    private boolean skatVisible;
    private boolean skatTakenBeforeContract;
    private boolean finished;
    private String lastTrickSummary;
    private String status;

    public LocalGameController() {
        startNewDeal();
    }

    public final void startNewDeal() {
        List<Karta> deck = DeckFactory.shuffledDeck();

        playerHand.clear();
        opponentOneHand.clear();
        opponentTwoHand.clear();
        skatCards.clear();
        currentTrick.clear();
        declarerCollectedCards.clear();
        opponentsCollectedCards.clear();
        passedBidders.clear();
        opponentMaximumBids.clear();

        playerHand.addAll(deck.subList(0, HAND_SIZE));
        opponentOneHand.addAll(deck.subList(HAND_SIZE, HAND_SIZE * 2));
        opponentTwoHand.addAll(deck.subList(HAND_SIZE * 2, HAND_SIZE * 3));
        skatCards.addAll(deck.subList(HAND_SIZE * 3, HAND_SIZE * 3 + SKAT_SIZE));

        contract = null;
        gameType = GameContract.defaultGrand().toCore();
        sortHands();

        opponentMaximumBids.put(PlayerSeat.OPPONENT_ONE, BiddingStrength.estimateMaximumBid(opponentOneHand));
        opponentMaximumBids.put(PlayerSeat.OPPONENT_TWO, BiddingStrength.estimateMaximumBid(opponentTwoHand));

        round = null;
        phase = GamePhase.BIDDING;
        nextPlayer = PlayerSeat.DECLARER;
        highestBidder = null;
        currentBid = 0;
        declarerWonTricks = 0;
        opponentsWonTricks = 0;
        skatVisible = false;
        skatTakenBeforeContract = false;
        finished = false;
        lastTrickSummary = "";
        status = "Nowe rozdanie. Trwa licytacja. Możesz zalicytować " + nextBidText() + " albo spasować.";
    }

    public void bid() {
        if (phase != GamePhase.BIDDING) {
            status = "Licytacja jest już zakończona.";
            return;
        }

        OptionalInt nextBid = BidLadder.nextAfter(currentBid);
        if (nextBid.isEmpty()) {
            status = "Nie ma już wyższej wartości licytacji w tej wersji prototypu.";
            return;
        }

        currentBid = nextBid.getAsInt();
        highestBidder = PlayerSeat.DECLARER;

        StringBuilder biddingLog = new StringBuilder("Zalicytowałeś ")
                .append(currentBid)
                .append(". ");

        runAutomaticBidding(biddingLog);

        if (phase == GamePhase.BIDDING) {
            biddingLog.append("Możesz zalicytować ")
                    .append(nextBidText())
                    .append(" albo spasować.");
            status = biddingLog.toString();
        }
    }

    public void takeSkatBeforeContract() {
        if (phase != GamePhase.DECLARER_DECISION) {
            status = "Skat możesz dobrać tylko bezpośrednio po wygraniu licytacji, zanim zadeklarujesz grę.";
            return;
        }

        playerHand.addAll(skatCards);
        skatCards.clear();
        skatVisible = true;
        skatTakenBeforeContract = true;
        phase = GamePhase.CONTRACT_SELECTION;
        sortHands();
        status = "Dobrałeś skat przed wyborem gry. Teraz wybierz kolor, grand albo null. "
                + "Po dobraniu skata nie możesz wybrać hand, zapowiedzi krawca, zapowiedzi szwarca ani ouvert, "
                + "z wyjątkiem ouvert przy grze null. Po wyborze gry odłożysz dwie karty do skata.";
    }

    public void chooseGameWithoutTakingSkat() {
        if (phase != GamePhase.DECLARER_DECISION) {
            status = "Decyzję o grze bez skata podejmujesz tylko po wygraniu licytacji.";
            return;
        }

        skatTakenBeforeContract = false;
        skatVisible = false;
        phase = GamePhase.CONTRACT_SELECTION;
        status = "Wybrałeś grę bez dobierania skata. Teraz zadeklaruj rodzaj gry z ręki i ewentualne zapowiedzi.";
    }

    public void confirmContract(GameContract selectedContract) {
        if (phase != GamePhase.CONTRACT_SELECTION) {
            status = "Najpierw zdecyduj: weź skat albo graj bez dobierania skata.";
            return;
        }

        if (skatTakenBeforeContract && hasForbiddenDeclarationAfterTakingSkat(selectedContract)) {
            status = "Po dobraniu skata nie możesz zadeklarować hand, krawca, szwarca ani ouvert. "
                    + "Wyjątek: ouvert jest dostępny tylko przy grze null.";
            return;
        }

        contract = normalizeContractForCurrentPath(selectedContract);
        gameType = contract.toCore();
        sortHands();

        if (!skatTakenBeforeContract) {
            configureCoreRound();
            startCardPlay("Wybrałeś " + contract.displayName()
                    + ". To gra z ręki, więc skat zostaje zakryty. Wychodzisz do pierwszej lewy.");
            return;
        }

        status = "Wybrałeś " + contract.displayName()
                + ". Skat jest już w ręce. Wybierz dwie karty i kliknij \"Odłóż kartę\".";
        phase = GamePhase.SKAT_EXCHANGE;
    }

    public void discardSelectedCardToSkat(Karta card) {
        if (phase != GamePhase.SKAT_EXCHANGE) {
            status = "Karty do skata odkładasz tylko po dobraniu skata.";
            return;
        }

        if (!playerHand.contains(card)) {
            status = "Nie możesz odłożyć tej karty, bo nie ma jej w ręce.";
            return;
        }

        playerHand.remove(card);
        skatCards.add(card);
        sortHands();

        int remaining = SKAT_SIZE - skatCards.size();
        if (remaining > 0) {
            status = "Odłożono " + CardFormatter.format(card)
                    + ". Odłóż jeszcze " + remaining + " kartę do skata.";
            return;
        }

        configureCoreRound();
        startCardPlay("Odłożyłeś dwie karty do skata. Grasz " + contract.displayName()
                + ". Wychodzisz do pierwszej lewy.");
    }

    public void playCard(Karta card) {
        if (phase == GamePhase.BIDDING) {
            status = "Najpierw zakończ licytację.";
            return;
        }

        if (phase == GamePhase.DECLARER_DECISION) {
            status = "Najpierw zdecyduj, czy bierzesz skat, czy grasz bez dobierania skata.";
            return;
        }

        if (phase == GamePhase.CONTRACT_SELECTION) {
            status = "Najpierw wybierz rodzaj gry.";
            return;
        }

        if (phase == GamePhase.SKAT_EXCHANGE) {
            status = "Najpierw odłóż dwie karty do skata.";
            return;
        }

        if (finished) {
            status = "Rozdanie jest zakończone. Kliknij \"Nowe rozdanie\".";
            return;
        }

        if (nextPlayer != PlayerSeat.DECLARER) {
            playAutomaticCardsUntilPlayerTurn();
            if (finished || nextPlayer != PlayerSeat.DECLARER) {
                return;
            }
        }

        if (!playerHand.contains(card)) {
            status = "Nie możesz zagrać tej karty, bo nie ma jej już w ręce.";
            return;
        }

        if (!TrickRules.canPlay(card, playerHand, currentTrickCards(), gameType)) {
            status = TrickRules.illegalMoveMessage(card, playerHand, currentTrickCards(), gameType);
            return;
        }

        playCardFor(PlayerSeat.DECLARER, card);
        playAutomaticCardsUntilPlayerTurn();

        if (!finished) {
            updateStatusForPlayerTurn();
        }
    }

    public void pass() {
        if (phase == GamePhase.BIDDING) {
            passBidding();
            return;
        }

        if (finished) {
            return;
        }

        phase = GamePhase.FINISHED;
        finished = true;
        status = "Spasowałeś/poddałeś rozdanie. Kliknij \"Nowe rozdanie\", aby zacząć od nowa.";
    }

    public GameSnapshot snapshot() {
        return new GameSnapshot(
                CardSorter.sortedCopy(playerHand, gameType),
                List.copyOf(currentTrickCards()),
                List.copyOf(skatCards),
                skatVisible,
                opponentOneHand.size(),
                opponentTwoHand.size(),
                declarerCollectedCards.size(),
                phase,
                currentBid,
                nextBidValue(),
                highestBidder == null ? "brak" : highestBidder.displayName(),
                contract == null ? "brak" : contract.displayName(),
                phase == GamePhase.SKAT_EXCHANGE ? SKAT_SIZE - skatCards.size() : 0,
                phase == GamePhase.DECLARER_DECISION,
                phase == GamePhase.DECLARER_DECISION,
                skatTakenBeforeContract && phase == GamePhase.CONTRACT_SELECTION,
                !skatTakenBeforeContract && phase == GamePhase.CONTRACT_SELECTION,
                phase == GamePhase.BIDDING && !finished,
                phase == GamePhase.CONTRACT_SELECTION && !finished,
                phase == GamePhase.SKAT_EXCHANGE && !finished,
                phase == GamePhase.PLAYING && !finished,
                !finished,
                true,
                status,
                finished
        );
    }

    private GameContract normalizeContractForCurrentPath(GameContract selectedContract) {
        if (!skatTakenBeforeContract) {
            return new GameContract(
                    selectedContract.type(),
                    selectedContract.color(),
                    true,
                    selectedContract.schneiderAnnounced(),
                    selectedContract.schwarzAnnounced(),
                    selectedContract.ouvert()
            );
        }

        return selectedContract;
    }

    private boolean hasForbiddenDeclarationAfterTakingSkat(GameContract selectedContract) {
        if (selectedContract.hand() || selectedContract.schneiderAnnounced() || selectedContract.schwarzAnnounced()) {
            return true;
        }

        return selectedContract.ouvert() && selectedContract.type() != TypGry.NULL;
    }

    private void runAutomaticBidding(StringBuilder biddingLog) {
        for (PlayerSeat opponent : List.of(PlayerSeat.OPPONENT_ONE, PlayerSeat.OPPONENT_TWO)) {
            if (passedBidders.contains(opponent)) {
                continue;
            }

            OptionalInt nextBid = BidLadder.nextAfter(currentBid);
            int maximumBid = opponentMaximumBids.getOrDefault(opponent, 0);

            if (nextBid.isPresent() && nextBid.getAsInt() <= maximumBid) {
                currentBid = nextBid.getAsInt();
                highestBidder = opponent;
                biddingLog.append(opponent.displayName())
                        .append(" licytuje ")
                        .append(currentBid)
                        .append(". ");
            } else {
                passedBidders.add(opponent);
                biddingLog.append(opponent.displayName())
                        .append(" pasuje. ");
            }
        }

        if (bothOpponentsPassed()) {
            if (highestBidder == PlayerSeat.DECLARER) {
                startContractSelectionAfterWonBidding(biddingLog.toString());
            } else {
                finishAfterLostBidding(biddingLog.toString());
            }
        }
    }

    private void passBidding() {
        passedBidders.add(PlayerSeat.DECLARER);
        phase = GamePhase.FINISHED;
        finished = true;

        if (highestBidder == null) {
            status = "Spasowałeś przed pierwszą ofertą. W tej wersji prototypu rozgrywasz tylko rozdania wygrane przez gracza.";
            return;
        }

        status = "Spasowałeś. Licytację wygrał " + highestBidder.displayName()
                + " za " + currentBid
                + ". Kliknij \"Nowe rozdanie\", aby zagrać kolejne rozdanie.";
    }

    private void startContractSelectionAfterWonBidding(String biddingLog) {
        phase = GamePhase.DECLARER_DECISION;
        finished = false;
        nextPlayer = PlayerSeat.DECLARER;
        status = biddingLog
                + "Wygrałeś licytację za " + currentBid
                + ". Teraz najpierw zdecyduj: weź skat przed deklaracją gry albo graj bez dobierania skata.";
    }

    private void finishAfterLostBidding(String biddingLog) {
        phase = GamePhase.FINISHED;
        finished = true;
        status = biddingLog
                + "Licytację wygrał " + highestBidder.displayName()
                + " za " + currentBid
                + ". W tej wersji prototypu grę rozgrywasz tylko wtedy, gdy licytację wygra gracz.";
    }

    private void startCardPlay(String message) {
        phase = GamePhase.PLAYING;
        finished = false;
        skatVisible = !contract.hand();
        nextPlayer = PlayerSeat.DECLARER;
        status = message;
    }

    private void playAutomaticCardsUntilPlayerTurn() {
        while (!finished && nextPlayer != PlayerSeat.DECLARER) {
            List<Karta> hand = handOf(nextPlayer);
            if (hand.isEmpty()) {
                finishRound();
                return;
            }

            Karta card = TrickRules.firstLegalCard(hand, currentTrickCards(), gameType)
                    .orElse(hand.get(0));
            playCardFor(nextPlayer, card);
        }
    }

    private void playCardFor(PlayerSeat player, Karta card) {
        handOf(player).remove(card);
        currentTrick.add(new PlayedCard(player, card));

        if (currentTrick.size() == 3) {
            collectCurrentTrick();
            if (allHandsAreEmpty()) {
                finishRound();
            }
            return;
        }

        nextPlayer = player.nextClockwise();
    }

    private void collectCurrentTrick() {
        PlayedCard winner = TrickWinner.strongestCard(currentTrick, gameType);
        List<Karta> trickCards = currentTrickCards();

        if (winner.player() == PlayerSeat.DECLARER) {
            declarerCollectedCards.addAll(trickCards);
            declarerWonTricks++;
        } else {
            opponentsCollectedCards.addAll(trickCards);
            opponentsWonTricks++;
        }

        currentTrick.clear();
        nextPlayer = winner.player();
        lastTrickSummary = "Lewę zebrał: " + winner.player().displayName()
                + " kartą " + CardFormatter.format(winner.card())
                + ".";
        status = lastTrickSummary + " Ten gracz wychodzi do następnej lewy.";
    }

    private void finishRound() {
        declarer.ustawZebraneKarty(new ArrayList<>(declarerCollectedCards));
        WynikGry result = round.obliczWynik();
        phase = GamePhase.FINISHED;
        finished = true;

        String outcome = result.wygrana ? "wygrana" : "przegrana";
        status = "Koniec rozdania. Gra: " + contract.displayName()
                + ". Licytacja: " + currentBid
                + ". Lewy gracza: " + declarerWonTricks
                + ", lewy przeciwników: " + opponentsWonTricks
                + ". Karty zebrane przez gracza: " + declarerCollectedCards.size()
                + ". Wynik z Projekt_PIO: " + outcome
                + ", wartość/oczka = " + result.wynik + ".";
    }

    private void updateStatusForPlayerTurn() {
        String prefix = lastTrickSummary == null || lastTrickSummary.isBlank() ? "" : lastTrickSummary + " ";

        if (currentTrick.isEmpty()) {
            status = prefix + "Twoja kolej. Wychodzisz do nowej lewy.";
            return;
        }

        Karta leadCard = currentTrick.get(0).card();
        if (TrickRules.isTrump(leadCard, gameType)) {
            status = prefix + "Twoja kolej. Pierwsza karta lewy jest atutem, więc dołóż atut, jeśli go masz.";
            return;
        }

        status = prefix + "Twoja kolej. Dołóż do koloru " + CardFormatter.formatSuit(leadCard.kolor()) + ", jeśli go masz.";
    }

    private List<Karta> handOf(PlayerSeat player) {
        return switch (player) {
            case DECLARER -> playerHand;
            case OPPONENT_ONE -> opponentOneHand;
            case OPPONENT_TWO -> opponentTwoHand;
        };
    }

    private List<Karta> currentTrickCards() {
        return currentTrick.stream()
                .map(PlayedCard::card)
                .toList();
    }

    private boolean allHandsAreEmpty() {
        return playerHand.isEmpty() && opponentOneHand.isEmpty() && opponentTwoHand.isEmpty();
    }

    private boolean bothOpponentsPassed() {
        return passedBidders.contains(PlayerSeat.OPPONENT_ONE)
                && passedBidders.contains(PlayerSeat.OPPONENT_TWO);
    }

    private int nextBidValue() {
        return BidLadder.nextAfter(currentBid)
                .orElse(0);
    }

    private String nextBidText() {
        int nextBid = nextBidValue();
        if (nextBid == 0) {
            return "brak wyższej wartości";
        }
        return Integer.toString(nextBid);
    }

    private void configureCoreRound() {
        if (playerHand.size() != HAND_SIZE) {
            throw new IllegalStateException("Przed rozpoczęciem gry gracz musi mieć dokładnie 10 kart w ręce.");
        }
        if (skatCards.size() != SKAT_SIZE) {
            throw new IllegalStateException("Przed rozpoczęciem gry skat musi mieć dokładnie 2 karty.");
        }

        declarer.ustawPosiadaneKarty(new ArrayList<>(playerHand));

        Skat skat = new Skat();
        skat.ustawKarta1(skatCards.get(0));
        skat.ustawKarta2(skatCards.get(1));

        round = new Rozdanie(declarer, opponentOne, opponentTwo);
        round.ustawSkat(skat);
        round.ustawRodzajGry(gameType);
        round.ustawWartoscLicytacji(currentBid);
    }

    private void sortHands() {
        CardSorter.sort(playerHand, gameType);
        CardSorter.sort(opponentOneHand, gameType);
        CardSorter.sort(opponentTwoHand, gameType);
    }
}
