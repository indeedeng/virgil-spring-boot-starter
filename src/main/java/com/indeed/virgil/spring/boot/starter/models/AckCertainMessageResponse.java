package com.indeed.virgil.spring.boot.starter.models;

import org.immutables.value.Value;
import org.springframework.amqp.core.Message;

import javax.annotation.Nullable;


@Value.Immutable
@Value.Style(init = "set*", get = {"get*", "is*"})
public interface AckCertainMessageResponse {

    boolean isSuccess();

    @Nullable
    @Value.Default
    default Message getMessage() {
        return null;
    }
}
