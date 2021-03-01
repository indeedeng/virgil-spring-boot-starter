package com.indeed.virgil.spring.boot.starter.services;

import com.indeed.virgil.spring.boot.starter.config.VirgilPropertyConfig;
import com.indeed.virgil.spring.boot.starter.config.VirgilPropertyConfig.QueueProperties;
import com.indeed.virgil.spring.boot.starter.models.AckCertainMessageResponse;
import com.indeed.virgil.spring.boot.starter.models.ImmutableAckCertainMessageResponse;
import com.indeed.virgil.spring.boot.starter.models.ImmutableRepublishMessageResponse;
import com.indeed.virgil.spring.boot.starter.models.RepublishMessageResponse;
import com.indeed.virgil.spring.boot.starter.models.VirgilMessage;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.ChannelCallback;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;
import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class MessageOperator {

    private static final Logger LOG = LoggerFactory.getLogger(MessageOperator.class);

    private final VirgilPropertyConfig virgilPropertyConfig;
    private final RabbitMqConnectionService rabbitMqConnectionService;
    private final MessageConverterService messageConverterService;

    private volatile MessagePropertiesConverter messagePropertiesConverter = new DefaultMessagePropertiesConverter();

    public MessageOperator(
        final VirgilPropertyConfig virgilPropertyConfig,
        final RabbitMqConnectionService rabbitMqConnectionService,
        final MessageConverterService messageConverterService
    ) {
        this.virgilPropertyConfig = virgilPropertyConfig;
        this.rabbitMqConnectionService = rabbitMqConnectionService;
        this.messageConverterService = messageConverterService;
    }

    /**
     * @param queueId Queue Property Key, this is not the actual name of the queue
     * @return queueSize
     */
    @Nullable
    public Integer getQueueSize(final String queueId) {
        final QueueProperties queueProperties = virgilPropertyConfig.getQueueProperties(queueId);
        if(queueProperties == null) {
            LOG.error("QueueProperties is null. QueueId: {}", queueId);
            return null;
        }

        final AmqpAdmin amqpAdmin = rabbitMqConnectionService.getReadAmqpAdmin(queueId);

        final Properties properties = amqpAdmin.getQueueProperties(queueProperties.getReadName());

        if (properties == null) {
            LOG.error("Amqp queue properties is null for queueId: {}", queueId);
            return null;
        }

        final Object queueMessageCount = properties.get(RabbitAdmin.QUEUE_MESSAGE_COUNT);

        Integer queueSize;
        if (queueMessageCount instanceof String) {
            queueSize = Integer.valueOf((String) queueMessageCount);
        } else {
            queueSize = (Integer) queueMessageCount;
        }

        return queueSize;
    }

    /**
     * Retrieves messages from the DLQ up to the limit passed in
     *
     * @param queueId Queue Property Key, this is not the actual name of the queue
     * @param limit Limits the number of messages returned from DLQ
     * @return List of messages
     */
    public List<VirgilMessage> getMessages(final String queueId, @Nullable final Integer limit) {
        final Integer queueSize = getQueueSize(queueId);
        if (queueSize == null) {
            LOG.error("Queue size is null.");
            return Collections.emptyList();
        }

        final QueueProperties queueProperties = virgilPropertyConfig.getQueueProperties(queueId);
        if(queueProperties == null) {
            LOG.error("QueueProperties is null. Returning Empty List. QueueId: {}", queueId);
            return Collections.emptyList();
        }

        try {
            final int numToRetrieve = Optional.ofNullable(limit)
                .filter(value -> value > 0)
                .orElse(queueSize);

            final HandleGetMessages handleGetMessages = new HandleGetMessages(messagePropertiesConverter, messageConverterService, queueProperties, numToRetrieve);
            for (int i = 0; i < numToRetrieve; i++) {
                rabbitMqConnectionService.getReadRabbitTemplate(queueId).execute(handleGetMessages);
            }

            return handleGetMessages.getDlqMessages();
        } finally {

            //TODO: Need to move this logic into RabbitMqConnectionService so it flushes the connection from cache

            // Close connection so 'Unacked' messages could be put back to 'Ready' state
            // Connection will be automatically reestablished on next Actuator endpoint invocation
            rabbitMqConnectionService.destroyReadConnection(queueId);
        }
    }

    /**
     * Drop all messages in the queue.
     *
     * @param queueId Queue Property Key, this is not the actual name of the queue
     * @return
     */
    public boolean dropMessages(final String queueId) {

        final Integer queueSize = getQueueSize(queueId);
        if (queueSize == null) {
            LOG.error("Queue size is null.");
            return false;
        }

        final QueueProperties queueProperties = virgilPropertyConfig.getQueueProperties(queueId);
        if(queueProperties == null) {
            LOG.error("QueueProperties is null. QueueId: {}", queueId);
            return false;
        }

        final HandleDropMessages handleDropMessages = new HandleDropMessages(queueProperties.getReadName());

        final RabbitTemplate rabbitTemplate = rabbitMqConnectionService.getReadRabbitTemplate(queueId);

        rabbitTemplate.execute(handleDropMessages);

        return true;
    }

    /**
     * Acknowledges a message on the DLQ
     *
     * @param queueId Queue Property Key, this is not the actual name of the queue
     * @param messageId
     * @return
     */
    public AckCertainMessageResponse ackCertainMessage(final String queueId, final String messageId) {

        if (StringUtils.isEmpty(messageId)) {
            LOG.error("messageId is null or empty. QueueId: {}", queueId);
            return ImmutableAckCertainMessageResponse.builder()
                .setSuccess(false)
                .build();
        }

        final Integer queueSize = getQueueSize(queueId);
        if (queueSize == null) {
            LOG.error("Queue size is null. QueueId: {}", queueId);
            return ImmutableAckCertainMessageResponse.builder()
                .setSuccess(false)
                .build();
        }

        final QueueProperties queueProperties = virgilPropertyConfig.getQueueProperties(queueId);
        if(queueProperties == null) {
            LOG.error("QueueProperties is null. QueueId: {}", queueId);
            return ImmutableAckCertainMessageResponse.builder()
                .setSuccess(false)
                .build();
        }

        try {

            final HandleAckCertainMessage handleAckCertainMessage = new HandleAckCertainMessage(messagePropertiesConverter, messageConverterService, queueProperties, messageId);
            final RabbitTemplate rabbitTemplate = rabbitMqConnectionService.getReadRabbitTemplate(queueId);
            for (int i = 0; i < queueSize; i++) {
                rabbitTemplate.execute(handleAckCertainMessage);

                //break out of loop if we have ack'd the message
                if (handleAckCertainMessage.hasMessageBeenAckd()) {
                    break;
                }
            }

            final ImmutableAckCertainMessageResponse.Builder responseBuilder = ImmutableAckCertainMessageResponse.builder()
                .setSuccess(handleAckCertainMessage.hasMessageBeenAckd());

            if (handleAckCertainMessage.getAckedMessage() != null) {
                responseBuilder.setMessage(handleAckCertainMessage.getAckedMessage());
            }

            return responseBuilder.build();
        } finally {
            //TODO: Need to move this logic into RabbitMqConnectionService so it flushes the connection from cache

            // Close connection so 'Unacked' messages could be put back to 'Ready' state
            // Connection will be automatically reestablished on next Actuator endpoint invocation
            rabbitMqConnectionService.destroyReadConnection(queueId);
        }
    }

    /**
     *
     * @param queueId Queue Property Key, this is not the actual name of the queue
     * @param messageId
     * @return
     */
    public RepublishMessageResponse republishMessage(final String queueId, final String messageId) {

        if (StringUtils.isEmpty(messageId)) {
            LOG.warn("messageId is null or empty.");
            return ImmutableRepublishMessageResponse.builder()
                .setSuccess(false)
                .build();
        }

        final Integer queueSize = getQueueSize(queueId);
        if (queueSize == null) {
            LOG.warn("Queue size is null.");
            return ImmutableRepublishMessageResponse.builder()
                .setSuccess(false)
                .build();
        }

        try {
            final QueueProperties queueProperties = virgilPropertyConfig.getQueueProperties(queueId);
            if(queueProperties == null) {
                LOG.error("QueueProperties is null. QueueId: {}", queueId);
                return ImmutableRepublishMessageResponse.builder()
                    .setSuccess(false)
                    .build();
            }

            final HandleRepublishMessage handleRepublishMessage = new HandleRepublishMessage(rabbitMqConnectionService, messagePropertiesConverter, messageConverterService, queueProperties, queueId, messageId);
            final RabbitTemplate rabbitTemplate = rabbitMqConnectionService.getReadRabbitTemplate(queueId);
            for (int i = 0; i < queueSize; i++) {
                rabbitTemplate.execute(handleRepublishMessage);
            }

            return ImmutableRepublishMessageResponse.builder()
                .setSuccess(handleRepublishMessage.isRepublishSuccessful())
                .build();
        } finally {
            //TODO: Need to move this logic into RabbitMqConnectionService so it flushes the connection from cache

            // Close connection so 'Unacked' messages could be put back to 'Ready' state
            // Connection will be automatically reestablished on next Actuator endpoint invocation
            rabbitMqConnectionService.destroyReadConnection(queueId);
        }
    }

    protected static class HandleRepublishMessage implements ChannelCallback<Void> {

        private final RabbitMqConnectionService rabbitMqConnectionService;
        private final MessagePropertiesConverter messagePropertiesConverter;
        private final MessageConverterService messageConverterService;
        private final QueueProperties queueProperties;
        private final String queueName;
        private final String messageId;

        private boolean messageRepublished;

        public HandleRepublishMessage(
            final RabbitMqConnectionService rabbitMqConnectionService,
            final MessagePropertiesConverter messagePropertiesConverter,
            final MessageConverterService messageConverterService,
            final QueueProperties queueProperties,
            final String queueName,
            final String messageId
        ) {
            this.rabbitMqConnectionService = rabbitMqConnectionService;
            this.messagePropertiesConverter = messagePropertiesConverter;
            this.messageConverterService = messageConverterService;
            this.queueProperties = queueProperties;
            this.queueName = queueName;
            this.messageId = messageId;
        }

        @Override
        public Void doInRabbit(final Channel channel) throws Exception {
            final GetResponse response = channel.basicGet(queueProperties.getReadName(), false);
            if (response == null) {
                return null;
            }

            final MessageProperties messageProps =
                messagePropertiesConverter.toMessageProperties(response.getProps(), response.getEnvelope(), "UTF-8");
            final Message message = new Message(response.getBody(), messageProps);
            final VirgilMessage virgilMessage = messageConverterService.mapMessage(message);

            if (messageId.equals(virgilMessage.getId())) {
                channel.basicAck(response.getEnvelope().getDeliveryTag(), false);

                final RabbitTemplate rabbitTemplate = rabbitMqConnectionService.getReadRabbitTemplate(queueName);

                rabbitTemplate.convertAndSend(queueProperties.getReadBinderProperties().getName(), queueProperties.getRepublishBindingRoutingKey(), message);
                messageRepublished = true;
            }
            return null;
        }

        /**
         * Returns true if the message was found and ack'd, otherwise returns false
         *
         * @return boolean
         */
        public boolean isRepublishSuccessful() {
            return this.messageRepublished;
        }
    }

    protected static class HandleAckCertainMessage implements ChannelCallback<String> {

        private final MessagePropertiesConverter messagePropertiesConverter;
        private final MessageConverterService messageConverterService;
        private final QueueProperties queueProperties;
        private final String messageId;

        @Nullable
        private Message ackedMessage;
        private boolean messageFound = false;

        public HandleAckCertainMessage(
            final MessagePropertiesConverter messagePropertiesConverter,
            final MessageConverterService messageConverterService,
            final QueueProperties queueProperties,
            final String messageId
        ) {
            this.messagePropertiesConverter = messagePropertiesConverter;
            this.messageConverterService = messageConverterService;
            this.queueProperties = queueProperties;
            this.messageId = messageId;
        }

        @Override
        public String doInRabbit(final Channel channel) throws Exception {
            final GetResponse response = channel.basicGet(queueProperties.getReadName(), false);
            if (response == null) {
                return null;
            }

            final MessageProperties messageProps =
                messagePropertiesConverter.toMessageProperties(response.getProps(), response.getEnvelope(), "UTF-8");
            final Message message = new Message(response.getBody(), messageProps);
            final VirgilMessage virgilMessage = messageConverterService.mapMessage(message);

            if (messageId.equals(virgilMessage.getId())) {
                channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
                ackedMessage = message;
                messageFound = true;
            }
            return null;
        }

        /**
         * Returns true if the message was found and ack'd, otherwise returns false
         *
         * @return
         */
        public boolean hasMessageBeenAckd() {
            return this.messageFound;
        }

        /**
         * Returns ackedMessage if message has been ack'd otherwise returns null
         *
         * @return
         */
        @Nullable
        public Message getAckedMessage() {
            return this.ackedMessage;
        }
    }

    protected static class HandleDropMessages implements ChannelCallback<Void> {

        private final String queueName;

        /**
         *
         * @param queueName Name of the queue that will be purged
         */
        public HandleDropMessages(
            final String queueName
        ) {
            this.queueName = queueName;
        }

        @Override
        public Void doInRabbit(final Channel channel) throws Exception {
            LOG.info("Purging the queue. Queue: {}", queueName);
            channel.queuePurge(queueName);
            return null;
        }
    }

    protected static class HandleGetMessages implements ChannelCallback<Void> {

        private final MessagePropertiesConverter messagePropertiesConverter;
        private final MessageConverterService messageConverterService;
        private final QueueProperties queueProperties;

        private final List<VirgilMessage> dlqMessages;
        private final Map<String, Message> messageLookup;

        public HandleGetMessages(
            final MessagePropertiesConverter messagePropertiesConverter,
            final MessageConverterService messageConverterService,
            final QueueProperties queueProperties,
            final int numToRetrieve
        ) {
            this.messagePropertiesConverter = messagePropertiesConverter;
            this.messageConverterService = messageConverterService;
            this.queueProperties = queueProperties;

            this.dlqMessages = new ArrayList<>(numToRetrieve);
            this.messageLookup = new HashMap<>(numToRetrieve);
        }

        @Override
        public Void doInRabbit(final Channel channel) throws Exception {
            final GetResponse response = channel.basicGet(queueProperties.getReadName(), false);
            if (response == null) {
                return null;
            }

            final MessageProperties messageProps =
                messagePropertiesConverter.toMessageProperties(response.getProps(), response.getEnvelope(), "UTF-8");
            final Message message = new Message(response.getBody(), messageProps);
            final VirgilMessage virgilMessage = messageConverterService.mapMessage(message);

            dlqMessages.add(virgilMessage);
            messageLookup.put(virgilMessage.getId(), message);
            return null;
        }

        public List<VirgilMessage> getDlqMessages() {
            return dlqMessages;
        }

        public Map<String, Message> getMessageLookup() {
            return messageLookup;
        }
    }

//    private String getReadBindingKey() {
//        return virgilPropertyConfig.getDefaultQueue().getRepublishBindingRoutingKey();
//    }
//
//    private RabbitTemplate getReadRabbitTemplate(final String queueName) {
//        final QueueProperties queueProperties = virgilPropertyConfig.getQueueProperties(queueName);
//        return rabbitMqConnectionService.getRabbitTemplate(queueProperties.getReadBinderName());
//    }
//
//    private AmqpAdmin getReadAmqpAdmin(final String queueName) {
//        final QueueProperties queueProperties = virgilPropertyConfig.getQueueProperties(queueName);
//        return rabbitMqConnectionService.getAmqpAdmin(queueProperties.getReadBinderName());
//    }
//
//    private void destroyReadConnection(final String queueName) {
//        final QueueProperties queueProperties = virgilPropertyConfig.getQueueProperties(queueName);
//
//        rabbitMqConnectionService.destroyConnectionsByName(queueProperties.getReadBinderName());
//    }
}
