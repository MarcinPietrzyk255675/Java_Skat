package com.example.java_skat.game;

import pl.skat.core.Gracz;
import pl.skat.core.Karta;
import pl.skat.core.RodzajGry;
import pl.skat.core.Rozdanie;
import pl.skat.core.Skat;
import pl.skat.core.TypGry;
import pl.skat.core.WynikGry;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

public class MultiplayerGameController {
    private static final int HAND_SIZE = 10;
    private static final int SKAT_SIZE = 2;
    private static final int TOTAL_DEALS = 12;

    private final EnumMap<PlayerSeat, String> playerNames = new EnumMap<>(PlayerSeat.class);
    private final EnumMap<PlayerSeat, List<Karta>> hands = new EnumMap<>(PlayerSeat.class);
    private final EnumMap<PlayerSeat, HandPosition> positions = new EnumMap<>(PlayerSeat.class);
    private final EnumMap<PlayerSeat, Integer> totalScores = new EnumMap<>(PlayerSeat.class);

    private final List<Karta> skatCards = new ArrayList<>();
    private final List<PlayedCard> currentTrick = new ArrayList<>();
    private final List<PlayedCard> lastCompletedTrick = new ArrayList<>();
    private final List<Karta> declarerCollectedCards = new ArrayList<>();
    private final List<Karta> opponentsCollectedCards = new ArrayList<>();
    private final List<Karta> declarerInitialHand = new ArrayList<>();

    private GamePhase phase;
    private GameContract contract;
    private RodzajGry gameType;
    private Rozdanie round;

    private PlayerSeat biddingAsker;
    private PlayerSeat biddingResponder;
    private boolean waitingForBidAnswer;
    private boolean rearhandEnteredBidding;
    private boolean forehandFinalDecision;

    private PlayerSeat highestBidder;
    private PlayerSeat declarerSeat;
    private PlayerSeat nextPlayer;
    private int currentDealNumber;
    private int currentBid;
    private int declarerWonTricks;
    private int opponentsWonTricks;
    private boolean skatVisible;
    private boolean skatTakenBeforeContract;
    private boolean finished;
    private String status;
    private String lastTrickSummary;

    public MultiplayerGameController(Map<PlayerSeat, String> names) {
        playerNames.putAll(names);
        for (PlayerSeat seat : PlayerSeat.values()) {
            playerNames.putIfAbsent(seat, seat.displayName());
            hands.put(seat, new ArrayList<>());
            totalScores.put(seat, 0);
        }
        startNewDeal();
    }

    public synchronized void startNewDeal() {
        if (currentDealNumber > 0 && !finished) {
            status = "Nie można rozpocząć kolejnego rozdania, gdy obecne nadal trwa.";
            return;
        }

        if (currentDealNumber >= TOTAL_DEALS) {
            phase = GamePhase.FINISHED;
            finished = true;
            status = gameOverSummary();
            return;
        }

        currentDealNumber++;
        assignPositionsForCurrentDeal();

        List<Karta> deck = DeckFactory.shuffledDeck();

        for (List<Karta> hand : hands.values()) {
            hand.clear();
        }
        skatCards.clear();
        currentTrick.clear();
        lastCompletedTrick.clear();
        declarerCollectedCards.clear();
        opponentsCollectedCards.clear();
        declarerInitialHand.clear();

        hands.get(PlayerSeat.DECLARER).addAll(deck.subList(0, HAND_SIZE));
        hands.get(PlayerSeat.OPPONENT_ONE).addAll(deck.subList(HAND_SIZE, HAND_SIZE * 2));
        hands.get(PlayerSeat.OPPONENT_TWO).addAll(deck.subList(HAND_SIZE * 2, HAND_SIZE * 3));
        skatCards.addAll(deck.subList(HAND_SIZE * 3, HAND_SIZE * 3 + SKAT_SIZE));

        contract = null;
        gameType = GameContract.defaultGrand().toCore();
        sortHands();

        round = null;
        phase = GamePhase.BIDDING;
        highestBidder = null;
        declarerSeat = null;
        nextPlayer = null;
        currentBid = 0;
        declarerWonTricks = 0;
        opponentsWonTricks = 0;
        skatVisible = false;
        skatTakenBeforeContract = false;
        forehandFinalDecision = false;
        finished = false;
        lastTrickSummary = "";
        configureInitialBiddingPair();
        status = "Rozdanie " + currentDealNumber + "/" + TOTAL_DEALS + ". "
                + positionSummary()
                + " Licytację zaczyna środek jako pytający: " + nameOf(biddingAsker)
                + ". Pyta przodka: " + nameOf(biddingResponder) + ".";
    }

