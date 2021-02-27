package com.indeed.virgil.spring.boot.starter.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for common methods related to VirgilMessage and amqp's Message object.
 */
@Component
public class VirgilMessageUtils {
    private static final Logger LOG = LoggerFactory.getLogger(VirgilMessageUtils.class);

    private static final String MESSAGE_PROP_SPLITTER = "|";
    private static final String MESSAGE_DIGEST_ALGORITHM = "MD5";
    private static final char[] HEX_CHARS =
        {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private final ThreadLocal<MessageDigest> messageDigestThreadLocal;

    public VirgilMessageUtils() {
        messageDigestThreadLocal = new ThreadLocal<>();
    }

    public String generateFingerprint(@Nullable final Message msg) {
        return internalGenerateFingerprint(msg);
    }

    private synchronized String internalGenerateFingerprint(@Nullable final Message msg) {

        final MessageDigest md = getMessageDigest();
        if (msg == null) {
            return String.valueOf(encodeHex(md.digest()));
        }

        final byte[] body = msg.getBody();
        if (body != null) {
            md.update(body);
        }

        if (msg.getMessageProperties() != null) {
            final byte[] messagePropertyBytes = getMessagePropertyBytes(msg.getMessageProperties());
            md.update(messagePropertyBytes);
        }

        return String.valueOf(encodeHex(md.digest()));
    }

    private MessageDigest getMessageDigest() {
        MessageDigest md = messageDigestThreadLocal.get();
        if (md != null) {
            return md;
        }

        try {
            md = MessageDigest.getInstance(MESSAGE_DIGEST_ALGORITHM);
        } catch (final NoSuchAlgorithmException ex) {
            LOG.error("Unable to find algorithm. [Algorithm: {}]", MESSAGE_DIGEST_ALGORITHM);
            throw new RuntimeException(ex);
        }

        messageDigestThreadLocal.set(md);

        return md;
    }

    /**
     * Converts array of bytes into hex characters.
     *
     * @param bytes
     * @return
     */
    private char[] encodeHex(byte[] bytes) {
        char[] chars = new char[32];
        for (int i = 0; i < chars.length; i = i + 2) {
            byte b = bytes[i / 2];
            chars[i] = HEX_CHARS[(b >>> 0x4) & 0xf];
            chars[i + 1] = HEX_CHARS[b & 0xf];
        }
        return chars;
    }

    /**
     * Converts messageProperties into a byte[] by appending it into a string and converting it to byte[]
     *
     * There are fields that are not included as part of the byte[] since they change based on other messages
     * Ignored Fields that are dependent on other messages:
     *   - deliveryTag
     *   - deliveryTagSet
     *   - messageCount
     * Ignored fields since they are not included in hashCode
     *   - consumerTag
     *   - consumerQueue
     *   - receivedDelay
     *   - receivedDeliveryMode
     *   - finalRetryForMessageWithNoId
     *   - publishSequenceNumber
     *   - lastInBatch
     *   - inferredArgumentType
     *   - targetMethod
     *   - targetBean
     * @param messageProperties
     * @return
     */
    private byte[] getMessagePropertyBytes(final MessageProperties messageProperties) {
        final StringBuilder sb = new StringBuilder();

        if (messageProperties.getHeaders() != null) {
            sb.append(messageProperties.getHeaders()).append(MESSAGE_PROP_SPLITTER);
        }

        if (messageProperties.getTimestamp() != null) {
            sb.append(messageProperties.getTimestamp()).append(MESSAGE_PROP_SPLITTER);
        }

        if (messageProperties.getMessageId() != null) {
            sb.append(messageProperties.getMessageId()).append(MESSAGE_PROP_SPLITTER);
        }

        if (messageProperties.getUserId() != null) {
            sb.append(messageProperties.getUserId()).append(MESSAGE_PROP_SPLITTER);
        }

        if (messageProperties.getAppId() != null) {
            sb.append(messageProperties.getAppId()).append(MESSAGE_PROP_SPLITTER);
        }

        if (messageProperties.getClusterId() != null) {
            sb.append(messageProperties.getClusterId()).append(MESSAGE_PROP_SPLITTER);
        }

        if (messageProperties.getType() != null) {
            sb.append(messageProperties.getType()).append(MESSAGE_PROP_SPLITTER);
        }

        if (messageProperties.getCorrelationId() != null) {
            sb.append(messageProperties.getCorrelationId()).append(MESSAGE_PROP_SPLITTER);
        }

        if (messageProperties.getReplyTo() != null) {
            sb.append(messageProperties.getReplyTo()).append(MESSAGE_PROP_SPLITTER);
        }

        if (messageProperties.getContentType() != null) {
            sb.append(messageProperties.getContentType()).append(MESSAGE_PROP_SPLITTER);
        }

        if (messageProperties.getContentEncoding() != null) {
            sb.append(messageProperties.getContentEncoding()).append(MESSAGE_PROP_SPLITTER);
        }

        sb.append(messageProperties.getContentLength()).append(MESSAGE_PROP_SPLITTER);

        if (messageProperties.getDeliveryMode() != null) {
            sb.append(messageProperties.getDeliveryMode()).append(MESSAGE_PROP_SPLITTER);
        }

        if (messageProperties.getExpiration() != null) {
            sb.append(messageProperties.getExpiration()).append(MESSAGE_PROP_SPLITTER);
        }

        if (messageProperties.getPriority() != null) {
            sb.append(messageProperties.getPriority()).append(MESSAGE_PROP_SPLITTER);
        }

        if (messageProperties.getRedelivered() != null) {
            sb.append(messageProperties.getRedelivered()).append(MESSAGE_PROP_SPLITTER);
        }

        if (messageProperties.getReceivedExchange() != null) {
            sb.append(messageProperties.getReceivedExchange()).append(MESSAGE_PROP_SPLITTER);
        }

        if (messageProperties.getReceivedRoutingKey() != null) {
            sb.append(messageProperties.getReceivedRoutingKey()).append(MESSAGE_PROP_SPLITTER);
        }

        if (messageProperties.getReceivedUserId() != null) {
            sb.append(messageProperties.getReceivedUserId()).append(MESSAGE_PROP_SPLITTER);
        }

        return sb.toString().getBytes();
    }

}
