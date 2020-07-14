package com.indeed.virgil.spring.boot.starter.services;

import com.indeed.virgil.spring.boot.starter.config.VirgilPropertyConfig;
import com.indeed.virgil.spring.boot.starter.models.ImmutableVirgilMessage;
import com.indeed.virgil.spring.boot.starter.models.VirgilMessage;
import com.indeed.virgil.spring.boot.starter.services.MessageOperator.HandleAckCertainMessage;
import com.indeed.virgil.spring.boot.starter.services.MessageOperator.HandleDropMessages;
import com.indeed.virgil.spring.boot.starter.services.MessageOperator.HandleGetMessages;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;
import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestMessageOperator {

    private static final String EXCHANGE_NAME = "exchange";
    private static final String QUEUE_NAME = "default-queue-name";
    private static final String BINDER_NAME = "default-binder-name";
    private static final Integer DEFAULT_QUEUE_SIZE = 200;
    private static final Integer QUEUE_SIZE_3 = 3;
    private static final Integer QUEUE_SIZE_0 = 0;
    private static final String FINGER_PRINT = "04222b1ddbd35132da9684f0c9c452a2";
    private static final String MESSAGE_BODY = "body";
    private static final String BINDING_KEY = "#";

    @Mock
    private MessageConverterService messageConverterService;

    @Mock
    private VirgilPropertyConfig virgilPropertyConfig;

    @Mock
    private RabbitMqConnectionService rabbitMqConnectionService;

    @Mock
    private AmqpAdmin amqpAdmin;

    @Mock
    private RabbitTemplate rabbitTemplate;

    private MessageOperator messageOperator;

    @BeforeEach
    public void initializeSetup() {
        MockitoAnnotations.initMocks(this);

        messageOperator = new MessageOperator(virgilPropertyConfig, rabbitMqConnectionService, messageConverterService);
    }

    @Nested
    class getQueueSize {

        @Test
        public void shouldReturnQueueSizeWhenQueueExists() {

            initializeQueueProperties(false);

            assertThat(messageOperator.getQueueSize()).isEqualTo(QUEUE_SIZE_3);
        }

        @Test
        public void shouldReturnNullWhenQueueDoesNotExist() {

            initializeQueueProperties(true);


            assertThat(messageOperator.getQueueSize()).isNull();
        }
    }

    @Nested
    class getMessages {
        @Test
        public void testGetMessages() {

            initializeQueueProperties(false);

            assertNotNull(messageOperator.getMessages(null));

            verify(rabbitTemplate, times(QUEUE_SIZE_3)).execute(any());
        }

        @Test
        public void testGetMessages_limited() {

            initializeQueueProperties(false);

            assertNotNull(messageOperator.getMessages(1));

            verify(rabbitTemplate, times(1)).execute(any());
        }

        @Test
        public void testGetMessages_limited_invalid() {

            initializeQueueProperties(false);

            assertNotNull(messageOperator.getMessages(-1));

            verify(rabbitTemplate, times(QUEUE_SIZE_3)).execute(any());
        }

        @Test
        public void testGetMessagesQueueNotExist() {

            initializeQueueProperties(true);

            assertNull(messageOperator.getMessages(null));

            verify(rabbitTemplate, times(QUEUE_SIZE_0)).execute(any());
        }
    }

    @Nested
    class publishCertainMessage {
        @Test
        public void testPublishCertainMessage() {

            initializeQueueProperties(false);

            final Map<String, Message> messageCache = new HashMap<>();
            final VirgilMessage virgilMessage = mock(VirgilMessage.class);

            final Message message = new Message(MESSAGE_BODY.getBytes(), new MessageProperties());

            messageCache.put(FINGER_PRINT, message);

            when(virgilMessage.getFingerprint()).thenReturn(FINGER_PRINT);

            messageOperator.setMessageCache(messageCache);

            assertTrue(messageOperator.publishCertainMessage(virgilMessage.getFingerprint()));

            verify(rabbitTemplate, times(1)).convertAndSend(eq(BINDER_NAME), eq(BINDING_KEY), any(Message.class));
        }

        @Test
        public void testPublishCertainMessageInvalidFingerPrint() {
            // null
            assertFalse(messageOperator.publishCertainMessage(null));

            // empty
            assertFalse(messageOperator.publishCertainMessage(""));
        }

        @Test
        public void testPublishCertainMessageEmptyMessageCache() {
            initializeQueueProperties(false);
            assertFalse(messageOperator.publishCertainMessage(FINGER_PRINT));
            // if messageCache is empty, the method will call getMessages() function to re-generate it
            verify(rabbitTemplate, times(DEFAULT_QUEUE_SIZE)).execute(any());
        }

        @Test
        public void testPublishCertainMessageInvalidMessageCache() {
            final Message message = mock(Message.class);
            initializeQueueProperties(false);
            final Map<String, Message> messageCache = new HashMap<>();
            messageCache.put("invalid", message);
            messageOperator.setMessageCache(messageCache);
            assertFalse(messageOperator.publishCertainMessage(FINGER_PRINT));
            // if messageCache doesn't contain target finger print, the method will call getMessages() function to re-generate it
            verify(rabbitTemplate, times(DEFAULT_QUEUE_SIZE)).execute(any());
        }
    }

    @Nested
    class ackMessages {

        @Test
        public void testAckMessages() {

            initializeQueueProperties(false);

            assertTrue(messageOperator.dropMessages());

            verify(rabbitTemplate, times(1)).execute(any());
        }

        @Test
        public void testAckMessagesQueueNotExist() {

            initializeQueueProperties(true);

            assertFalse(messageOperator.dropMessages());

            verify(rabbitTemplate, times(QUEUE_SIZE_0)).execute(any());
        }

    }

    @Nested
    class ackCertainMessage {

        @Test
        public void testAckCertainMessage() {

            //Arrange
            initializeQueueProperties(false);

            //Act
            messageOperator.ackCertainMessage(FINGER_PRINT);

            //Assert
            verify(rabbitTemplate, times(QUEUE_SIZE_3)).execute(any());
        }

        @Test
        public void testAckCertainMessageInvalidFingerPrint() {
            // null
            assertFalse(messageOperator.ackCertainMessage(null));

            // empty
            assertFalse(messageOperator.ackCertainMessage(""));
        }

        @Test
        public void shouldNotCallExecuteIfQueueSizeIsNull() {

            initializeQueueProperties(true);

            assertFalse(messageOperator.ackCertainMessage(FINGER_PRINT));

            verify(rabbitTemplate, times(0)).execute(any());
        }

        @Test
        public void shouldReturnFalseIfResponseFromBasicGetIsNull() {
            //Arrange
            initializeQueueProperties(false);


            //Act
            final boolean result = messageOperator.ackCertainMessage(FINGER_PRINT);

            //Assert
            assertThat(result).isFalse();
        }
    }

    @Nested
    class testHandleAckCertainMessage {

        @Test
        void shouldPassFalseToAutoAckInBasicGet() throws Exception {
            //Arrange
            initializeQueueProperties(false);

            final MessagePropertiesConverter messagePropertiesConverter = new DefaultMessagePropertiesConverter();
            final MessageConverterService messageConverterService = mock(MessageConverterService.class);

            final HandleAckCertainMessage handleAckCertainMessage = new HandleAckCertainMessage(messageOperator, messagePropertiesConverter, messageConverterService,"");

            final Channel mockChannel = mock(Channel.class);

            final ArgumentCaptor<Boolean> autoAckCapture = ArgumentCaptor.forClass(Boolean.class);

            when(mockChannel.basicGet(eq(QUEUE_NAME), autoAckCapture.capture())).thenReturn(null);

            //Act
            handleAckCertainMessage.doInRabbit(mockChannel);

            //Assert
            assertThat(autoAckCapture.getValue()).isFalse();
        }

        @Test
        void shouldReturnNullIfBasicGetReturnsNull() throws Exception {
            //Arrange
            initializeQueueProperties(false);

            final MessagePropertiesConverter messagePropertiesConverter = new DefaultMessagePropertiesConverter();
            final MessageConverterService messageConverterService = mock(MessageConverterService.class);

            final HandleAckCertainMessage handleAckCertainMessage = new HandleAckCertainMessage(messageOperator, messagePropertiesConverter, messageConverterService,"");

            final Channel mockChannel = mock(Channel.class);

            when(mockChannel.basicGet(QUEUE_NAME, false)).thenReturn(null);

            //Act
            final String result = handleAckCertainMessage.doInRabbit(mockChannel);

            //Assert
            assertThat(result).isNull();
        }

        @Test
        void shouldCallBasicAckIfFingerMatchesCurrentMessageFingerprint() throws Exception {
            //Arrange
            initializeQueueProperties(false);

            final long deliveryTag = 123L;
            final boolean redeliver = false;
            final String fingerprint = "12313920123912";

            final MessagePropertiesConverter messagePropertiesConverter = new DefaultMessagePropertiesConverter();
            final MessageConverterService messageConverterService = mock(MessageConverterService.class);

            final VirgilMessage virgilMessage = ImmutableVirgilMessage.builder()
                .setBody("bodymessage")
                .setFingerprint(fingerprint)
                .build();

            when(messageConverterService.mapMessage(any())).thenReturn(virgilMessage);

            final HandleAckCertainMessage handleAckCertainMessage = new HandleAckCertainMessage(messageOperator, messagePropertiesConverter, messageConverterService,fingerprint);

            final GetResponse mockGetResponse = mock(GetResponse.class);
            when(mockGetResponse.getProps()).thenReturn(new BasicProperties());
            when(mockGetResponse.getEnvelope()).thenReturn(new Envelope(deliveryTag, redeliver, EXCHANGE_NAME, BINDING_KEY));


            final Channel mockChannel = mock(Channel.class);
            when(mockChannel.basicGet(QUEUE_NAME, false)).thenReturn(mockGetResponse);

            //Act
            final String result = handleAckCertainMessage.doInRabbit(mockChannel);

            //Assert
            assertThat(result).isNull();
            verify(mockChannel, times(1)).basicAck(deliveryTag, false);
        }
    }

    @Nested
    class testHandleDropMessages {

        @Test
        void shouldCallQueuePurge() throws Exception {
            //Arrange
            initializeQueueProperties(false);

            final Channel mockChannel = mock(Channel.class);

            final HandleDropMessages handleDropMessages = new HandleDropMessages(messageOperator);

            //Act
            handleDropMessages.doInRabbit(mockChannel);

            //Assert
            verify(mockChannel, times(1)).queuePurge(any());
        }

        @Test
        void shouldReturnNullOnSuccess() throws Exception {
            //Arrange
            initializeQueueProperties(false);

            final Channel mockChannel = mock(Channel.class);

            final HandleDropMessages handleDropMessages = new HandleDropMessages(messageOperator);

            //Act
            final Void result = handleDropMessages.doInRabbit(mockChannel);

            //Assert
            assertThat(result).isEqualTo(null);
        }
    }

    @Nested
    class testHandleGetMessages {

        @Test
        void shouldPassFalseToAutoAckInBasicGet() throws Exception {
            //Arrange
            initializeQueueProperties(false);

            final MessagePropertiesConverter messagePropertiesConverter = new DefaultMessagePropertiesConverter();
            final MessageConverterService messageConverterService = mock(MessageConverterService.class);

            final HandleGetMessages handleGetMessages = new HandleGetMessages(messageOperator, messagePropertiesConverter, messageConverterService,10);

            final Channel mockChannel = mock(Channel.class);

            final ArgumentCaptor<Boolean> autoAckCapture = ArgumentCaptor.forClass(Boolean.class);

            when(mockChannel.basicGet(eq(QUEUE_NAME), autoAckCapture.capture())).thenReturn(null);

            //Act
            handleGetMessages.doInRabbit(mockChannel);

            //Assert
            assertThat(autoAckCapture.getValue()).isFalse();
        }

        @Test
        void shouldReturnNullIfBasicGetReturnsNull() throws Exception {
            //Arrange
            initializeQueueProperties(false);

            final MessagePropertiesConverter messagePropertiesConverter = new DefaultMessagePropertiesConverter();
            final MessageConverterService messageConverterService = mock(MessageConverterService.class);

            final HandleGetMessages handleGetMessages = new HandleGetMessages(messageOperator, messagePropertiesConverter, messageConverterService,10);

            final Channel mockChannel = mock(Channel.class);

            when(mockChannel.basicGet(QUEUE_NAME, false)).thenReturn(null);

            //Act
            final Void result = handleGetMessages.doInRabbit(mockChannel);

            //Assert
            assertThat(result).isEqualTo(null);
        }

        @Test
        void shouldAddMessageToDlqMessages() throws Exception {
            //Arrange
            initializeQueueProperties(false);

            final long deliveryTag = 123L;
            final boolean redeliver = false;
            final String fingerprint = "uniqueFingerprint";
            final String body = "bodymessage";

            final MessagePropertiesConverter messagePropertiesConverter = new DefaultMessagePropertiesConverter();
            final MessageConverterService messageConverterService = mock(MessageConverterService.class);

            final VirgilMessage virgilMessage = ImmutableVirgilMessage.builder()
                .setBody(body)
                .setFingerprint(fingerprint)
                .build();

            when(messageConverterService.mapMessage(any())).thenReturn(virgilMessage);

            final HandleGetMessages handleGetMessages = new HandleGetMessages(messageOperator, messagePropertiesConverter, messageConverterService,10);

            final GetResponse mockGetResponse = mock(GetResponse.class);
            when(mockGetResponse.getProps()).thenReturn(new BasicProperties());
            when(mockGetResponse.getEnvelope()).thenReturn(new Envelope(deliveryTag, redeliver, EXCHANGE_NAME, BINDING_KEY));


            final Channel mockChannel = mock(Channel.class);
            when(mockChannel.basicGet(QUEUE_NAME, false)).thenReturn(mockGetResponse);

            //Act
            handleGetMessages.doInRabbit(mockChannel);

            //Assert
            assertThat(handleGetMessages.getDlqMessages()).isEqualTo(Arrays.asList(
                ImmutableVirgilMessage.builder()
                    .setBody(body)
                    .setFingerprint(fingerprint)
                    .build()
            ));
        }

        @Test
        void shouldAddMessageToMessageLookup() throws Exception {
            //Arrange
            initializeQueueProperties(false);

            final long deliveryTag = 123L;
            final boolean redeliver = false;
            final String fingerprint = "uniqueFingerprint";
            final String body = "bodymessage";

            final MessageProperties mockMessageProperties = mock(MessageProperties.class);
            final MessagePropertiesConverter messagePropertiesConverter = mock(MessagePropertiesConverter.class);

            when(messagePropertiesConverter.toMessageProperties(any(), any(), any())).thenReturn(mockMessageProperties);

            final MessageConverterService messageConverterService = mock(MessageConverterService.class);

            final Message expectedMessage = new Message(body.getBytes(), mockMessageProperties);

            final VirgilMessage virgilMessage = ImmutableVirgilMessage.builder()
                .setBody(body)
                .setFingerprint(fingerprint)
                .build();

            when(messageConverterService.mapMessage(any())).thenReturn(virgilMessage);

            final HandleGetMessages handleGetMessages = new HandleGetMessages(messageOperator, messagePropertiesConverter, messageConverterService,10);

            final GetResponse mockGetResponse = mock(GetResponse.class);
            when(mockGetResponse.getBody()).thenReturn(body.getBytes());
            when(mockGetResponse.getProps()).thenReturn(new BasicProperties());
            when(mockGetResponse.getEnvelope()).thenReturn(new Envelope(deliveryTag, redeliver, EXCHANGE_NAME, BINDING_KEY));


            final Channel mockChannel = mock(Channel.class);
            when(mockChannel.basicGet(QUEUE_NAME, false)).thenReturn(mockGetResponse);

            //Act
            handleGetMessages.doInRabbit(mockChannel);

            //Assert
            assertThat(handleGetMessages.getMessageLookup()).contains(entry(fingerprint, expectedMessage));
        }
    }

    private void initializeQueueProperties(final boolean testQueueNotExist) {

        final VirgilPropertyConfig.BinderProperties binderProperties = new VirgilPropertyConfig.BinderProperties(
            BINDER_NAME,
            null,
            null
        );

        final VirgilPropertyConfig.QueueProperties queueProperties = new VirgilPropertyConfig.QueueProperties(
            QUEUE_NAME,
            BINDER_NAME,
            binderProperties,
            null,
            BINDING_KEY,
            null,
            null
        );
        when(virgilPropertyConfig.getDefaultQueue()).thenReturn(queueProperties);

        when(rabbitMqConnectionService.getAmqpAdmin(BINDER_NAME)).thenReturn(amqpAdmin);

        if (testQueueNotExist) {
            when(amqpAdmin.getQueueProperties(QUEUE_NAME)).thenReturn(null);
        } else {
            final Properties properties = new Properties();
            properties.put(RabbitAdmin.QUEUE_MESSAGE_COUNT.toString(), QUEUE_SIZE_3);

            when(amqpAdmin.getQueueProperties(QUEUE_NAME)).thenReturn(properties);
        }

        when(rabbitMqConnectionService.getRabbitTemplate(BINDER_NAME)).thenReturn(rabbitTemplate);
        when(rabbitTemplate.getConnectionFactory()).thenReturn(new CachingConnectionFactory());
    }
}
