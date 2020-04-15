package com.indeed.virgil.spring.boot.starter.services;

import com.indeed.virgil.spring.boot.starter.models.VirgilMessage;
import org.springframework.amqp.core.Message;

public class MessageConverterService {

    private final IMessageConverter messageConverter;

    public MessageConverterService(
        final IMessageConverter messageConverter
    ) {
        this.messageConverter = messageConverter;
    }

    public VirgilMessage mapMessage(final Message msg) {
        return messageConverter.convertMessage(msg);
    }
}
