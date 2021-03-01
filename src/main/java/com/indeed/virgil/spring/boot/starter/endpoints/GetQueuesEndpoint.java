package com.indeed.virgil.spring.boot.starter.endpoints;

import com.indeed.virgil.spring.boot.starter.config.VirgilPropertyConfig;
import com.indeed.virgil.spring.boot.starter.models.EndpointResponse;
import com.indeed.virgil.spring.boot.starter.models.ImmutableEndpointResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

import static com.indeed.virgil.spring.boot.starter.util.EndpointConstants.ENDPOINT_DEFAULT_PATH_MAPPING;
import static com.indeed.virgil.spring.boot.starter.util.EndpointConstants.GET_QUEUES_ENDPOINT_ID;

@Component
@Endpoint(id = GET_QUEUES_ENDPOINT_ID)
public class GetQueuesEndpoint implements IVirgilEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(GetQueuesEndpoint.class);

    private final VirgilPropertyConfig virgilPropertyConfig;

    @Autowired
    public GetQueuesEndpoint(
        final VirgilPropertyConfig virgilPropertyConfig
    ) {
        this.virgilPropertyConfig = virgilPropertyConfig;
    }

    @ReadOperation
    public EndpointResponse<Serializable> index() {

        final List<String> list = virgilPropertyConfig.getQueueNames();

        return ImmutableEndpointResponse.builder()
            .setData((Serializable)list)
            .build();
    }

    public static String getEndpointId() {
        return GET_QUEUES_ENDPOINT_ID;
    }

    public static String getEndpointPath() {
        return ENDPOINT_DEFAULT_PATH_MAPPING + getEndpointId();
    }
}
