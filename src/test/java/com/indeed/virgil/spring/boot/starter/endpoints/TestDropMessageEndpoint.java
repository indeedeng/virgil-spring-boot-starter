package com.indeed.virgil.spring.boot.starter.endpoints;

import com.indeed.virgil.spring.boot.starter.models.AckCertainMessageResponse;
import com.indeed.virgil.spring.boot.starter.models.ImmutableAckCertainMessageResponse;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestDropMessageEndpoint {

    @Mock
    private MessageOperator messageOperator;

    private DropMessageEndpoint dropMessageEndpoint;

    @BeforeEach
    void setup() {
        dropMessageEndpoint = new DropMessageEndpoint(messageOperator);
    }

    @Test
    void shouldImplementIVirgilEndpoint() {

        //Act
        final Class<?> c = DropMessageEndpoint.class;

        //Assert
        Assertions.assertTrue(IVirgilEndpoint.class.isAssignableFrom(c));
    }

    @Nested
    class index {

        @Test
        void shouldPassQueueNameToAckCertainMessage() {
            //Arrange
            final String queueName = "primaryQueue";
            final String messageId = "f_12312321321";

            when(messageOperator.ackCertainMessage(any(), any())).thenReturn(ImmutableAckCertainMessageResponse.builder()
                .setSuccess(false)
                .build());

            //Act
            dropMessageEndpoint.index(queueName, messageId);

            //Assert
            verify(messageOperator, times(1)).ackCertainMessage(eq(queueName), any());
        }

        @Test
        void shouldPassMessageIdToAckCertainMessage() {
            //Arrange
            final String queueName = "primaryQueue";
            final String messageId = "f_12312321321";

            when(messageOperator.ackCertainMessage(any(), any())).thenReturn(ImmutableAckCertainMessageResponse.builder()
                .setSuccess(false)
                .build());

            //Act
            dropMessageEndpoint.index(queueName, messageId);

            //Assert
            verify(messageOperator, times(1)).ackCertainMessage(any(), eq(messageId));
        }
    }

    @Nested
    class GetEndpointId {
        @Test
        void shouldReturnExpectedEndpointId() {
            //Arrange

            //Act
            final String result = DropMessageEndpoint.getEndpointId();

            //Assert
            assertThat(result).isEqualTo(EndpointConstants.DROP_MESSAGE_ENDPOINT_ID);
        }
    }

    @Nested
    class GetEndpointPath {
        @Test
        void shouldReturnExpectedEndpointPath() {
            //Arrange

            //Act
            final String result = DropMessageEndpoint.getEndpointPath();

            //Assert
            assertThat(result).isEqualTo(ENDPOINT_DEFAULT_PATH_MAPPING + EndpointConstants.DROP_MESSAGE_ENDPOINT_ID);
        }
    }
}
