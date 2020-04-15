package com.indeed.virgil.spring.boot.starter.endpoints;

import com.indeed.virgil.spring.boot.starter.services.MessageConverterService;
import com.indeed.virgil.spring.boot.starter.services.MessageOperator;
import com.indeed.virgil.spring.boot.starter.util.EndpointConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.indeed.virgil.spring.boot.starter.util.EndpointConstants.ENDPOINT_DEFAULT_PATH_MAPPING;

public class TestGetDlqMessagesEndpoint {

    @Mock
    private MessageOperator messageOperator;

    @Mock
    private MessageConverterService messageConverterService;

    private GetDlqMessagesEndpoint getDlqMessagesEndpoint;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);

        getDlqMessagesEndpoint = new GetDlqMessagesEndpoint(messageOperator, messageConverterService);
    }

    @Test
    void shouldImplementIVirgilEndpoint() {

        //Act
        final Class<?> c = GetDlqMessagesEndpoint.class;

        //Assert
        Assertions.assertTrue(IVirgilEndpoint.class.isAssignableFrom(c));
    }

    @Test
    void testGetEndpointId_shouldReturnExpectedEndpointId() {
        //Arrange

        //Act
        final String result = getDlqMessagesEndpoint.getEndpointId();

        //Assert
        Assertions.assertEquals(EndpointConstants.GET_DLQ_MESSAGES_ENDPOINT_ID, result);
    }

    @Test
    void testGetEndpointPath_shouldReturnExpectedEndpointPath() {
        //Arrange

        //Act
        final String result = getDlqMessagesEndpoint.getEndpointPath();

        //Assert
        Assertions.assertEquals(ENDPOINT_DEFAULT_PATH_MAPPING + EndpointConstants.GET_DLQ_MESSAGES_ENDPOINT_ID, result);
    }
}
