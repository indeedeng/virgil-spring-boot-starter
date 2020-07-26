package com.indeed.virgil.spring.boot.starter.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.indeed.virgil.spring.boot.starter.util.VirgilMessageUtils;
import com.rabbitmq.client.LongString;
import com.rabbitmq.client.impl.LongStringHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import static org.assertj.core.api.Assertions.assertThat;

public class VirgilMessageUtilsTest {

    private VirgilMessageUtils virgilMessageUtils;

    @BeforeEach
    void setup() {
        virgilMessageUtils = new VirgilMessageUtils();
    }

    @Nested
    class generateFingerprint {

        @Test
        void shouldReturnFingerprint() throws JsonProcessingException {
            //Arrange
            final byte[] body = "".getBytes();
            final MessageProperties messageProperties = new MessageProperties();
            messageProperties.setHeader("uniqueKey", "1");
            final Message msg = new Message(body, messageProperties);

            //Act
            final String result = virgilMessageUtils.generateFingerprint(msg);

            //Assert
            assertThat(result).isEqualTo("91c85dabc466d4d697b877245911c3f9");
        }

        @Test
        void shouldReturnFingerprintForMessageWithNullBody() throws JsonProcessingException {
            //Arrange
            final MessageProperties messageProperties = new MessageProperties();
            messageProperties.setHeader("uniqueKey", "1");
            final Message msg = new Message(null, messageProperties);

            //Act
            final String result = virgilMessageUtils.generateFingerprint(msg);

            //Assert
            assertThat(result).isEqualTo("91c85dabc466d4d697b877245911c3f9");
        }

        @Test
        void shouldReturnFingerprintForMessageWithNoProperties() throws JsonProcessingException {
            //Arrange
            final byte[] body = "asfdafdas".getBytes();
            final Message msg = new Message(body, null);

            //Act
            final String result = virgilMessageUtils.generateFingerprint(msg);

            //Assert
            assertThat(result).isEqualTo("81f2dd8b7cf2ef9e7b3210c6741766b7");
        }

        @Test
        void shouldReturnValidFingerprintWhenMessageIsNull() throws JsonProcessingException {
            //Arrange

            //Act
            final String result = virgilMessageUtils.generateFingerprint(null);

            //Assert
            assertThat(result).isEqualTo("d41d8cd98f00b204e9800998ecf8427e");
        }

        @Test
        void shouldHandleNonSerializedItemInHeader() throws JsonProcessingException {
            //Arrange
            final byte[] body = "".getBytes();
            final MessageProperties messageProperties = new MessageProperties();
            messageProperties.setHeader("uniqueKey", "12312321");

            final LongString item = LongStringHelper.asLongString("");
            messageProperties.setHeader("item", item);

            final Message msg = new Message(body, messageProperties);

            //Act
            final String result = virgilMessageUtils.generateFingerprint(msg);

            //Assert
            assertThat(result).isEqualTo("3154ebc077ff7b725955b065f71732f3");
        }
    }
}
