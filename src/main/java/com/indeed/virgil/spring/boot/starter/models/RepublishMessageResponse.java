package com.indeed.virgil.spring.boot.starter.models;

import org.immutables.value.Value;

@Value.Immutable
@Value.Style(init = "set*", get = {"get*", "is*"})
public interface RepublishMessageResponse {

    boolean isSuccess();
}
