package com.indeed.virgil.spring.boot.starter.services;

import com.indeed.virgil.spring.boot.starter.config.VirgilPropertyConfig;
import com.indeed.virgil.spring.boot.starter.models.AckCertainMessageResponse;
import com.indeed.virgil.spring.boot.starter.models.ImmutableVirgilMessage;
import com.indeed.virgil.spring.boot.starter.models.RepublishMessageResponse;
import com.indeed.virgil.spring.boot.starter.models.VirgilMessage;
import com.indeed.virgil.spring.boot.starter.services.MessageOperator.HandleAckCertainMessage;
import com.indeed.virgil.spring.boot.starter.services.MessageOperator.HandleDropMessages;
import com.indeed.virgil.spring.boot.starter.services.MessageOperator.HandleGetMessages;
import com.indeed.virgil.spring.boot.starter.services.MessageOperator.HandleRepublishMessage;
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

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
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
    private static final String MESSAGE_ID = "f_04222b1ddbd35132da9684f0c9c452a2";
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
            //Arrange
            initializeQueueProperties(false);

            //Act
            final List<VirgilMessage> result = messageOperator.getMessages(null);

            //Assert
            assertThat(result).isNotNull();

            verify(rabbitTemplate, times(QUEUE_SIZE_3)).execute(any());
        }

        @Test
        public void testGetMessages_limited() {
            //Arrange
            initializeQueueProperties(false);

            //Act
            final List<VirgilMessage> result = messageOperator.getMessages(1);

            //Assert
            assertThat(result).isEmpty();

            verify(rabbitTemplate, times(1)).execute(any());
        }

        @Test
        public void testGetMessages_limited_invalid() {
            //Arrange
            initializeQueueProperties(false);

            //Act
            final List<VirgilMessage> result = messageOperator.getMessages(-1);

            //Assert
            assertThat(result).isNotNull();

            verify(rabbitTemplate, times(QUEUE_SIZE_3)).execute(any());
        }

        @Test
        public void testGetMessagesQueueNotExist() {
            //Arrange
            initializeQueueProperties(true);

            //Act
            final List<VirgilMessage> result = messageOperator.getMessages(null);

            //Assert
            assertThat(result).isEmpty();

            verify(rabbitTemplate, times(QUEUE_SIZE_0)).execute(any());
        }
    }

    @Nested
    class ackMessages {

        @Test
        void shouldReturnTrueAfterDroppingMessagesSuccessfully() {
            //Arrange
            initializeQueueProperties(false);

            //Act
            final boolean result = messageOperator.dropMessages();

            //Assert
            assertThat(result).isTrue();

            verify(rabbitTemplate, times(1)).execute(any());
        }

        @Test
        void shouldReturnFalseIfQueueDoesntExists() {
            //Arrange
            initializeQueueProperties(true);

            //Act
            final boolean result = messageOperator.dropMessages();

            //Assert
            assertThat(result).isFalse();

            verify(rabbitTemplate, times(QUEUE_SIZE_0)).execute(any());
        }

    }

    @Nested
    class ackCertainMessage {

        @Test
        void testAckCertainMessage() {

            //Arrange
            initializeQueueProperties(false);

            //Act
            messageOperator.ackCertainMessage(MESSAGE_ID);

            //Assert
            verify(rabbitTemplate, times(QUEUE_SIZE_3)).execute(any());
        }

        @Test
        void shouldReturnSuccessIsFalseWhenMessageIdIsNull() {

            //Act
            final AckCertainMessageResponse result = messageOperator.ackCertainMessage(null);

            //Assert
            assertThat(result.isSuccess()).isFalse();
        }

        @Test
        void shouldReturnSuccessIsFalseWhenMessageIdIsEmpty() {

            //Act
            final AckCertainMessageResponse result = messageOperator.ackCertainMessage("");

            //Assert
            assertThat(result.isSuccess()).isFalse();
        }

        @Test
        public void shouldNotCallExecuteIfQueueSizeIsNull() {
            //Arrange
            initializeQueueProperties(true);

            //Act
            final AckCertainMessageResponse response = messageOperator.ackCertainMessage(MESSAGE_ID);

            //Assert
            assertThat(response.isSuccess()).isFalse();

            verify(rabbitTemplate, times(0)).execute(any());
        }

        @Test
        public void shouldReturnFalseIfResponseFromBasicGetIsNull() {
            //Arrange
            initializeQueueProperties(false);

            //Act
            final AckCertainMessageResponse result = messageOperator.ackCertainMessage(MESSAGE_ID);

            //Assert
            assertThat(result.isSuccess()).isFalse();
        }
    }

    @Nested
    class republishMessage {

        @Test
        void shouldReturnSuccessIsFalseWhenMessageIdIsNull() {
            //Arrange

            //Act
            final RepublishMessageResponse result = messageOperator.republishMessage(null);

            //Assert
            assertThat(result.isSuccess()).isFalse();
        }

        @Test
        void shouldReturnSuccessIsFalseWhenMessageIdIsEmpty() {
            //Arrange

            //Act
            final RepublishMessageResponse result = messageOperator.republishMessage("");

            //Assert
            assertThat(result.isSuccess()).isFalse();
        }

        @Test
        void shouldReturnSuccessIsFalseIfNoQueueSize() {
            //Arrange
            initializeQueueProperties(true);

            //Act
            final RepublishMessageResponse result = messageOperator.republishMessage("123");

            //Assert
            assertThat(result.isSuccess()).isFalse();
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

            final HandleAckCertainMessage handleAckCertainMessage = new HandleAckCertainMessage(messageOperator, messagePropertiesConverter, messageConverterService, "");

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

            final HandleAckCertainMessage handleAckCertainMessage = new HandleAckCertainMessage(messageOperator, messagePropertiesConverter, messageConverterService, "");

            final Channel mockChannel = mock(Channel.class);

            when(mockChannel.basicGet(QUEUE_NAME, false)).thenReturn(null);

            //Act
            final String result = handleAckCertainMessage.doInRabbit(mockChannel);

            //Assert
            assertThat(result).isNull();
        }

        @Test
        void shouldCallBasicAckIfMessageIdExistsInQueue() throws Exception {
            //Arrange
            initializeQueueProperties(false);

            final long deliveryTag = 123L;
            final boolean redeliver = false;
            final String fingerprint = "12313920123912";
            final String messageId = String.format("f_%s", fingerprint);

            final MessagePropertiesConverter messagePropertiesConverter = new DefaultMessagePropertiesConverter();
            final MessageConverterService messageConverterService = mock(MessageConverterService.class);

            final VirgilMessage virgilMessage = ImmutableVirgilMessage.builder()
                .setBody("bodymessage")
                .setFingerprint(fingerprint)
                .setId(messageId)
                .build();

            when(messageConverterService.mapMessage(any())).thenReturn(virgilMessage);

            final HandleAckCertainMessage handleAckCertainMessage = new HandleAckCertainMessage(messageOperator, messagePropertiesConverter, messageConverterService, messageId);

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

        @Test
        void shouldSetMessageBeenAckdToTrue() throws Exception {
            //Arrange
            initializeQueueProperties(false);

            final long deliveryTag = 123L;
            final boolean redeliver = false;
            final String fingerprint = "12313920123912";
            final String messageId = String.format("f_%s", fingerprint);

            final MessagePropertiesConverter messagePropertiesConverter = new DefaultMessagePropertiesConverter();
            final MessageConverterService messageConverterService = mock(MessageConverterService.class);

            final VirgilMessage virgilMessage = ImmutableVirgilMessage.builder()
                .setBody("bodymessage")
                .setFingerprint(fingerprint)
                .setId(messageId)
                .build();

            when(messageConverterService.mapMessage(any())).thenReturn(virgilMessage);

            final HandleAckCertainMessage handleAckCertainMessage = new HandleAckCertainMessage(messageOperator, messagePropertiesConverter, messageConverterService, messageId);

            final GetResponse mockGetResponse = mock(GetResponse.class);
            when(mockGetResponse.getProps()).thenReturn(new BasicProperties());
            when(mockGetResponse.getEnvelope()).thenReturn(new Envelope(deliveryTag, redeliver, EXCHANGE_NAME, BINDING_KEY));


            final Channel mockChannel = mock(Channel.class);
            when(mockChannel.basicGet(QUEUE_NAME, false)).thenReturn(mockGetResponse);

            //Act
            handleAckCertainMessage.doInRabbit(mockChannel);

            //Assert
            assertThat(handleAckCertainMessage.hasMessageBeenAckd()).isTrue();
        }

        @Test
        void shouldSetAckMesageOnSuccess() throws Exception {
            //Arrange
            initializeQueueProperties(false);

            final long deliveryTag = 123L;
            final boolean redeliver = false;
            final String fingerprint = "12313920123912";
            final String messageId = String.format("f_%s", fingerprint);

            final MessagePropertiesConverter messagePropertiesConverter = new DefaultMessagePropertiesConverter();
            final MessageConverterService messageConverterService = mock(MessageConverterService.class);

            final VirgilMessage virgilMessage = ImmutableVirgilMessage.builder()
                .setBody("bodymessage")
                .setFingerprint(fingerprint)
                .setId(messageId)
                .build();

            when(messageConverterService.mapMessage(any())).thenReturn(virgilMessage);

            final HandleAckCertainMessage handleAckCertainMessage = new HandleAckCertainMessage(messageOperator, messagePropertiesConverter, messageConverterService, messageId);

            final GetResponse mockGetResponse = mock(GetResponse.class);
            when(mockGetResponse.getProps()).thenReturn(new BasicProperties());
            when(mockGetResponse.getEnvelope()).thenReturn(new Envelope(deliveryTag, redeliver, EXCHANGE_NAME, BINDING_KEY));


            final Channel mockChannel = mock(Channel.class);
            when(mockChannel.basicGet(QUEUE_NAME, false)).thenReturn(mockGetResponse);

            //Act
            handleAckCertainMessage.doInRabbit(mockChannel);

            //Assert
            assertThat(handleAckCertainMessage.getAckedMessage()).isNotNull();
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

            final HandleGetMessages handleGetMessages = new HandleGetMessages(messageOperator, messagePropertiesConverter, messageConverterService, 10);

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

            final HandleGetMessages handleGetMessages = new HandleGetMessages(messageOperator, messagePropertiesConverter, messageConverterService, 10);

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
                .setId(String.format("f_%s", fingerprint))
                .build();

            when(messageConverterService.mapMessage(any())).thenReturn(virgilMessage);

            final HandleGetMessages handleGetMessages = new HandleGetMessages(messageOperator, messagePropertiesConverter, messageConverterService, 10);

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
                    .setId(String.format("f_%s", fingerprint))
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
            final String messageId = String.format("f_%s", fingerprint);

            final MessageProperties mockMessageProperties = mock(MessageProperties.class);
            final MessagePropertiesConverter messagePropertiesConverter = mock(MessagePropertiesConverter.class);

            when(messagePropertiesConverter.toMessageProperties(any(), any(), any())).thenReturn(mockMessageProperties);

            final MessageConverterService messageConverterService = mock(MessageConverterService.class);

            final Message expectedMessage = new Message(body.getBytes(), mockMessageProperties);

            final VirgilMessage virgilMessage = ImmutableVirgilMessage.builder()
                .setBody(body)
                .setFingerprint(fingerprint)
                .setId(messageId)
                .build();

            when(messageConverterService.mapMessage(any())).thenReturn(virgilMessage);

            final HandleGetMessages handleGetMessages = new HandleGetMessages(messageOperator, messagePropertiesConverter, messageConverterService, 10);

            final GetResponse mockGetResponse = mock(GetResponse.class);
            when(mockGetResponse.getBody()).thenReturn(body.getBytes());
            when(mockGetResponse.getProps()).thenReturn(new BasicProperties());
            when(mockGetResponse.getEnvelope()).thenReturn(new Envelope(deliveryTag, redeliver, EXCHANGE_NAME, BINDING_KEY));


            final Channel mockChannel = mock(Channel.class);
            when(mockChannel.basicGet(QUEUE_NAME, false)).thenReturn(mockGetResponse);

            //Act
            handleGetMessages.doInRabbit(mockChannel);

            //Assert
            assertThat(handleGetMessages.getMessageLookup()).contains(entry(messageId, expectedMessage));
        }
    }

    @Nested
    class testHandleRepublishMessage {

        @Test
        void shouldPassFalseToAutoAckInBasicGet() throws Exception {
            //Arrange
            initializeQueueProperties(false);

            final MessagePropertiesConverter messagePropertiesConverter = new DefaultMessagePropertiesConverter();
            final MessageConverterService messageConverterService = mock(MessageConverterService.class);

            final HandleRepublishMessage handleRepublishMessage = new HandleRepublishMessage(messageOperator, messagePropertiesConverter, messageConverterService, "");

            final Channel mockChannel = mock(Channel.class);

            final ArgumentCaptor<Boolean> autoAckCapture = ArgumentCaptor.forClass(Boolean.class);

            when(mockChannel.basicGet(eq(QUEUE_NAME), autoAckCapture.capture())).thenReturn(null);

            //Act
            handleRepublishMessage.doInRabbit(mockChannel);

            //Assert
            assertThat(autoAckCapture.getValue()).isFalse();
        }

        @Test
        void shouldReturnNullWhenGetResponseIsNull() throws Exception {
            //Arrange
            initializeQueueProperties(false);

            final MessagePropertiesConverter messagePropertiesConverter = new DefaultMessagePropertiesConverter();
            final MessageConverterService messageConverterService = mock(MessageConverterService.class);

            final HandleRepublishMessage handleRepublishMessage = new HandleRepublishMessage(messageOperator, messagePropertiesConverter, messageConverterService, "");

            final Channel mockChannel = mock(Channel.class);

            final ArgumentCaptor<Boolean> autoAckCapture = ArgumentCaptor.forClass(Boolean.class);

            when(mockChannel.basicGet(eq(QUEUE_NAME), autoAckCapture.capture())).thenReturn(null);

            //Act
            final Void result = handleRepublishMessage.doInRabbit(mockChannel);

            //Assert
            assertThat(result).isNull();
        }

        @Test
        void shouldCallBasicAckWhenMessageIdMatches() throws Exception {
            //Arrange
            initializeQueueProperties(false);

            final long deliveryTag = 123L;
            final boolean redeliver = false;
            final String fingerprint = "12313920123912";
            final String messageId = String.format("f_%s", fingerprint);

            final MessagePropertiesConverter messagePropertiesConverter = new DefaultMessagePropertiesConverter();
            final MessageConverterService messageConverterService = mock(MessageConverterService.class);

            final VirgilMessage virgilMessage = ImmutableVirgilMessage.builder()
                .setBody("bodymessage")
                .setFingerprint(fingerprint)
                .setId(messageId)
                .build();

            when(messageConverterService.mapMessage(any())).thenReturn(virgilMessage);

            final HandleRepublishMessage handleRepublishMessage = new HandleRepublishMessage(messageOperator, messagePropertiesConverter, messageConverterService, messageId);

            final GetResponse mockGetResponse = mock(GetResponse.class);
            when(mockGetResponse.getProps()).thenReturn(new BasicProperties());
            when(mockGetResponse.getEnvelope()).thenReturn(new Envelope(deliveryTag, redeliver, EXCHANGE_NAME, BINDING_KEY));


            final Channel mockChannel = mock(Channel.class);
            when(mockChannel.basicGet(QUEUE_NAME, false)).thenReturn(mockGetResponse);

            doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), (Object) any());

            //Act
            final Void result = handleRepublishMessage.doInRabbit(mockChannel);

            //Assert
            assertThat(result).isNull();
            verify(mockChannel, times(1)).basicAck(deliveryTag, false);
        }

        @Test
        void shouldCallConvertAndSendWhenMessageIdMatches() throws Exception {
            //Arrange
            initializeQueueProperties(false);

            final long deliveryTag = 123L;
            final boolean redeliver = false;
            final String fingerprint = "12313920123912";
            final String messageId = String.format("f_%s", fingerprint);

            final MessagePropertiesConverter messagePropertiesConverter = new DefaultMessagePropertiesConverter();
            final MessageConverterService messageConverterService = mock(MessageConverterService.class);

            final VirgilMessage virgilMessage = ImmutableVirgilMessage.builder()
                .setBody("bodymessage")
                .setFingerprint(fingerprint)
                .setId(messageId)
                .build();

            when(messageConverterService.mapMessage(any())).thenReturn(virgilMessage);

            final HandleRepublishMessage handleRepublishMessage = new HandleRepublishMessage(messageOperator, messagePropertiesConverter, messageConverterService, messageId);

            final GetResponse mockGetResponse = mock(GetResponse.class);
            when(mockGetResponse.getProps()).thenReturn(new BasicProperties());
            when(mockGetResponse.getEnvelope()).thenReturn(new Envelope(deliveryTag, redeliver, EXCHANGE_NAME, BINDING_KEY));


            final Channel mockChannel = mock(Channel.class);
            when(mockChannel.basicGet(QUEUE_NAME, false)).thenReturn(mockGetResponse);

            doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), (Object) any());

            //Act
            final Void result = handleRepublishMessage.doInRabbit(mockChannel);

            //Assert
            assertThat(result).isNull();
            verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), (Object) any());
        }

        @Test
        void shouldSetMessageRepublishToTrueWhenMessageIdMatches() throws Exception {
            //Arrange
            initializeQueueProperties(false);

            final long deliveryTag = 123L;
            final boolean redeliver = false;
            final String fingerprint = "12313920123912";
            final String messageId = String.format("f_%s", fingerprint);

            final MessagePropertiesConverter messagePropertiesConverter = new DefaultMessagePropertiesConverter();
            final MessageConverterService messageConverterService = mock(MessageConverterService.class);

            final VirgilMessage virgilMessage = ImmutableVirgilMessage.builder()
                .setBody("bodymessage")
                .setFingerprint(fingerprint)
                .setId(messageId)
                .build();

            when(messageConverterService.mapMessage(any())).thenReturn(virgilMessage);

            final HandleRepublishMessage handleRepublishMessage = new HandleRepublishMessage(messageOperator, messagePropertiesConverter, messageConverterService, messageId);

            final GetResponse mockGetResponse = mock(GetResponse.class);
            when(mockGetResponse.getProps()).thenReturn(new BasicProperties());
            when(mockGetResponse.getEnvelope()).thenReturn(new Envelope(deliveryTag, redeliver, EXCHANGE_NAME, BINDING_KEY));


            final Channel mockChannel = mock(Channel.class);
            when(mockChannel.basicGet(QUEUE_NAME, false)).thenReturn(mockGetResponse);

            doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), (Object) any());

            //Act
            handleRepublishMessage.doInRabbit(mockChannel);

            //Assert
            assertThat(handleRepublishMessage.isRepublishSuccessful()).isTrue();
        }

        @Test
        void shouldSetMessageRepublishToFalseWhenMessageIdDoesNotMatch() throws Exception {
            //Arrange
            initializeQueueProperties(false);

            final long deliveryTag = 123L;
            final boolean redeliver = false;
            final String fingerprint = "12313920123912";
            final String messageId = String.format("f_%s", fingerprint);

            final MessagePropertiesConverter messagePropertiesConverter = new DefaultMessagePropertiesConverter();
            final MessageConverterService messageConverterService = mock(MessageConverterService.class);

            final VirgilMessage virgilMessage = ImmutableVirgilMessage.builder()
                .setBody("bodymessage")
                .setFingerprint(fingerprint)
                .setId(messageId)
                .build();

            when(messageConverterService.mapMessage(any())).thenReturn(virgilMessage);

            final HandleRepublishMessage handleRepublishMessage = new HandleRepublishMessage(messageOperator, messagePropertiesConverter, messageConverterService, messageId + "2");

            final GetResponse mockGetResponse = mock(GetResponse.class);
            when(mockGetResponse.getProps()).thenReturn(new BasicProperties());
            when(mockGetResponse.getEnvelope()).thenReturn(new Envelope(deliveryTag, redeliver, EXCHANGE_NAME, BINDING_KEY));


            final Channel mockChannel = mock(Channel.class);
            when(mockChannel.basicGet(QUEUE_NAME, false)).thenReturn(mockGetResponse);

            doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), (Object) any());

            //Act
            handleRepublishMessage.doInRabbit(mockChannel);

            //Assert
            assertThat(handleRepublishMessage.isRepublishSuccessful()).isFalse();
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
