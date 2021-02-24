package com.indeed.virgil.example.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indeed.virgil.example.config.RabbitMqConfig;
import com.indeed.virgil.example.models.CustomMessage;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessagePublisher {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RabbitTemplate rabbitTemplate;

    public MessagePublisher(final RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessageToQueue(final CustomMessage customMessage) throws JsonProcessingException {
        final String jsonString = objectMapper.writeValueAsString(customMessage);
        final Message message = new Message(jsonString.getBytes(), new MessageProperties());
        this.rabbitTemplate.convertAndSend(RabbitMqConfig.TOPIC_EXCHANGE_NAME, RabbitMqConfig.ROUTING_KEY, message);
    }

    public void sendMessageToDlq(final CustomMessage customMessage) throws JsonProcessingException {
        final String jsonString = objectMapper.writeValueAsString(customMessage);
        final Message message = new Message(jsonString.getBytes(), new MessageProperties());
        this.rabbitTemplate.convertAndSend(RabbitMqConfig.TOPIC_EXCHANGE_NAME, RabbitMqConfig.DLQ_ROUTING_KEY, message);
    }
}