    public synchronized void bid(PlayerSeat seat) {
        if (phase != GamePhase.BIDDING || finished) {
            status = "Licytacja nie jest teraz aktywna.";
            return;
        }

        if (forehandFinalDecision) {
            forehandDeclaresMinimumGame(seat);
            return;
        }

        if (waitingForBidAnswer) {
            answerYes(seat);
            return;
        }

        askNextValue(seat);
    }

    public synchronized void pass(PlayerSeat seat) {
        if (phase == GamePhase.BIDDING) {
            passBidding(seat);
            return;
        }

        if (finished) {
            return;
        }

        phase = GamePhase.FINISHED;
        finished = true;
        status = nameOf(seat) + " poddał rozdanie. Można rozpocząć nowe rozdanie.";
    }

    public synchronized void takeSkatBeforeContract(PlayerSeat seat) {
        if (!isDeclarerDecisionTurn(seat)) {
            status = nameOf(seat) + " nie może teraz dobrać skata.";
            return;
        }

        handOf(seat).addAll(skatCards);
        skatCards.clear();
        skatVisible = true;
        skatTakenBeforeContract = true;
        phase = GamePhase.CONTRACT_SELECTION;
        sortHands();
        status = nameOf(seat) + " dobrał skat przed deklaracją gry. Teraz wybiera grę.";
    }

    public synchronized void chooseGameWithoutTakingSkat(PlayerSeat seat) {
        if (!isDeclarerDecisionTurn(seat)) {
            status = nameOf(seat) + " nie może teraz wybrać gry bez skata.";
            return;
        }

        skatTakenBeforeContract = false;
        skatVisible = false;
        phase = GamePhase.CONTRACT_SELECTION;
        status = nameOf(seat) + " gra bez dobierania skata. Teraz wybiera grę z ręki.";
    }

    public synchronized void confirmContract(PlayerSeat seat, GameContract selectedContract) {
        if (phase != GamePhase.CONTRACT_SELECTION || seat != declarerSeat) {
            status = nameOf(seat) + " nie może teraz deklarować gry.";
            return;
        }

        if (skatTakenBeforeContract && hasForbiddenDeclarationAfterTakingSkat(selectedContract)) {
            status = "Po dobraniu skata nie można zadeklarować hand, krawca, szwarca ani ouvert. "
                    + "Wyjątek: ouvert jest dostępny tylko przy grze null.";
            return;
        }

        contract = normalizeContractForCurrentPath(selectedContract);
        gameType = contract.toCore();
        sortHands();

        if (!skatTakenBeforeContract) {
            configureCoreRound();
            startCardPlay(nameOf(seat) + " zadeklarował: " + contract.displayName()
                    + ". To gra z ręki, skat zostaje zakryty.");
            return;
        }

        phase = GamePhase.SKAT_EXCHANGE;
        status = nameOf(seat) + " zadeklarował: " + contract.displayName()
                + ". Teraz odkłada dwie karty do skata.";
    }

    public synchronized void discardSelectedCardToSkat(PlayerSeat seat, Karta card) {
        if (phase != GamePhase.SKAT_EXCHANGE || seat != declarerSeat) {
            status = nameOf(seat) + " nie może teraz odkładać kart do skata.";
            return;
        }

        List<Karta> hand = handOf(seat);
        if (!hand.contains(card)) {
            status = "Nie możesz odłożyć tej karty, bo nie ma jej w ręce.";
            return;
        }

        hand.remove(card);
        skatCards.add(card);
        sortHands();

        int remaining = SKAT_SIZE - skatCards.size();
        if (remaining > 0) {
            status = nameOf(seat) + " odłożył " + CardFormatter.format(card)
                    + ". Do odłożenia została jeszcze jedna karta.";
            return;
        }

        configureCoreRound();
        startCardPlay(nameOf(seat) + " odłożył dwie karty do skata. Gramy: " + contract.displayName() + ".");
    }

