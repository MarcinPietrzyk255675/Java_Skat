package com.example.java_skat.network;

import java.io.IOException;

public final class SkatServer {
    private SkatServer() {
    }

    public static void main(String[] args) throws IOException {
        MultiplayerGameServer.main(args);
    }
}
