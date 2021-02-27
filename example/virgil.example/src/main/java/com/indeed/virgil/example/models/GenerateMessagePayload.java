package com.indeed.virgil.example.models;

public class GenerateMessagePayload {
    private final long num;
    private final boolean sendToDlq;

    public GenerateMessagePayload(
        final long num,
        final boolean sendToDlq
    ) {
        this.num = num;
        this.sendToDlq = sendToDlq;
    }

    public long getNum() {
        return num;
    }

    public boolean getSendToDlq() {
        return this.sendToDlq;
    }
}
