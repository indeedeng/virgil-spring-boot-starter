package com.indeed.virgil.spring.boot.starter.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indeed.virgil.spring.boot.starter.util.VirgilMessageUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

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
            final Message msg = new Message(body, new MessageProperties());

            //Act
            final String result = virgilMessageUtils.generateFingerprint(msg);

            //Assert
            assertThat(result).isEqualTo("e168e2c084bd946f2ab2343795488834");
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
