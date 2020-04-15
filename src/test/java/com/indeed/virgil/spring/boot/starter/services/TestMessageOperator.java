package com.indeed.virgil.spring.boot.starter.services;

import com.indeed.virgil.spring.boot.starter.config.VirgilPropertyConfig;
import com.indeed.virgil.spring.boot.starter.models.VirgilMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestMessageOperator {

    private static final String QUEUE_NAME = "default-queue-name";
    private static final String BINDER_NAME = "default-binder-name";
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

    @Test
    public void testGetQueueSize() {

        initializeQueueProperties(false);

        assertEquals(QUEUE_SIZE_3, messageOperator.getQueueSize());
    }

    @Test
    public void testGetQueueSizeQueueNotExist() {

        initializeQueueProperties(true);

        assertNull(messageOperator.getQueueSize());
    }

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
    public void testPublishCertainMessageInvalidMessageCache() {
        // null; messageCache intentionally not populated
        assertFalse(messageOperator.publishCertainMessage(FINGER_PRINT));

        // not contain; messageCache intentionally initialized as empty
        messageOperator.setMessageCache(new HashMap<>());
        assertFalse(messageOperator.publishCertainMessage(FINGER_PRINT));
    }

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

    @Test
    public void testAckCertainMessage() {

        initializeQueueProperties(false);

        assertTrue(messageOperator.ackCertainMessage(FINGER_PRINT));

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
    public void testAckCertainMessageQueueNotExist() {

        initializeQueueProperties(true);

        assertFalse(messageOperator.ackCertainMessage(FINGER_PRINT));

        verify(rabbitTemplate, times(QUEUE_SIZE_0)).execute(any());
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
