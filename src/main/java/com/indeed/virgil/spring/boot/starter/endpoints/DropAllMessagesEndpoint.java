package com.indeed.virgil.spring.boot.starter.endpoints;

import com.indeed.virgil.spring.boot.starter.models.EndpointResponse;
import com.indeed.virgil.spring.boot.starter.models.ImmutableEndpointResponse;
import com.indeed.virgil.spring.boot.starter.services.MessageOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;

import java.io.Serializable;

import static com.indeed.virgil.spring.boot.starter.util.EndpointConstants.DROP_ALL_MESSAGES_ENDPOINT_ID;
import static com.indeed.virgil.spring.boot.starter.util.EndpointConstants.ENDPOINT_DEFAULT_PATH_MAPPING;

/**
 * We 'drop' message by acking the message.
 */
@Component
@Endpoint(id = DROP_ALL_MESSAGES_ENDPOINT_ID)
public class DropAllMessagesEndpoint implements IVirgilEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(DropAllMessagesEndpoint.class);

    private final MessageOperator messageOperator;

    @Autowired
    public DropAllMessagesEndpoint(final MessageOperator messageOperator) {
        this.messageOperator = messageOperator;
    }

    @WriteOperation
    public EndpointResponse<Serializable> index(final String queueId) {

        return ImmutableEndpointResponse.builder()
            .setData(messageOperator.dropMessages(queueId) ? "Success!" : "Failure")
            .build();
    }

    public static String getEndpointId() {
        return DROP_ALL_MESSAGES_ENDPOINT_ID;
    }

    public static String getEndpointPath() {
        return ENDPOINT_DEFAULT_PATH_MAPPING + getEndpointId();
    }
}
