package com.indeed.virgil.spring.boot.starter.models;

import org.immutables.value.Value;

import java.io.Serializable;
import java.util.List;

@Value.Immutable
@Value.Style(init = "set*", get = {"get*", "is*"})
public interface EndpointResponse<T extends Serializable> {

    T getData();

    List<EndpointResponseError> getErrors();
}
