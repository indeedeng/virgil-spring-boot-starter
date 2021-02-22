package com.indeed.virgil.example.controllers;

import com.indeed.virgil.example.models.CustomMessage;
import com.indeed.virgil.example.models.GenerateMessagePayload;
import com.indeed.virgil.example.source.MessageSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@EnableBinding(MessageSource.class)
public class MessageController {

    @Autowired
    MessageSource messageSource;

    @PostMapping("/message")
    public CustomMessage create(@RequestBody final CustomMessage customMessage) {

        messageSource.virgilExchange().send(MessageBuilder.withPayload(customMessage).build());

        return customMessage;
    }

    @PostMapping("/generatemessages")
    public boolean generateMany(@RequestBody final GenerateMessagePayload payload) {

        //generate payload.num number of messages
        for (int i = 0; i < payload.getNum(); i++) {
            messageSource.virgilExchange().send(MessageBuilder.withPayload(new CustomMessage(i, UUID.randomUUID().toString())).build());
        }

        return true;
    }
}
