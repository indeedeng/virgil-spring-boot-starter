package com.indeed.virgil.spring.boot.starter.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TestMessageConverterService {

    @Mock
    private IMessageConverter messageConverter;
    private MessageConverterService messageConverterService;

    @BeforeEach
    void setup() {
        messageConverterService = new MessageConverterService(messageConverter);
    }

    @Nested
    class mapMessage {

        @Test
        void shouldCallConvertMessage() {
            //Arrange
            final String messageBody = "hello world, this is a message body 1...hello world, this is a message body 2...hello world, this is a message body 3...hello world, this is a message body 4...hello world, this is a message body 5...hello world, this is a message body 6...hello world, this is a message body 7...hello world, this is a message body 8...";
            final byte[] bodyBytes = messageBody.getBytes();
            final MessageProperties msgProps = MessagePropertiesBuilder.newInstance()
                .setMessageId("")
                .build();
            final Message msg = new Message(bodyBytes, msgProps);


            //Act
            messageConverterService.mapMessage(msg);

            //Assert
            verify(messageConverter, times(1)).convertMessage(msg);
        }
    }
}
