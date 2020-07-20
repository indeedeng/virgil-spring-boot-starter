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

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class TestDefaultMessageConverter {

    //copied from MessageConverterService
    private static final String MESSAGE_HEADER_EXCEPTION = "x-exception-message";
    private static final String MESSAGE_HEADER_ORIGINAL_ROUTING_KEY = "x-original-routingKey";
    private static final String MESSAGE_HEADER_ORIGINAL_EXCHANGE = "x-original-exchange";

    private VirgilMessageUtils virgilMessageUtils;
    private DefaultMessageConverter defaultMessageConverter;

    @BeforeEach
    void setup() {
        virgilMessageUtils = new VirgilMessageUtils();
        defaultMessageConverter = new DefaultMessageConverter(virgilMessageUtils);
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
        final VirgilMessage result = defaultMessageConverter.convertMessage(msg);

        //Assert
        assertThat(result).extracting("body").isEqualTo(truncatedMessageBody);
    }

    @Test
    void shouldPassNonCustomHeaderInMessage() {
        //Arrange
        final String messageBody = "hello world, this is a message body 1...hello world, this is a message body 2...hello world, this is a message body 3...hello world, this is a message body 4...hello world, this is a message body 5...hello world, this is a message body 6...hello world, this is a message body 7...hello world, this is a message body 8...";
        final byte[] bodyBytes = messageBody.getBytes();
        final MessageProperties msgProps = MessagePropertiesBuilder.newInstance()
            .setMessageId("uniqueMessageId")
            .setHeader("NEW", "1")
            .build();
        final Message msg = new Message(bodyBytes, msgProps);

        //Act
        final VirgilMessage result = defaultMessageConverter.convertMessage(msg);

        //Assert
        assertThat(result.getHeaders()).contains(entry("NEW", "1"));
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
        final VirgilMessage result = defaultMessageConverter.convertMessage(msg);

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
        final VirgilMessage result = defaultMessageConverter.convertMessage(msg);

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
        final VirgilMessage result = defaultMessageConverter.convertMessage(msg);

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

        //Act
        final VirgilMessage result = defaultMessageConverter.convertMessage(msg);

        //Assert
        assertThat(result.getFingerprint()).isEqualTo("dcefe73ee0ce7ac11639160c6175e9c9");
    }

    @Test
    void shouldSetIdFromMessageId() {
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
        final VirgilMessage result = defaultMessageConverter.convertMessage(msg);

        //Assert
        assertThat(result.getId()).isEqualTo(String.format("i_%s", "uniqueMessageId"));
    }

    @Test
    void shouldSetIdFromFingerprint() {
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
        final VirgilMessage result = defaultMessageConverter.convertMessage(msg);

        //Assert
        assertThat(result.getId()).isEqualTo(String.format("f_%s", "4838233e7a60c6e03659e4e201f0c89e"));
    }
}
