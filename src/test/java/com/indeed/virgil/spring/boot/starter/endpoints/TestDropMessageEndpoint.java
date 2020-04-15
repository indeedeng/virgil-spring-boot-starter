package com.indeed.virgil.spring.boot.starter.endpoints;

import com.indeed.virgil.spring.boot.starter.services.MessageOperator;
import com.indeed.virgil.spring.boot.starter.util.EndpointConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.indeed.virgil.spring.boot.starter.util.EndpointConstants.ENDPOINT_DEFAULT_PATH_MAPPING;

public class TestDropMessageEndpoint {

    @Mock
    private MessageOperator messageOperator;

    private DropMessageEndpoint dropMessageEndpoint;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);

        dropMessageEndpoint = new DropMessageEndpoint(messageOperator);
    }

    @Test
    void shouldImplementIVirgilEndpoint() {

        //Act
        final Class<?> c = DropMessageEndpoint.class;

        //Assert
        Assertions.assertTrue(IVirgilEndpoint.class.isAssignableFrom(c));
    }

    @Test
    void testGetEndpointId_shouldReturnExpectedEndpointId() {
        //Arrange

        //Act
        final String result = dropMessageEndpoint.getEndpointId();

        //Assert
        Assertions.assertEquals(EndpointConstants.DROP_MESSAGE_ENDPOINT_ID, result);
    }

    @Test
    void testGetEndpointPath_shouldReturnExpectedEndpointPath() {
        //Arrange

        //Act
        final String result = dropMessageEndpoint.getEndpointPath();

        //Assert
        Assertions.assertEquals(ENDPOINT_DEFAULT_PATH_MAPPING + EndpointConstants.DROP_MESSAGE_ENDPOINT_ID, result);
    }
}
