package com.indeed.virgil.spring.boot.starter.endpoints;

import com.indeed.virgil.spring.boot.starter.services.MessageOperator;
import com.indeed.virgil.spring.boot.starter.util.EndpointConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.indeed.virgil.spring.boot.starter.util.EndpointConstants.ENDPOINT_DEFAULT_PATH_MAPPING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TestDropAllMessagesEndpoint {

    @Mock
    private MessageOperator messageOperator;

    private DropAllMessagesEndpoint dropAllMessagesEndpoint;

    @BeforeEach
    void setup() {
        dropAllMessagesEndpoint = new DropAllMessagesEndpoint(messageOperator);
    }

    @Test
    void shouldImplementIVirgilEndpoint() {

        //Act
        final Class<?> c = DropAllMessagesEndpoint.class;

        //Assert
        Assertions.assertTrue(IVirgilEndpoint.class.isAssignableFrom(c));
    }

    @Nested
    class index {

        @Test
        void shouldPassQueueNameToDropMessages() {
            //Arrange
            final String queueName = "primaryQueue";

            //Act
            dropAllMessagesEndpoint.index(queueName);

            //Assert
            verify(messageOperator, times(1)).dropMessages(queueName);
        }
    }

    @Nested
    class GetEndpointId {
        @Test
        void shouldReturnExpectedEndpointId() {
            //Arrange

            //Act
            final String result = DropAllMessagesEndpoint.getEndpointId();

            //Assert
            assertThat(result).isEqualTo(EndpointConstants.DROP_ALL_MESSAGES_ENDPOINT_ID);
        }
    }

    @Nested
    class GetEndpointPath {
        @Test
        void shouldReturnExpectedEndpointPath() {
            //Arrange

            //Act
            final String result = DropAllMessagesEndpoint.getEndpointPath();

            //Assert
            assertThat(result).isEqualTo(ENDPOINT_DEFAULT_PATH_MAPPING + EndpointConstants.DROP_ALL_MESSAGES_ENDPOINT_ID);
        }
    }
}
