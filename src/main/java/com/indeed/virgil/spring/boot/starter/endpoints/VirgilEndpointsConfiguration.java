package com.indeed.virgil.spring.boot.starter.endpoints;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Configuration for the available endpoints.
 */
@Configuration
@Import({
    DropAllMessagesEndpoint.class,
    DropMessageEndpoint.class,
    GetDlqMessagesEndpoint.class,
    GetQueueSizeEndpoint.class,
    PublishMessageEndpoint.class
})
public class VirgilEndpointsConfiguration {
}