    public synchronized void playCard(PlayerSeat seat, Karta card) {
        if (phase != GamePhase.PLAYING || finished) {
            status = "Rozgrywka kartami jeszcze się nie rozpoczęła albo już się skończyła.";
            return;
        }

        if (seat != nextPlayer) {
            status = "Teraz ruch ma " + positionNameOf(nextPlayer) + ": " + nameOf(nextPlayer) + ", a nie " + nameOf(seat) + ".";
            return;
        }

        List<Karta> hand = handOf(seat);
        if (!hand.contains(card)) {
            status = "Nie możesz zagrać tej karty, bo nie ma jej w ręce.";
            return;
        }

        if (!TrickRules.canPlay(card, hand, currentTrickCards(), gameType)) {
            status = TrickRules.illegalMoveMessage(card, hand, currentTrickCards(), gameType);
            return;
        }

        playCardFor(seat, card);
        if (!finished) {
            updateStatusForCurrentTurn();
        }
    }

    public synchronized GameSnapshot snapshotFor(PlayerSeat seat) {
        List<Karta> ownHand = CardSorter.sortedCopy(handOf(seat), gameType);
        PlayerSeat leftOpponent = leftNeighborOf(seat);
        PlayerSeat rightOpponent = rightNeighborOf(seat);
        boolean rightVisible = isOpponentHandVisibleFor(seat, rightOpponent);
        boolean leftVisible = isOpponentHandVisibleFor(seat, leftOpponent);
        PlayedCard ownTrickCard = trickCardPlayedBy(seat);
        PlayedCard leftTrickCard = trickCardPlayedBy(leftOpponent);
        PlayedCard rightTrickCard = trickCardPlayedBy(rightOpponent);

        return new GameSnapshot(
                ownHand,
                List.copyOf(displayedTrickCards()),
                ownTrickCard == null ? null : ownTrickCard.card(),
                trickLabel(ownTrickCard),
                leftTrickCard == null ? null : leftTrickCard.card(),
                trickLabel(leftTrickCard),
                rightTrickCard == null ? null : rightTrickCard.card(),
                trickLabel(rightTrickCard),
                nameOf(seat) + " (" + positionNameOf(seat) + ")",
                totalScores.getOrDefault(seat, 0),
                visibleSkatFor(seat),
                isSkatVisibleFor(seat),
                rightVisible ? CardSorter.sortedCopy(handOf(rightOpponent), gameType) : List.of(),
                rightVisible,
                handOf(rightOpponent).size(),
                nameOf(rightOpponent) + " (" + positionNameOf(rightOpponent) + ")",
                totalScores.getOrDefault(rightOpponent, 0),
                leftVisible ? CardSorter.sortedCopy(handOf(leftOpponent), gameType) : List.of(),
                leftVisible,
                handOf(leftOpponent).size(),
                nameOf(leftOpponent) + " (" + positionNameOf(leftOpponent) + ")",
                totalScores.getOrDefault(leftOpponent, 0),
                collectedCardsCountFor(seat),
                phase,
                currentBid,
                nextBidValueFor(seat),
                highestBidder == null ? "brak" : nameOf(highestBidder),
                contract == null ? "brak" : contract.displayName(),
                phase == GamePhase.SKAT_EXCHANGE && seat == declarerSeat ? SKAT_SIZE - skatCards.size() : 0,
                phase == GamePhase.DECLARER_DECISION && seat == declarerSeat,
                phase == GamePhase.DECLARER_DECISION && seat == declarerSeat,
                skatTakenBeforeContract && phase == GamePhase.CONTRACT_SELECTION && seat == declarerSeat,
                !skatTakenBeforeContract && phase == GamePhase.CONTRACT_SELECTION && seat == declarerSeat,
                canUseBidAction(seat),
                phase == GamePhase.CONTRACT_SELECTION && seat == declarerSeat && !finished,
                phase == GamePhase.SKAT_EXCHANGE && seat == declarerSeat && !finished,
                phase == GamePhase.PLAYING && seat == nextPlayer && !finished,
                canPass(seat),
                finished && currentDealNumber < TOTAL_DEALS,
                bidActionTextFor(seat),
                passActionTextFor(seat),
                personalStatusFor(seat),
                finished,
                currentDealNumber,
                TOTAL_DEALS,
                positionNameOf(seat),
                positionSummary()
        );
    }

