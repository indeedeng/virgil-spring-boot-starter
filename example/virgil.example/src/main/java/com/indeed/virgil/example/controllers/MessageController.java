package com.indeed.virgil.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.indeed.virgil.example.models.CustomMessage;
import com.indeed.virgil.example.models.GenerateMessagePayload;
import com.indeed.virgil.example.utils.MessagePublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Component
public class MessageController {

    private final MessagePublisher messagePublisher;

    public MessageController(
        final MessagePublisher messagePublisher
    ) {
        this.messagePublisher = messagePublisher;
    }

    @PostMapping("/message")
    public CustomMessage create(
        @RequestBody final CustomMessage customMessage
    ) throws JsonProcessingException {
        messagePublisher.sendMessageToQueue(customMessage);

        return customMessage;
    }

    @PostMapping("/generatemessages")
    public boolean generateMany(
        @RequestBody final GenerateMessagePayload payload
    ) throws JsonProcessingException {

        //generate payload.num number of messages
        for (int i = 0; i < payload.getNum(); i++) {
            messagePublisher.sendMessageToQueue(new CustomMessage(i, UUID.randomUUID().toString()));
        }

        return true;
    }
}
