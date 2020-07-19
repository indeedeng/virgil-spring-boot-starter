package com.indeed.virgil.spring.boot.starter.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indeed.virgil.spring.boot.starter.util.VirgilMessageUtils;
import org.ietf.jgss.MessageProp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VirgilMessageUtilsTest {

    private VirgilMessageUtils virgilMessageUtils;

    @BeforeEach
    void setup() {
        virgilMessageUtils = new VirgilMessageUtils(new ObjectMapper());
    }

    @Nested
    class generateFingerprint {

        @Test
        void shouldReturnFingerprint() {
            //Arrange
            final byte[] body = "".getBytes();
            final MessageProperties messageProperties = new MessageProperties();
            messageProperties.setTimestamp(new Date(1234567L));
            final Message msg = new Message(body, messageProperties);

            //Act
            final String result = virgilMessageUtils.generateFingerprint(msg);

            //Assert
            assertThat(result).isEqualTo("a96c89461c301b20533cc981d253eda8");
        }

        @Test
        void shouldThrowExceptionOnNull() throws JsonProcessingException {
            //Arrange
            final ObjectMapper objectMapper = mock(ObjectMapper.class);

            when(objectMapper.writeValueAsBytes(any())).thenThrow(new JsonMappingException(""));

            final VirgilMessageUtils localInstance = new VirgilMessageUtils(objectMapper);

            //Act / Assert
            assertThatThrownBy(() -> localInstance.generateFingerprint(null))
                .isExactlyInstanceOf(RuntimeException.class);
        }
    }
}
