package com.indeed.virgil.spring.boot.starter.endpoints;

import com.indeed.virgil.spring.boot.starter.services.MessageOperator;
import com.indeed.virgil.spring.boot.starter.util.EndpointConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.indeed.virgil.spring.boot.starter.util.EndpointConstants.ENDPOINT_DEFAULT_PATH_MAPPING;

public class TestPublishMessageEndpoint {

    @Mock
    private MessageOperator messageOperator;

    private PublishMessageEndpoint publishMessageEndpoint;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);

        publishMessageEndpoint = new PublishMessageEndpoint(messageOperator);
    }

    @Test
    void shouldImplementIVirgilEndpoint() {

        //Act
        final Class<?> c = PublishMessageEndpoint.class;

        //Assert
        Assertions.assertTrue(IVirgilEndpoint.class.isAssignableFrom(c));
    }

    @Test
    void testGetEndpointId_shouldReturnExpectedEndpointId() {
        //Arrange

        //Act
        final String result = publishMessageEndpoint.getEndpointId();

        //Assert
        Assertions.assertEquals(EndpointConstants.PUBLISH_MESSAGE_ENDPOINT_ID, result);
    }

    @Test
    void testGetEndpointPath_shouldReturnExpectedEndpointPath() {
        //Arrange

        //Act
        final String result = publishMessageEndpoint.getEndpointPath();

        //Assert
        Assertions.assertEquals(ENDPOINT_DEFAULT_PATH_MAPPING + EndpointConstants.PUBLISH_MESSAGE_ENDPOINT_ID, result);
    }
}
