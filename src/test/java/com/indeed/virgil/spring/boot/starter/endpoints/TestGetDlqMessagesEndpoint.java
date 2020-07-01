package com.indeed.virgil.spring.boot.starter.endpoints;

import com.indeed.virgil.spring.boot.starter.models.EndpointResponse;
import com.indeed.virgil.spring.boot.starter.models.ImmutableEndpointResponse;
import com.indeed.virgil.spring.boot.starter.services.MessageConverterService;
import com.indeed.virgil.spring.boot.starter.services.MessageOperator;
import com.indeed.virgil.spring.boot.starter.util.EndpointConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.Serializable;
import java.util.ArrayList;

import static com.indeed.virgil.spring.boot.starter.util.EndpointConstants.ENDPOINT_DEFAULT_PATH_MAPPING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestGetDlqMessagesEndpoint {

    @Mock
    private MessageOperator messageOperator;

    private GetDlqMessagesEndpoint getDlqMessagesEndpoint;

    @BeforeEach
    void setup() {
        getDlqMessagesEndpoint = new GetDlqMessagesEndpoint(messageOperator);
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

    @Nested
    class testIndex {

        @Test
        void shouldCallGetMessagesWithLimit() {
            //Arrange
            final Integer limit = 100;
            when(messageOperator.getMessages(any())).thenReturn(new ArrayList<>());

            //Act
            getDlqMessagesEndpoint.index(limit);

            //Assert
            verify(messageOperator, times(1)).getMessages(limit);
        }

        @Test
        void shouldReturnDataFromGetMessages() {
            //Arrange
            final Integer limit = 100;
            when(messageOperator.getMessages(any())).thenReturn(new ArrayList<>());

            //Act
            final EndpointResponse<Serializable> result = getDlqMessagesEndpoint.index(limit);

            //Assert
            assertThat(result).isEqualTo(ImmutableEndpointResponse.builder()
                .setData(new ArrayList<>())
                .build());
        }
    }
}
