package com.indeed.virgil.spring.boot.starter.utils;

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
        void shouldReturnFingerprint() {
            //Arrange
            final byte[] body = "".getBytes();
            final MessageProperties messageProperties = new MessageProperties();
            messageProperties.setHeader("uniqueKey", "1");
            final Message msg = new Message(body, messageProperties);

            //Act
            final String result = virgilMessageUtils.generateFingerprint(msg);

            //Assert
            assertThat(result).isEqualTo("3e1bc27b4db8f518e7ebbb2da9912ec5");
        }

        @Test
        void shouldReturnFingerprintForMessageWithNullBody() {
            //Arrange
            final MessageProperties messageProperties = new MessageProperties();
            messageProperties.setHeader("uniqueKey", "1");
            final Message msg = new Message(null, messageProperties);

            //Act
            final String result = virgilMessageUtils.generateFingerprint(msg);

            //Assert
            assertThat(result).isEqualTo("3e1bc27b4db8f518e7ebbb2da9912ec5");
        }

        @Test
        void shouldReturnFingerprintForMessageWithNoProperties() {
            //Arrange
            final byte[] body = "asfdafdas".getBytes();
            final Message msg = new Message(body, null);

            //Act
            final String result = virgilMessageUtils.generateFingerprint(msg);

            //Assert
            assertThat(result).isEqualTo("81f2dd8b7cf2ef9e7b3210c6741766b7");
        }

        @Test
        void shouldReturnValidFingerprintWhenMessageIsNull() {
            //Arrange

            //Act
            final String result = virgilMessageUtils.generateFingerprint(null);

            //Assert
            assertThat(result).isEqualTo("d41d8cd98f00b204e9800998ecf8427e");
        }

        @Test
        void shouldHandleNonSerializedItemInHeader() {
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
            assertThat(result).isEqualTo("c79fc54eb5a4060d41184c804e0fc84e");
        }

        @Test
        void shouldNotSerializeDeliveryTagAsPartOfFingerprint() {
            //Arrange
            final byte[] body = "{ \"uniqueId\": 55 }".getBytes();

            final LongString item = LongStringHelper.asLongString("");


            final MessageProperties messageProperties1 = new MessageProperties();
            messageProperties1.setDeliveryTag(7L);
            messageProperties1.setHeader("trackingCode", "AF190B");
            messageProperties1.setHeader("item", item);


            final MessageProperties messageProperties2 = new MessageProperties();
            messageProperties2.setDeliveryTag(55L);
            messageProperties2.setHeader("trackingCode", "AF190B");
            messageProperties2.setHeader("item", item);

            final Message msg1 = new Message(body, messageProperties1);
            final Message msg2 = new Message(body, messageProperties2);

            //Act
            final String result1 = virgilMessageUtils.generateFingerprint(msg1);
            final String result2 = virgilMessageUtils.generateFingerprint(msg2);

            //Assert
            assertThat(result1).isEqualTo("6fb4d910a8c82397e1892a142ab5ffb1");
            assertThat(result2).isEqualTo("6fb4d910a8c82397e1892a142ab5ffb1");
            assertThat(result1).isEqualTo(result2);
        }

    }
}
