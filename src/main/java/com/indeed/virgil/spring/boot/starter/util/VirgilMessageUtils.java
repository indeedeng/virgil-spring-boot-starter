package com.indeed.virgil.spring.boot.starter.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
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
            md.update(msg.getMessageProperties().toString().getBytes());
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
    private static char[] encodeHex(byte[] bytes) {
        char[] chars = new char[32];
        for (int i = 0; i < chars.length; i = i + 2) {
            byte b = bytes[i / 2];
            chars[i] = HEX_CHARS[(b >>> 0x4) & 0xf];
            chars[i + 1] = HEX_CHARS[b & 0xf];
        }
        return chars;
    }
}
