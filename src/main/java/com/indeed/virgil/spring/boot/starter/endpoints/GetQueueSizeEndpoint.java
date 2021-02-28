package com.indeed.virgil.spring.boot.starter.endpoints;

import com.indeed.virgil.spring.boot.starter.models.EndpointResponse;
import com.indeed.virgil.spring.boot.starter.models.ImmutableEndpointResponse;
import com.indeed.virgil.spring.boot.starter.services.MessageOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.stereotype.Component;

import java.io.Serializable;

import static com.indeed.virgil.spring.boot.starter.util.EndpointConstants.ENDPOINT_DEFAULT_PATH_MAPPING;
import static com.indeed.virgil.spring.boot.starter.util.EndpointConstants.GET_QUEUE_SIZE_ENDPOINT_ID;

@Component
@Endpoint(id = GET_QUEUE_SIZE_ENDPOINT_ID)
public class GetQueueSizeEndpoint implements IVirgilEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(GetQueueSizeEndpoint.class);

    private final MessageOperator messageOperator;

    @Autowired
    public GetQueueSizeEndpoint(final MessageOperator messageOperator) {
        this.messageOperator = messageOperator;
    }

    @ReadOperation
    public EndpointResponse<Serializable> index(@Selector final String queueName) {
        return ImmutableEndpointResponse.builder()
            .setData(messageOperator.getQueueSize(queueName))
            .build();
    }

    public static String getEndpointId() {
        return GET_QUEUE_SIZE_ENDPOINT_ID;
    }

    public static String getEndpointPath() {
        return ENDPOINT_DEFAULT_PATH_MAPPING + getEndpointId();
    }
}
