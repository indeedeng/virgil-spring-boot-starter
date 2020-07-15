package com.indeed.virgil.spring.boot.starter.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

@Component
public class VirgilMessageUtils {

    private final ObjectMapper objectMapper;

    public VirgilMessageUtils(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String generateFingerprint(final Message msg) {
        try {
            return DigestUtils.md5DigestAsHex(objectMapper.writeValueAsBytes(msg));
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
