package com.example.java_skat.network;

import java.io.Serializable;

public sealed interface SkatMessage extends Serializable
        permits JoinGameMessage, PlayCardMessage, GameStateMessage, ErrorMessage {
}
