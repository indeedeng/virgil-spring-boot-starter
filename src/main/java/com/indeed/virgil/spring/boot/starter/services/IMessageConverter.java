package com.indeed.virgil.spring.boot.starter.services;

import com.indeed.virgil.spring.boot.starter.models.VirgilMessage;
import org.springframework.amqp.core.Message;

public interface IMessageConverter {
    VirgilMessage convertMessage(final Message msg);
}
