package com.indeed.virgil.spring.boot.starter.models;

import org.immutables.value.Value;

import java.util.Map;

@Value.Immutable
@Value.Style(init = "set*", get = {"get*", "is*"})
public interface VirgilMessage {

    String getBody();

    Map<String, Object> getHeaders();

    String getFingerprint();
}
