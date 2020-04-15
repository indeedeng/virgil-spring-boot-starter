package com.indeed.virgil.spring.boot.starter.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indeed.virgil.spring.boot.starter.models.ImmutableVirgilMessage;
import com.indeed.virgil.spring.boot.starter.models.VirgilMessage;
import org.springframework.amqp.core.Message;
import org.springframework.util.DigestUtils;

import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class DefaultMessageConverter implements IMessageConverter {

    private static final int MAX_DISPLAY_STR_LEN = 256;

    private static final String MESSAGE_HEADER_EXCEPTION = "x-exception-message";
    private static final String MESSAGE_HEADER_ORIGINAL_ROUTING_KEY = "x-original-routingKey";
    private static final String MESSAGE_HEADER_ORIGINAL_EXCHANGE = "x-original-exchange";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public DefaultMessageConverter() {

    }

    @Override
    public VirgilMessage convertMessage(Message msg) {
        final ImmutableVirgilMessage.Builder virgilMessageBuilder = ImmutableVirgilMessage.builder()
            .setBody(new String(msg.getBody(), 0, Math.min(msg.getBody().length, MAX_DISPLAY_STR_LEN), UTF_8));

        final Map<String, Object> messageHeaders = msg.getMessageProperties().getHeaders();

        for (Map.Entry<String, Object> messageHeader : messageHeaders.entrySet()) {
            if (!messageHeader.getKey().startsWith("x-")) {
                virgilMessageBuilder.putHeaders(messageHeader.getKey(), messageHeader.getValue());
            }
        }

        // keep an allowed list of exception headers so we don't accidentally include unexpected stuff
        if (messageHeaders.containsKey(MESSAGE_HEADER_EXCEPTION)) {
            final Object messageHeaderException = messageHeaders.get(MESSAGE_HEADER_EXCEPTION);
            virgilMessageBuilder.putHeaders(MESSAGE_HEADER_EXCEPTION, messageHeaderException);
        }

        if (messageHeaders.containsKey(MESSAGE_HEADER_ORIGINAL_ROUTING_KEY)) {
            final Object originalRoutingKey = messageHeaders.get(MESSAGE_HEADER_ORIGINAL_ROUTING_KEY);
            virgilMessageBuilder.putHeaders(MESSAGE_HEADER_ORIGINAL_ROUTING_KEY, originalRoutingKey);
        }

        if (messageHeaders.containsKey(MESSAGE_HEADER_ORIGINAL_EXCHANGE)) {
            final Object originalExchange = messageHeaders.get(MESSAGE_HEADER_ORIGINAL_EXCHANGE);
            virgilMessageBuilder.putHeaders(MESSAGE_HEADER_ORIGINAL_EXCHANGE, originalExchange);
        }

        try {
            final String fingerprint = DigestUtils.md5DigestAsHex(OBJECT_MAPPER.writeValueAsBytes(msg));
            virgilMessageBuilder.setFingerprint(fingerprint);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return virgilMessageBuilder.build();
    }
}