    private void configureInitialBiddingPair() {
        biddingAsker = seatWithPosition(HandPosition.MIDDLEHAND);
        biddingResponder = seatWithPosition(HandPosition.FOREHAND);
        waitingForBidAnswer = false;
        rearhandEnteredBidding = false;
    }

    private void askNextValue(PlayerSeat seat) {
        if (seat != biddingAsker) {
            status = nameOf(seat) + " nie może teraz pytać. Pytającym jest "
                    + positionNameOf(biddingAsker) + ": " + nameOf(biddingAsker) + ".";
            return;
        }

        OptionalInt nextBid = BidLadder.nextAfter(currentBid);
        if (nextBid.isEmpty()) {
            status = "Nie ma już wyższej wartości licytacji w tej wersji prototypu. "
                    + nameOf(biddingAsker) + " może spasować.";
            return;
        }

        currentBid = nextBid.getAsInt();
        waitingForBidAnswer = true;
        status = positionNameOf(biddingAsker) + " " + nameOf(biddingAsker)
                + " pyta " + positionNameOf(biddingResponder) + " " + nameOf(biddingResponder)
                + " o " + currentBid + ". " + nameOf(biddingResponder) + " odpowiada: Tak albo Pas.";
    }

    private void answerYes(PlayerSeat seat) {
        if (seat != biddingResponder) {
            status = nameOf(seat) + " nie może teraz odpowiedzieć. Odpowiada "
                    + positionNameOf(biddingResponder) + ": " + nameOf(biddingResponder) + ".";
            return;
        }

        highestBidder = biddingResponder;
        waitingForBidAnswer = false;
        status = positionNameOf(biddingResponder) + " " + nameOf(biddingResponder)
                + " odpowiada Tak na " + currentBid + ". "
                + nameOf(biddingAsker) + " może pytać dalej albo spasować.";
    }

    private void passBidding(PlayerSeat seat) {
        if (forehandFinalDecision) {
            forehandPassesOutDeal(seat);
            return;
        }

        if (waitingForBidAnswer) {
            if (seat != biddingResponder) {
                status = nameOf(seat) + " nie może teraz pasować. Odpowiada " + nameOf(biddingResponder) + ".";
                return;
            }
            PlayerSeat pairWinner = biddingAsker;
            highestBidder = pairWinner;
            waitingForBidAnswer = false;
            status = positionNameOf(seat) + " " + nameOf(seat) + " pasuje. "
                    + nameOf(pairWinner) + " zostaje w licytacji.";
            finishCurrentBiddingPair(pairWinner);
            return;
        }

        if (seat != biddingAsker) {
            status = nameOf(seat) + " nie może teraz pasować. Decyzję ma pytający: " + nameOf(biddingAsker) + ".";
            return;
        }

        PlayerSeat pairWinner = biddingResponder;
        highestBidder = pairWinner;
        status = positionNameOf(seat) + " " + nameOf(seat) + " pasuje. "
                + nameOf(pairWinner) + " zostaje w licytacji.";
        finishCurrentBiddingPair(pairWinner);
    }

    private void finishCurrentBiddingPair(PlayerSeat pairWinner) {
        if (!rearhandEnteredBidding) {
            rearhandEnteredBidding = true;
            biddingAsker = seatWithPosition(HandPosition.REARHAND);
            biddingResponder = pairWinner;
            waitingForBidAnswer = false;
            status = status + " Do licytacji wchodzi zadek jako pytający: " + nameOf(biddingAsker)
                    + ". Pyta gracza: " + nameOf(biddingResponder) + ".";
            return;
        }

        if (currentBid == 0 && pairWinner == seatWithPosition(HandPosition.FOREHAND)) {
            enterForehandFinalDecision();
            return;
        }

        highestBidder = pairWinner;
        startDeclarerDecision();
    }

