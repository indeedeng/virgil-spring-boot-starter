package com.indeed.virgil.example.controllers;

import com.indeed.virgil.example.models.CustomMessage;
import com.indeed.virgil.example.models.GenerateMessagePayload;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {

    @PostMapping("/message")
    public CustomMessage create(@RequestBody final CustomMessage customMessage) {


        return customMessage;
    }

    @PostMapping("/generatemessages")
    public boolean generateMany(@RequestBody final GenerateMessagePayload payload) {

        //generate payload.num number of messages
        for (int i = 0; i < payload.getNum(); i++) {
            //add message
        }

        return true;
    }
}
