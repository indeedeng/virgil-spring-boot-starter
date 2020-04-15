package com.indeed.virgil.spring.boot.starter.endpoints;

public interface IVirgilEndpoint {

    static String getEndpointId() {
        throw new RuntimeException("getEndpointId not implemented");
    }

    static String getEndpointPath() {
        throw new RuntimeException("getEndpointPath not implemented");
    }
}
