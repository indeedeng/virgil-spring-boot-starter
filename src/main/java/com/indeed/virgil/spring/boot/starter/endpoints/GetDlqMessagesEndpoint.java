package com.indeed.virgil.spring.boot.starter.endpoints;

import com.indeed.virgil.spring.boot.starter.models.EndpointResponse;
import com.indeed.virgil.spring.boot.starter.models.ImmutableEndpointResponse;
import com.indeed.virgil.spring.boot.starter.models.VirgilMessage;
import com.indeed.virgil.spring.boot.starter.services.MessageOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;

import static com.indeed.virgil.spring.boot.starter.util.EndpointConstants.ENDPOINT_DEFAULT_PATH_MAPPING;
import static com.indeed.virgil.spring.boot.starter.util.EndpointConstants.GET_DLQ_MESSAGES_ENDPOINT_ID;

@Component
@Endpoint(id = GET_DLQ_MESSAGES_ENDPOINT_ID)
public class GetDlqMessagesEndpoint implements IVirgilEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(GetDlqMessagesEndpoint.class);

    private final MessageOperator messageOperator;

    @Autowired
    public GetDlqMessagesEndpoint(
        final MessageOperator messageOperator
    ) {
        this.messageOperator = messageOperator;
    }

    @ReadOperation
    public EndpointResponse<Serializable> index(final String queueName, @Nullable final Integer limit) {
        final ArrayList<VirgilMessage> result = new ArrayList<>(messageOperator.getMessages(queueName, limit));

        return ImmutableEndpointResponse.builder()
            .setData(result)
            .build();
    }

    public static String getEndpointId() {
        return GET_DLQ_MESSAGES_ENDPOINT_ID;
    }

    public static String getEndpointPath() {
        return ENDPOINT_DEFAULT_PATH_MAPPING + getEndpointId();
    }
}
