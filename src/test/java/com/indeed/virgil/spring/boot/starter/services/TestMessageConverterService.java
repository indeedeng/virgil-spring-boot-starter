package com.indeed.virgil.spring.boot.starter.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indeed.virgil.spring.boot.starter.models.VirgilMessage;
import com.indeed.virgil.spring.boot.starter.util.VirgilMessageUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.util.DigestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class TestMessageConverterService {

    //copied from MessageConverterService
    private static final String MESSAGE_HEADER_EXCEPTION = "x-exception-message";
    private static final String MESSAGE_HEADER_ORIGINAL_ROUTING_KEY = "x-original-routingKey";
    private static final String MESSAGE_HEADER_ORIGINAL_EXCHANGE = "x-original-exchange";

    private VirgilMessageUtils virgilMessageUtils;
    private DefaultMessageConverter defaultMessageConverter;
    private MessageConverterService messageConverterService;

    @BeforeEach
    void setup() {
        virgilMessageUtils = new VirgilMessageUtils(new ObjectMapper());
        defaultMessageConverter = new DefaultMessageConverter(virgilMessageUtils);
        messageConverterService = new MessageConverterService(defaultMessageConverter);
    }

    @Test
    void shouldCutoffTextIfTooLong() {
        //Arrange
        final String messageBody = "hello world, this is a message body 1...hello world, this is a message body 2...hello world, this is a message body 3...hello world, this is a message body 4...hello world, this is a message body 5...hello world, this is a message body 6...hello world, this is a message body 7...hello world, this is a message body 8...";
        final String truncatedMessageBody = messageBody.substring(0, Math.min(messageBody.length(), 256));
        final byte[] bodyBytes = messageBody.getBytes();
        final MessageProperties msgProps = MessagePropertiesBuilder.newInstance()
            .setMessageId("uniqueMessageId")
            .setHeader(MESSAGE_HEADER_EXCEPTION, "this is why the message failed")
            .setHeader(MESSAGE_HEADER_ORIGINAL_ROUTING_KEY, "orig-routing-key")
            .setHeader(MESSAGE_HEADER_ORIGINAL_EXCHANGE, "someExchange")
            .build();
        final Message msg = new Message(bodyBytes, msgProps);

        //Act
        final VirgilMessage result = messageConverterService.mapMessage(msg);

        //Assert
        assertThat(result).extracting("body").isEqualTo(truncatedMessageBody);
    }

    @Test
    void shouldAllowExceptionHeader() {
        //Arrange
        final String messageBody = "hello world, this is a message body 1...hello world, this is a message body 2...hello world, this is a message body 3...hello world, this is a message body 4...hello world, this is a message body 5...hello world, this is a message body 6...hello world, this is a message body 7...hello world, this is a message body 8...";
        final byte[] bodyBytes = messageBody.getBytes();
        final MessageProperties msgProps = MessagePropertiesBuilder.newInstance()
            .setMessageId("uniqueMessageId")
            .setHeader(MESSAGE_HEADER_EXCEPTION, "this is why the message failed")
            .setHeader(MESSAGE_HEADER_ORIGINAL_ROUTING_KEY, "orig-routing-key")
            .setHeader(MESSAGE_HEADER_ORIGINAL_EXCHANGE, "someExchange")
            .build();
        final Message msg = new Message(bodyBytes, msgProps);

        //Act
        final VirgilMessage result = messageConverterService.mapMessage(msg);

        //Assert
        assertThat(result.getHeaders()).contains(entry(MESSAGE_HEADER_EXCEPTION, "this is why the message failed"));
    }

    @Test
    void shouldAllowOriginalRoutingKeyHeader() {
        //Arrange
        final String messageBody = "hello world, this is a message body 1...hello world, this is a message body 2...hello world, this is a message body 3...hello world, this is a message body 4...hello world, this is a message body 5...hello world, this is a message body 6...hello world, this is a message body 7...hello world, this is a message body 8...";
        final byte[] bodyBytes = messageBody.getBytes();
        final MessageProperties msgProps = MessagePropertiesBuilder.newInstance()
            .setMessageId("uniqueMessageId")
            .setHeader(MESSAGE_HEADER_EXCEPTION, "this is why the message failed")
            .setHeader(MESSAGE_HEADER_ORIGINAL_ROUTING_KEY, "orig-routing-key")
            .setHeader(MESSAGE_HEADER_ORIGINAL_EXCHANGE, "someExchange")
            .build();
        final Message msg = new Message(bodyBytes, msgProps);

        //Act
        final VirgilMessage result = messageConverterService.mapMessage(msg);

        //Assert
        assertThat(result.getHeaders()).contains(entry(MESSAGE_HEADER_ORIGINAL_ROUTING_KEY, "orig-routing-key"));
    }

    @Test
    void shouldAllowExchangeHeader() {
        //Arrange
        final String messageBody = "hello world, this is a message body 1...hello world, this is a message body 2...hello world, this is a message body 3...hello world, this is a message body 4...hello world, this is a message body 5...hello world, this is a message body 6...hello world, this is a message body 7...hello world, this is a message body 8...";
        final byte[] bodyBytes = messageBody.getBytes();
        final MessageProperties msgProps = MessagePropertiesBuilder.newInstance()
            .setMessageId("uniqueMessageId")
            .setHeader(MESSAGE_HEADER_EXCEPTION, "this is why the message failed")
            .setHeader(MESSAGE_HEADER_ORIGINAL_ROUTING_KEY, "orig-routing-key")
            .setHeader(MESSAGE_HEADER_ORIGINAL_EXCHANGE, "someExchange")
            .build();
        final Message msg = new Message(bodyBytes, msgProps);

        //Act
        final VirgilMessage result = messageConverterService.mapMessage(msg);

        //Assert
        assertThat(result.getHeaders()).contains(entry(MESSAGE_HEADER_ORIGINAL_EXCHANGE, "someExchange"));
    }

    @Test
    void shouldSetFingerPrint() throws JsonProcessingException {
        //Arrange
        final String messageBody = "hello world, this is a message body 1...hello world, this is a message body 2...hello world, this is a message body 3...hello world, this is a message body 4...hello world, this is a message body 5...hello world, this is a message body 6...hello world, this is a message body 7...hello world, this is a message body 8...";
        final byte[] bodyBytes = messageBody.getBytes();
        final MessageProperties msgProps = MessagePropertiesBuilder.newInstance()
            .setMessageId("uniqueMessageId")
            .setHeader(MESSAGE_HEADER_EXCEPTION, "this is why the message failed")
            .setHeader(MESSAGE_HEADER_ORIGINAL_ROUTING_KEY, "orig-routing-key")
            .setHeader(MESSAGE_HEADER_ORIGINAL_EXCHANGE, "someExchange")
            .build();
        final Message msg = new Message(bodyBytes, msgProps);

        final String expectedFingerprint = DigestUtils.md5DigestAsHex(new ObjectMapper().writeValueAsBytes(msg));

        //Act
        final VirgilMessage result = messageConverterService.mapMessage(msg);

        //Assert
        assertThat(result.getFingerprint()).isEqualTo(expectedFingerprint);
    }

    @Test
    void shouldSetIdFromMessageId() throws JsonProcessingException {
        //Arrange
        final String messageBody = "hello world, this is a message body 1...hello world, this is a message body 2...hello world, this is a message body 3...hello world, this is a message body 4...hello world, this is a message body 5...hello world, this is a message body 6...hello world, this is a message body 7...hello world, this is a message body 8...";
        final byte[] bodyBytes = messageBody.getBytes();
        final MessageProperties msgProps = MessagePropertiesBuilder.newInstance()
            .setMessageId("uniqueMessageId")
            .setHeader(MESSAGE_HEADER_EXCEPTION, "this is why the message failed")
            .setHeader(MESSAGE_HEADER_ORIGINAL_ROUTING_KEY, "orig-routing-key")
            .setHeader(MESSAGE_HEADER_ORIGINAL_EXCHANGE, "someExchange")
            .build();
        final Message msg = new Message(bodyBytes, msgProps);

        //Act
        final VirgilMessage result = messageConverterService.mapMessage(msg);

        //Assert
        assertThat(result.getId()).isEqualTo(String.format("i_%s", "uniqueMessageId"));
    }

    @Test
    void shouldSetIdFromFingerprint() throws JsonProcessingException {
        //Arrange
        final String messageBody = "hello world, this is a message body 1...hello world, this is a message body 2...hello world, this is a message body 3...hello world, this is a message body 4...hello world, this is a message body 5...hello world, this is a message body 6...hello world, this is a message body 7...hello world, this is a message body 8...";
        final byte[] bodyBytes = messageBody.getBytes();
        final MessageProperties msgProps = MessagePropertiesBuilder.newInstance()
            .setMessageId("")
            .setHeader(MESSAGE_HEADER_EXCEPTION, "this is why the message failed")
            .setHeader(MESSAGE_HEADER_ORIGINAL_ROUTING_KEY, "orig-routing-key")
            .setHeader(MESSAGE_HEADER_ORIGINAL_EXCHANGE, "someExchange")
            .build();
        final Message msg = new Message(bodyBytes, msgProps);

        //Act
        final VirgilMessage result = messageConverterService.mapMessage(msg);

        //Assert
        assertThat(result.getId()).isEqualTo(String.format("f_%s", "685a8fb53c4297fe8431c3df61fd8e66"));
    }
}