    private void enterForehandFinalDecision() {
        forehandFinalDecision = true;
        biddingAsker = seatWithPosition(HandPosition.FOREHAND);
        biddingResponder = null;
        waitingForBidAnswer = false;
        highestBidder = null;
        status = status + " Środek i zadek spasowali bez licytacji. Przodek "
                + nameOf(biddingAsker)
                + " może zagrać za 18 albo spasować. Jeśli spasuje, przechodzimy do następnego rozdania.";
    }

    private void forehandDeclaresMinimumGame(PlayerSeat seat) {
        PlayerSeat forehand = seatWithPosition(HandPosition.FOREHAND);
        if (seat != forehand) {
            status = nameOf(seat) + " nie może teraz licytować. Decyzję ma przodek: " + nameOf(forehand) + ".";
            return;
        }

        currentBid = 18;
        highestBidder = forehand;
        forehandFinalDecision = false;
        status = "Przodek " + nameOf(forehand) + " decyduje się grać i licytuje 18.";
        startDeclarerDecision();
    }

    private void forehandPassesOutDeal(PlayerSeat seat) {
        PlayerSeat forehand = seatWithPosition(HandPosition.FOREHAND);
        if (seat != forehand) {
            status = nameOf(seat) + " nie może teraz pasować. Decyzję ma przodek: " + nameOf(forehand) + ".";
            return;
        }

        phase = GamePhase.FINISHED;
        finished = true;
        forehandFinalDecision = false;
        status = "Przodek " + nameOf(forehand)
                + " też pasuje. Wszyscy spasowali, więc rozdanie " + currentDealNumber + "/" + TOTAL_DEALS
                + " kończy się bez gry."
                + (currentDealNumber < TOTAL_DEALS
                ? " Można przejść do kolejnego rozdania."
                : " " + gameOverSummary());
    }

    private void startDeclarerDecision() {
        forehandFinalDecision = false;
        declarerSeat = highestBidder;
        nextPlayer = declarerSeat;
        phase = GamePhase.DECLARER_DECISION;
        finished = false;
        status = "Licytację wygrał " + positionNameOf(declarerSeat) + ": " + nameOf(declarerSeat) + " za " + currentBid
                + ". Najpierw decyduje, czy bierze skat, czy gra bez skata.";
    }

    private void startCardPlay(String message) {
        phase = GamePhase.PLAYING;
        finished = false;
        skatVisible = !contract.hand();
        nextPlayer = seatWithPosition(HandPosition.FOREHAND);
        status = message + " Do pierwszej lewy wychodzi przodek: " + nameOf(nextPlayer) + ".";
    }

    private void playCardFor(PlayerSeat player, Karta card) {
        handOf(player).remove(card);
        if (currentTrick.isEmpty()) {
            lastCompletedTrick.clear();
        }
        currentTrick.add(new PlayedCard(player, card));

        if (currentTrick.size() == 3) {
            collectCurrentTrick();
            if (allHandsAreEmpty()) {
                finishRound();
            }
            return;
        }

        nextPlayer = nextClockwiseSeat(player);
    }

    private void collectCurrentTrick() {
        PlayedCard winner = TrickWinner.strongestCard(currentTrick, gameType);
        List<Karta> trickCards = currentTrickCards();

        if (winner.player() == declarerSeat) {
            declarerCollectedCards.addAll(trickCards);
            declarerWonTricks++;
        } else {
            opponentsCollectedCards.addAll(trickCards);
            opponentsWonTricks++;
        }

        lastCompletedTrick.clear();
        lastCompletedTrick.addAll(currentTrick);
        currentTrick.clear();
        nextPlayer = winner.player();
        lastTrickSummary = "Lewę zebrał " + nameOf(winner.player())
                + " kartą " + CardFormatter.format(winner.card()) + ".";
        status = lastTrickSummary + " Ten gracz wychodzi do następnej lewy.";
    }

