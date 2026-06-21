package com.example.java_skat.network;

import com.example.java_skat.game.MultiplayerGameController;
import com.example.java_skat.game.PlayerSeat;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class MultiplayerGameServer implements AutoCloseable {
    public static final int REQUIRED_PLAYERS = 3;
    public static final int DEFAULT_PORT = 8080;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final List<ClientSession> clients = new CopyOnWriteArrayList<>();

    private ServerSocket serverSocket;
    private MultiplayerGameController game;

    public void start(int port) throws IOException {
        if (running.get()) {
            throw new IllegalStateException("Serwer już działa.");
        }
        serverSocket = new ServerSocket(port);
        running.set(true);
        executor.submit(this::acceptLoop);
    }

    public boolean isRunning() {
        return running.get();
    }

    @Override
    public void close() throws IOException {
        running.set(false);
        if (serverSocket != null) {
            serverSocket.close();
        }
        for (ClientSession client : clients) {
            client.closeQuietly();
        }
        executor.shutdownNow();
    }

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        MultiplayerGameServer server = new MultiplayerGameServer();
        server.start(port);
        System.out.println("Serwer Skata działa na localhost:" + port + ". Zamknij proces, aby zakończyć.");
    }

    private void acceptLoop() {
        while (running.get()) {
            try {
                Socket socket = serverSocket.accept();
                executor.submit(() -> initializeClient(socket));
            } catch (SocketException e) {
                if (running.get()) {
                    System.err.println("Błąd gniazda serwera: " + e.getMessage());
                }
                return;
            } catch (IOException e) {
                if (running.get()) {
                    System.err.println("Błąd accept(): " + e.getMessage());
                }
            }
        }
    }

    private void initializeClient(Socket socket) {
        ClientSession session = null;
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            Object firstMessage = in.readObject();
            if (!(firstMessage instanceof JoinGameMessage joinMessage)) {
                send(out, new ErrorMessage("Pierwsza wiadomość musi być JoinGameMessage."));
                socket.close();
                return;
            }

            synchronized (this) {
                if (clients.size() >= REQUIRED_PLAYERS) {
                    send(out, new ErrorMessage("Gra jest już pełna."));
                    socket.close();
                    return;
                }

                PlayerSeat seat = PlayerSeat.values()[clients.size()];
                session = new ClientSession(socket, out, in, seat, safeName(joinMessage.playerName(), seat));
                clients.add(session);
                broadcastWaitingRoom();
                startGameIfReady();
            }

            listen(session);
        } catch (EOFException | SocketException e) {
            // klient rozłączył się podczas dołączania
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Błąd klienta: " + e.getMessage());
        } finally {
            if (session != null) {
                removeClient(session);
            } else {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private void listen(ClientSession session) throws IOException, ClassNotFoundException {
        while (running.get() && !session.socket.isClosed()) {
            Object object = session.in.readObject();
            if (object instanceof SkatMessage message) {
                handleMessage(session, message);
            }
        }
    }

    private synchronized void handleMessage(ClientSession session, SkatMessage message) {
        try {
            if (game == null) {
                session.send(new ErrorMessage("Gra jeszcze nie wystartowała. Czekamy na komplet graczy."));
                return;
            }

            switch (message) {
                case BidMessage ignored -> game.bid(session.seat);
                case PassMessage ignored -> game.pass(session.seat);
                case TakeSkatMessage ignored -> game.takeSkatBeforeContract(session.seat);
                case ChooseGameWithoutSkatMessage ignored -> game.chooseGameWithoutTakingSkat(session.seat);
                case ConfirmContractMessage confirm -> game.confirmContract(session.seat, confirm.contract().toGameContract());
                case DiscardCardMessage discard -> game.discardSelectedCardToSkat(session.seat, discard.card().toCore());
                case PlayCardMessage play -> game.playCard(session.seat, play.card().toCore());
                case NewDealMessage ignored -> game.startNewDeal();
                case JoinGameMessage ignored -> session.send(new ErrorMessage("Już jesteś połączony z grą."));
                case WaitingRoomMessage ignored -> session.send(new ErrorMessage("WaitingRoomMessage wysyła tylko serwer."));
                case GameStateMessage ignored -> session.send(new ErrorMessage("GameStateMessage wysyła tylko serwer."));
                case ErrorMessage ignored -> {
                }
            }

            broadcastGameState();
        } catch (RuntimeException | IOException e) {
            try {
                session.send(new ErrorMessage("Błąd obsługi akcji: " + e.getMessage()));
            } catch (IOException ignored) {
            }
        }
    }

    private synchronized void startGameIfReady() throws IOException {
        if (game != null || clients.size() < REQUIRED_PLAYERS) {
            return;
        }

        Map<PlayerSeat, String> names = new EnumMap<>(PlayerSeat.class);
        for (ClientSession client : clients) {
            names.put(client.seat, client.playerName);
        }

        game = new MultiplayerGameController(names);
        broadcastGameState();
    }

    private synchronized void broadcastWaitingRoom() throws IOException {
        WaitingRoomMessage message = new WaitingRoomMessage(
                clients.stream().map(client -> client.playerName).toList(),
                clients.size(),
                REQUIRED_PLAYERS,
                clients.size() >= REQUIRED_PLAYERS,
                clients.size() >= REQUIRED_PLAYERS
                        ? "Komplet graczy. Rozpoczynamy rozdanie."
                        : "Czekamy na graczy na localhost. Uruchom kolejne instancje i wybierz Dołącz do gry."
        );

        for (ClientSession client : clients) {
            client.send(message);
        }
    }

    private synchronized void broadcastGameState() throws IOException {
        if (game == null) {
            return;
        }

        for (ClientSession client : clients) {
            client.send(MessageMapper.toMessage(game.snapshotFor(client.seat)));
        }
    }

    private synchronized void removeClient(ClientSession session) {
        clients.remove(session);
        session.closeQuietly();
        if (running.get()) {
            try {
                if (game == null) {
                    broadcastWaitingRoom();
                } else {
                    for (ClientSession client : clients) {
                        client.send(new ErrorMessage("Gracz " + session.playerName + " rozłączył się. Uruchom grę od nowa."));
                    }
                }
            } catch (IOException ignored) {
            }
        }
    }

    private static void send(ObjectOutputStream out, SkatMessage message) throws IOException {
        synchronized (out) {
            out.writeObject(message);
            out.flush();
        }
    }

    private static String safeName(String rawName, PlayerSeat seat) {
        if (rawName == null || rawName.isBlank()) {
            return seat.displayName();
        }
        return rawName.trim();
    }

    private static final class ClientSession {
        private final Socket socket;
        private final ObjectOutputStream out;
        private final ObjectInputStream in;
        private final PlayerSeat seat;
        private final String playerName;

        private ClientSession(Socket socket, ObjectOutputStream out, ObjectInputStream in, PlayerSeat seat, String playerName) {
            this.socket = socket;
            this.out = out;
            this.in = in;
            this.seat = seat;
            this.playerName = playerName;
        }

        private void send(SkatMessage message) throws IOException {
            MultiplayerGameServer.send(out, message);
        }

        private void closeQuietly() {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
