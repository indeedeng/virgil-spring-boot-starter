package com.indeed.virgil.spring.boot.starter.endpoints;

import com.indeed.virgil.spring.boot.starter.models.AckCertainMessageResponse;
import com.indeed.virgil.spring.boot.starter.models.EndpointResponse;
import com.indeed.virgil.spring.boot.starter.models.ImmutableEndpointResponse;
import com.indeed.virgil.spring.boot.starter.services.MessageOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;

import java.io.Serializable;

import static com.indeed.virgil.spring.boot.starter.util.EndpointConstants.DROP_MESSAGE_ENDPOINT_ID;
import static com.indeed.virgil.spring.boot.starter.util.EndpointConstants.ENDPOINT_DEFAULT_PATH_MAPPING;

/**
 * We 'drop' message by acking the message.
 */
@Component
@Endpoint(id = DROP_MESSAGE_ENDPOINT_ID)
public class DropMessageEndpoint implements IVirgilEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(DropMessageEndpoint.class);


    private final MessageOperator messageOperator;

    @Autowired
    public DropMessageEndpoint(final MessageOperator messageOperator) {
        this.messageOperator = messageOperator;
    }

    @WriteOperation
    public EndpointResponse<Serializable> index(final String messageId) {

        final AckCertainMessageResponse response = messageOperator.ackCertainMessage(messageId);

        return ImmutableEndpointResponse.builder()
            .setData(response.isSuccess() ? "Success!" : "Failure")
            .build();
    }

    public static String getEndpointId() {
        return DROP_MESSAGE_ENDPOINT_ID;
    }

    public static String getEndpointPath() {
        return ENDPOINT_DEFAULT_PATH_MAPPING + getEndpointId();
    }
}