    private void finishRound() {
        if (round == null) {
            phase = GamePhase.FINISHED;
            finished = true;
            status = "Koniec rozdania, ale nie skonfigurowano obiektu Rozdanie.";
            return;
        }

        rebuildRoundForScore();
        WynikGry result = round.obliczWynik();
        int gameValue = round.obliczWartoscGry();
        int scoreDelta = result.wygrana ? gameValue : Math.min(result.wynik, -2 * gameValue);
        totalScores.merge(declarerSeat, scoreDelta, Integer::sum);

        phase = GamePhase.FINISHED;
        finished = true;
        String outcome = result.wygrana ? "wygrana" : "przegrana";
        status = "Koniec rozdania " + currentDealNumber + "/" + TOTAL_DEALS + ". Rozgrywający: " + nameOf(declarerSeat)
                + ". Gra: " + contract.displayName()
                + ". Licytacja: " + currentBid
                + ". Lewy rozgrywającego: " + declarerWonTricks
                + ", lewy przeciwników: " + opponentsWonTricks
                + ". Wynik rozdania: " + outcome
                + ", zmiana punktów rozgrywającego = " + scoreDelta + ". "
                + scoreSummary()
                + (currentDealNumber < TOTAL_DEALS
                ? " Można rozpocząć kolejne rozdanie."
                : " " + gameOverSummary());
    }

    private void rebuildRoundForScore() {
        Gracz coreDeclarer = new Gracz();
        coreDeclarer.ustawPosiadaneKarty(new ArrayList<>(declarerInitialHand));
        coreDeclarer.ustawZebraneKarty(new ArrayList<>(declarerCollectedCards));

        Gracz opponentOne = new Gracz();
        Gracz opponentTwo = new Gracz();
        Skat skat = new Skat();
        skat.ustawKarta1(skatCards.get(0));
        skat.ustawKarta2(skatCards.get(1));

        round = new Rozdanie(coreDeclarer, opponentOne, opponentTwo);
        round.ustawSkat(skat);
        round.ustawRodzajGry(gameType);
        round.ustawWartoscLicytacji(currentBid);
    }

    private void configureCoreRound() {
        if (handOf(declarerSeat).size() != HAND_SIZE) {
            throw new IllegalStateException("Przed rozpoczęciem gry rozgrywający musi mieć dokładnie 10 kart w ręce.");
        }
        if (skatCards.size() != SKAT_SIZE) {
            throw new IllegalStateException("Przed rozpoczęciem gry skat musi mieć dokładnie 2 karty.");
        }
        declarerInitialHand.clear();
        declarerInitialHand.addAll(handOf(declarerSeat));
        rebuildRoundForScore();
    }

