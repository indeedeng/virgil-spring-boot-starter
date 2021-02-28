package com.indeed.virgil.spring.boot.starter.endpoints;

import com.indeed.virgil.spring.boot.starter.services.MessageOperator;
import com.indeed.virgil.spring.boot.starter.util.EndpointConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.indeed.virgil.spring.boot.starter.util.EndpointConstants.ENDPOINT_DEFAULT_PATH_MAPPING;

@ExtendWith(MockitoExtension.class)
public class TestGetQueueSizeEndpoint {

    @Mock
    private MessageOperator messageOperator;

    private GetQueueSizeEndpoint getQueueSizeEndpoint;

    @BeforeEach
    void setup() {
        getQueueSizeEndpoint = new GetQueueSizeEndpoint(messageOperator);
    }

    @Test
    void shouldImplementIVirgilEndpoint() {

        //Act
        final Class<?> c = GetQueueSizeEndpoint.class;

        //Assert
        Assertions.assertTrue(IVirgilEndpoint.class.isAssignableFrom(c));
    }

    @Test
    void testGetEndpointId_shouldReturnExpectedEndpointId() {
        //Arrange

        //Act
        final String result = getQueueSizeEndpoint.getEndpointId();

        //Assert
        Assertions.assertEquals(EndpointConstants.GET_QUEUE_SIZE_ENDPOINT_ID + "-queueName", result);
    }

    @Test
    void testGetEndpointPath_shouldReturnExpectedEndpointPath() {
        //Arrange

        //Act
        final String result = getQueueSizeEndpoint.getEndpointPath();

        //Assert
        Assertions.assertEquals(ENDPOINT_DEFAULT_PATH_MAPPING + EndpointConstants.GET_QUEUE_SIZE_ENDPOINT_ID + "-queueName", result);
    }
}
