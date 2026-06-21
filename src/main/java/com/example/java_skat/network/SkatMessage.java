package com.example.java_skat.network;

import java.io.Serializable;

public sealed interface SkatMessage extends Serializable
        permits JoinGameMessage,
        WaitingRoomMessage,
        GameStateMessage,
        ErrorMessage,
        BidMessage,
        PassMessage,
        TakeSkatMessage,
        ChooseGameWithoutSkatMessage,
        ConfirmContractMessage,
        DiscardCardMessage,
        PlayCardMessage,
        NewDealMessage {
}
