package com.indeed.virgil.spring.boot.starter.models;

import org.immutables.value.Value;

import java.util.Map;

@Value.Immutable
@Value.Style(init = "set*", get = {"get*", "is*"})
public interface VirgilMessage {

    /**
     * Id should either be the messageId or the unique md5 digest of the message.
     *
     * @return
     */
    String getId();

    String getBody();

    Map<String, Object> getHeaders();

    String getFingerprint();
}
