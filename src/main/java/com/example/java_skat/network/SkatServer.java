package com.example.java_skat.network;

import com.example.java_skat.game.LocalGameController;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SkatServer {
    public static final int DEFAULT_PORT = 8080;

    private SkatServer() {
    }

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        ExecutorService clients = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Serwer Skata działa na porcie " + port);
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                clients.submit(() -> handleClient(socket));
            }
        } finally {
            clients.shutdownNow();
        }
    }

    private static void handleClient(Socket socket) {
        LocalGameController game = new LocalGameController();

        try (socket;
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            send(out, MessageMapper.toMessage(game.snapshot()));

            while (!socket.isClosed()) {
                Object object = in.readObject();
                if (object instanceof PlayCardMessage playCardMessage) {
                    game.playCard(playCardMessage.card().toCore());
                    send(out, MessageMapper.toMessage(game.snapshot()));
                } else if (object instanceof JoinGameMessage joinGameMessage) {
                    send(out, new ErrorMessage("Dołączono jako: " + joinGameMessage.playerName()));
                    send(out, MessageMapper.toMessage(game.snapshot()));
                }
            }
        } catch (EOFException | SocketException e) {
            System.out.println("Klient rozłączony.");
        } catch (IOException | ClassNotFoundException | IllegalArgumentException e) {
            System.err.println("Błąd klienta: " + e.getMessage());
        }
    }

    private static synchronized void send(ObjectOutputStream out, SkatMessage message) throws IOException {
        out.writeObject(message);
        out.flush();
    }
}
