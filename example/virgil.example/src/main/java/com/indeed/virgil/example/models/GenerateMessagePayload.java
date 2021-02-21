package com.indeed.virgil.example.models;

public class GenerateMessagePayload {
    private final long num;

    public GenerateMessagePayload(final long num) {
        this.num = num;
    }

    public long getNum() {
        return num;
    }
}
