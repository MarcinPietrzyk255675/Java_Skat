package com.example.java_skat.network;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class SkatClientConnection implements AutoCloseable {
    private final AtomicBoolean running = new AtomicBoolean(false);

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Thread listenerThread;

    public void connect(String host, int port, Consumer<SkatMessage> onMessage) throws IOException {
        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());

        running.set(true);
        listenerThread = new Thread(() -> listen(onMessage), "skat-client-listener");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public synchronized void send(SkatMessage message) throws IOException {
        if (!running.get()) {
            throw new IOException("Połączenie z serwerem nie jest aktywne.");
        }
        out.writeObject(message);
        out.flush();
    }

    @Override
    public void close() throws IOException {
        running.set(false);
        if (socket != null) {
            socket.close();
        }
    }

    private void listen(Consumer<SkatMessage> onMessage) {
        while (running.get()) {
            try {
                Object object = in.readObject();
                if (object instanceof SkatMessage message) {
                    onMessage.accept(message);
                }
            } catch (EOFException | SocketException e) {
                running.set(false);
            } catch (IOException | ClassNotFoundException e) {
                running.set(false);
                onMessage.accept(new ErrorMessage("Błąd komunikacji: " + e.getMessage()));
            }
        }
    }
}