    private void updateStatusForCurrentTurn() {
        String prefix = lastTrickSummary == null || lastTrickSummary.isBlank() ? "" : lastTrickSummary + " ";

        if (currentTrick.isEmpty()) {
            status = prefix + "Wychodzi " + positionNameOf(nextPlayer) + ": " + nameOf(nextPlayer) + ".";
            return;
        }

        Karta leadCard = currentTrick.get(0).card();
        if (TrickRules.isTrump(leadCard, gameType)) {
            status = prefix + "Teraz ruch ma " + positionNameOf(nextPlayer) + ": " + nameOf(nextPlayer)
                    + ". Pierwsza karta lewy jest atutem, więc trzeba dołożyć atut, jeśli się go ma.";
            return;
        }

        status = prefix + "Teraz ruch ma " + positionNameOf(nextPlayer) + ": " + nameOf(nextPlayer)
                + ". Trzeba dołożyć do koloru " + CardFormatter.formatSuit(leadCard.kolor()) + ", jeśli się go ma.";
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

    private boolean isDeclarerDecisionTurn(PlayerSeat seat) {
        return phase == GamePhase.DECLARER_DECISION && !finished && seat == declarerSeat;
    }

    private boolean canUseBidAction(PlayerSeat seat) {
        if (phase != GamePhase.BIDDING || finished) {
            return false;
        }
        if (forehandFinalDecision) {
            return seat == seatWithPosition(HandPosition.FOREHAND);
        }
        return waitingForBidAnswer ? seat == biddingResponder : seat == biddingAsker;
    }

    private boolean canPass(PlayerSeat seat) {
        if (finished) {
            return false;
        }
        if (phase == GamePhase.BIDDING) {
            if (forehandFinalDecision) {
                return seat == seatWithPosition(HandPosition.FOREHAND);
            }
            return waitingForBidAnswer ? seat == biddingResponder : seat == biddingAsker;
        }
        return phase == GamePhase.PLAYING && seat == nextPlayer;
    }

    private int nextBidValueFor(PlayerSeat seat) {
        if (phase != GamePhase.BIDDING) {
            return 0;
        }
        if (forehandFinalDecision) {
            return 18;
        }
        if (waitingForBidAnswer) {
            return currentBid;
        }
        return BidLadder.nextAfter(currentBid).orElse(0);
    }

    private String bidActionTextFor(PlayerSeat seat) {
        if (phase != GamePhase.BIDDING) {
            return "Licytacja zakończona";
        }
        if (forehandFinalDecision) {
            return seat == seatWithPosition(HandPosition.FOREHAND) ? "Gram 18" : "Czekaj";
        }
        if (waitingForBidAnswer) {
            return seat == biddingResponder ? "Tak (" + currentBid + ")" : "Czekaj";
        }
        int nextBid = BidLadder.nextAfter(currentBid).orElse(0);
        return seat == biddingAsker && nextBid > 0 ? "Pytaj " + nextBid : "Czekaj";
    }

    private String passActionTextFor(PlayerSeat seat) {
        if (phase == GamePhase.BIDDING) {
            return forehandFinalDecision ? "Pas — następne rozdanie" : "Pas";
        }
        return "Poddaj rozdanie";
    }

    private List<Karta> handOf(PlayerSeat player) {
        return hands.get(player);
    }

    private List<Karta> currentTrickCards() {
        return currentTrick.stream()
                .map(PlayedCard::card)
                .toList();
    }

    private List<PlayedCard> displayedTrick() {
        return currentTrick.isEmpty() ? lastCompletedTrick : currentTrick;
    }

    private List<Karta> displayedTrickCards() {
        return displayedTrick().stream()
                .map(PlayedCard::card)
                .toList();
    }

    private boolean allHandsAreEmpty() {
        return hands.values().stream().allMatch(List::isEmpty);
    }

    private void sortHands() {
        for (List<Karta> hand : hands.values()) {
            CardSorter.sort(hand, gameType);
        }
    }

    private PlayerSeat leftNeighborOf(PlayerSeat seat) {
        return nextClockwiseSeat(seat);
    }

    private PlayerSeat rightNeighborOf(PlayerSeat seat) {
        HandPosition position = positions.get(seat);
        if (position == null) {
            return PlayerSeat.OPPONENT_TWO;
        }
        return seatWithPosition(previousClockwisePosition(position));
    }

    private PlayerSeat nextClockwiseSeat(PlayerSeat seat) {
        HandPosition position = positions.get(seat);
        if (position == null) {
            return seat.nextClockwise();
        }
        return seatWithPosition(nextClockwisePosition(position));
    }

    private HandPosition nextClockwisePosition(HandPosition position) {
        return switch (position) {
            case FOREHAND -> HandPosition.MIDDLEHAND;
            case MIDDLEHAND -> HandPosition.REARHAND;
            case REARHAND -> HandPosition.FOREHAND;
        };
    }

    private HandPosition previousClockwisePosition(HandPosition position) {
        return switch (position) {
            case FOREHAND -> HandPosition.REARHAND;
            case MIDDLEHAND -> HandPosition.FOREHAND;
            case REARHAND -> HandPosition.MIDDLEHAND;
        };
    }

    private PlayedCard trickCardPlayedBy(PlayerSeat seat) {
        return displayedTrick().stream()
                .filter(played -> played.player() == seat)
                .findFirst()
                .orElse(null);
    }

    private String trickLabel(PlayedCard playedCard) {
        if (playedCard == null) {
            return "";
        }
        return nameOf(playedCard.player()) + " (" + positionNameOf(playedCard.player()) + ")";
    }

    private boolean isOpponentHandVisibleFor(PlayerSeat viewer, PlayerSeat opponent) {
        return viewer != opponent
                && opponent == declarerSeat
                && contract != null
                && contract.ouvert()
                && (phase == GamePhase.PLAYING || phase == GamePhase.FINISHED);
    }

    private List<Karta> visibleSkatFor(PlayerSeat seat) {
        if (!isSkatVisibleFor(seat)) {
            return List.of();
        }
        return List.copyOf(skatCards);
    }

    private boolean isSkatVisibleFor(PlayerSeat seat) {
        return skatVisible && (seat == declarerSeat || phase == GamePhase.FINISHED);
    }

    private int collectedCardsCountFor(PlayerSeat seat) {
        if (seat == declarerSeat) {
            return declarerCollectedCards.size();
        }
        return opponentsCollectedCards.size();
    }

    private String personalStatusFor(PlayerSeat seat) {
        String base = status;
        if (phase == GamePhase.BIDDING) {
            if (forehandFinalDecision) {
                PlayerSeat forehand = seatWithPosition(HandPosition.FOREHAND);
                if (seat == forehand) {
                    return base + " To Twoja decyzja: grasz za 18 albo pasujesz całe rozdanie.";
                }
                return base + " Czekasz na decyzję przodka: " + nameOf(forehand) + ".";
            }
            if (waitingForBidAnswer) {
                if (seat == biddingResponder) {
                    return base + " To Twoja odpowiedź: Tak albo Pas.";
                }
                return base + " Czekasz na odpowiedź gracza: " + nameOf(biddingResponder) + ".";
            }
            if (seat == biddingAsker) {
                return base + " To Twoja decyzja: pytasz dalej albo pasujesz.";
            }
            return base + " Czekasz na pytanie gracza: " + nameOf(biddingAsker) + ".";
        }
        if (phase == GamePhase.DECLARER_DECISION || phase == GamePhase.CONTRACT_SELECTION || phase == GamePhase.SKAT_EXCHANGE) {
            return base + (seat == declarerSeat ? " To Twoja decyzja." : " Czekasz na decyzję rozgrywającego: " + nameOf(declarerSeat) + ".");
        }
        if (phase == GamePhase.PLAYING) {
            return base + (seat == nextPlayer ? " To Twój ruch." : " Czekasz na ruch gracza: " + nameOf(nextPlayer) + ".");
        }
        return base;
    }

    private String scoreSummary() {
        return "Tabela: "
                + nameOf(PlayerSeat.DECLARER) + " " + totalScores.getOrDefault(PlayerSeat.DECLARER, 0) + " pkt, "
                + nameOf(PlayerSeat.OPPONENT_ONE) + " " + totalScores.getOrDefault(PlayerSeat.OPPONENT_ONE, 0) + " pkt, "
                + nameOf(PlayerSeat.OPPONENT_TWO) + " " + totalScores.getOrDefault(PlayerSeat.OPPONENT_TWO, 0) + " pkt.";
    }

    private String gameOverSummary() {
        PlayerSeat winner = totalScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(PlayerSeat.DECLARER);
        return "Koniec całej rozgrywki po " + TOTAL_DEALS + " rozdaniach. Wygrał: "
                + nameOf(winner) + " z wynikiem " + totalScores.getOrDefault(winner, 0) + " pkt. "
                + scoreSummary();
    }

    private void assignPositionsForCurrentDeal() {
        PlayerSeat[] seats = PlayerSeat.values();
        HandPosition[] handPositions = HandPosition.values();
        int shift = (currentDealNumber - 1) % seats.length;

        positions.clear();
        for (int i = 0; i < seats.length; i++) {
            positions.put(seats[(shift + i) % seats.length], handPositions[i]);
        }
    }

    private PlayerSeat seatWithPosition(HandPosition position) {
        return positions.entrySet().stream()
                .filter(entry -> entry.getValue() == position)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(PlayerSeat.DECLARER);
    }

    private String positionNameOf(PlayerSeat seat) {
        HandPosition position = positions.get(seat);
        return position == null ? "bez pozycji" : position.displayName();
    }

    private String positionSummary() {
        return "Pozycje: przodek — " + nameOf(seatWithPosition(HandPosition.FOREHAND))
                + ", środek — " + nameOf(seatWithPosition(HandPosition.MIDDLEHAND))
                + ", zadek — " + nameOf(seatWithPosition(HandPosition.REARHAND)) + ".";
    }

    private String nameOf(PlayerSeat seat) {
        if (seat == null) {
            return "brak";
        }
        return playerNames.getOrDefault(seat, seat.displayName());
    }
}
