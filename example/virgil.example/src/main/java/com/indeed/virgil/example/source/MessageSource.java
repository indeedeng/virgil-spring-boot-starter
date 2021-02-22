package com.indeed.virgil.example.source;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface MessageSource {
    @Output("virgilExchangeChannel")
    MessageChannel virgilExchange();
}
