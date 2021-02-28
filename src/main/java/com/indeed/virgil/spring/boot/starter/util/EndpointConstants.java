package com.indeed.virgil.spring.boot.starter.util;

public class EndpointConstants {

    public static final String PUBLISH_MESSAGE_ENDPOINT_ID = "publish-message";

    public static final String GET_QUEUE_SIZE_ENDPOINT_ID = "get-queue-size";

    public static final String GET_DLQ_MESSAGES_ENDPOINT_ID = "get-dlq-messages";

    public static final String GET_QUEUES_ENDPOINT_ID = "get-queues";

    public static final String DROP_MESSAGE_ENDPOINT_ID = "drop-message";

    public static final String DROP_ALL_MESSAGES_ENDPOINT_ID = "drop-all-messages";

    public static final String VIRGIL_PATH_PREFIX = "virgil/";

    public static final String ENDPOINT_DEFAULT_PATH_MAPPING = "private/" + VIRGIL_PATH_PREFIX;
}
