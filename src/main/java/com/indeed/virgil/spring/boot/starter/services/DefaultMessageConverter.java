package com.indeed.virgil.spring.boot.starter.services;

import com.indeed.virgil.spring.boot.starter.models.ImmutableVirgilMessage;
import com.indeed.virgil.spring.boot.starter.models.VirgilMessage;
import com.indeed.virgil.spring.boot.starter.util.VirgilMessageUtils;
import org.springframework.amqp.core.Message;
import org.springframework.lang.Nullable;

import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class DefaultMessageConverter implements IMessageConverter {

    private static final int MAX_DISPLAY_STR_LEN = 256;

    private static final String MESSAGE_HEADER_EXCEPTION = "x-exception-message";
    private static final String MESSAGE_HEADER_ORIGINAL_ROUTING_KEY = "x-original-routingKey";
    private static final String MESSAGE_HEADER_ORIGINAL_EXCHANGE = "x-original-exchange";

    private final VirgilMessageUtils virgilMessageUtils;

    public DefaultMessageConverter(final VirgilMessageUtils virgilMessageUtils) {
        this.virgilMessageUtils = virgilMessageUtils;
    }

    @Override
    public VirgilMessage convertMessage(final Message msg) {
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

        final String fingerprint = virgilMessageUtils.generateFingerprint(msg);
        virgilMessageBuilder.setFingerprint(fingerprint);

        //set the id with a value that is not dependent on the server, this will allow us to reference the same message
        // in the queue without the message cache
        final String potentialMessageId = msg.getMessageProperties().getMessageId();
        if (!isEmpty(potentialMessageId)) {
            virgilMessageBuilder.setId(String.format("i_%s", potentialMessageId));
        } else {
            virgilMessageBuilder.setId(String.format("f_%s", fingerprint));
        }
        return virgilMessageBuilder.build();
    }

    private boolean isEmpty(@Nullable final String s) {
        return s == null || s.isEmpty();
    }
}
