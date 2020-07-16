package com.indeed.virgil.spring.boot.starter.services;

import com.indeed.virgil.spring.boot.starter.config.VirgilPropertyConfig;
import com.indeed.virgil.spring.boot.starter.config.VirgilPropertyConfig.QueueProperties;
import com.indeed.virgil.spring.boot.starter.models.AckCertainMessageResponse;
import com.indeed.virgil.spring.boot.starter.models.ImmutableAckCertainMessageResponse;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

// TODO think about lock, consider add lock (if needed)

public class MessageOperator {

    private static final Logger LOG = LoggerFactory.getLogger(MessageOperator.class);
    private static int DEFAULT_MESSAGE_SIZE = 200;

    private final VirgilPropertyConfig virgilPropertyConfig;
    private final RabbitMqConnectionService rabbitMqConnectionService;
    private final MessageConverterService messageConverterService;

    private volatile MessagePropertiesConverter messagePropertiesConverter = new DefaultMessagePropertiesConverter();

    private Map<String, Message> messageCache;


    public MessageOperator(
        final VirgilPropertyConfig virgilPropertyConfig,
        final RabbitMqConnectionService rabbitMqConnectionService,
        final MessageConverterService messageConverterService) {
        this.virgilPropertyConfig = virgilPropertyConfig;
        this.rabbitMqConnectionService = rabbitMqConnectionService;
        this.messageConverterService = messageConverterService;
    }

    @Nullable
    public Integer getQueueSize() {

        final Properties properties = getReadAmqpAdmin().getQueueProperties(getReadQueueName());

        if (properties == null) {
            LOG.error("Amqp queue properties is null for queue name: {}", getReadQueueName());
            return null;
        }

        return (Integer) properties.get(RabbitAdmin.QUEUE_MESSAGE_COUNT);
    }

    public List<VirgilMessage> getMessages(@Nullable final Integer limit) {

        // initialize messageLookup on each method call; messageLookup is acting as server side cache for messages.
        messageCache = new ConcurrentHashMap<>();

        final Integer queueSize = getQueueSize();
        if (queueSize == null) {
            LOG.error("Queue size is null.");
            return null;
        }
        final int numToRetrieve = Optional.ofNullable(limit)
            .filter(value -> value > 0)
            .orElse(queueSize);

        final HandleGetMessages handleGetMessages = new HandleGetMessages(this, messagePropertiesConverter, messageConverterService, numToRetrieve);
        for (int i = 0; i < numToRetrieve; i++) {
            getReadRabbitTemplate().execute(handleGetMessages);
        }

        messageCache.putAll(handleGetMessages.getMessageLookup());

        //TODO: Need to move this logic into RabbitMqConnectionService so it flushes the connection from cache

        // Close connection so 'Unacked' messages could be put back to 'Ready' state
        // Connection will be automatically reestablished on next Actuator endpoint invocation
        destroyReadConnection();

        return handleGetMessages.getDlqMessages();
    }

    public boolean publishMessage(final Message msg) {
        getReadRabbitTemplate().convertAndSend(getReadExchangeName(), getReadBindingKey(), msg);
        return true;
    }

    /**
     * take messageId from UI, lookup from server side message cache for message body and publish
     * @param messageId
     * @return
     */
    public boolean publishCertainMessage(final String messageId) {

        if (StringUtils.isEmpty(messageId)) {
            LOG.error("messageId is null or empty.");
            return false;
        }

        if ((messageCache == null) || !messageCache.containsKey(messageId)) {
            // re-generate the messageCache again
            getMessages(DEFAULT_MESSAGE_SIZE);
            if (!messageCache.containsKey(messageId)) {
                LOG.error("Can not identify message with target messageId: " + messageId);
                return false;
            }
        }



        getReadRabbitTemplate().convertAndSend(getReadExchangeName(), getReadBindingKey(), messageCache.get(messageId));
        return true;
    }

    /**
     * Drop all messages in the queue.
     * @return
     */
    public boolean dropMessages() {

        final Integer queueSize = getQueueSize();
        if (queueSize == null) {
            LOG.error("Queue size is null.");
            return false;
        }

        final HandleDropMessages handleDropMessages = new HandleDropMessages(this);

        getReadRabbitTemplate().execute(handleDropMessages);

        return true;
    }

    /**
     * Acknowledges a message on the DLQ
     * @param messageId
     * @return
     */
    public AckCertainMessageResponse ackCertainMessage(final String messageId) {

        if (StringUtils.isEmpty(messageId)) {
            LOG.error("messageId is null or empty.");
            return ImmutableAckCertainMessageResponse.builder()
                .setSuccess(false)
                .build();
        }

        final Integer queueSize = getQueueSize();
        if (queueSize == null) {
            LOG.error("Queue size is null.");
            return ImmutableAckCertainMessageResponse.builder()
                .setSuccess(false)
                .build();
        }

        final HandleAckCertainMessage handleAckCertainMessage = new HandleAckCertainMessage(this, messagePropertiesConverter, messageConverterService, messageId);
        for (int i = 0; i < queueSize; i++) {
            getReadRabbitTemplate().execute(handleAckCertainMessage);
        }

        //TODO: Need to move this logic into RabbitMqConnectionService so it flushes the connection from cache

        // Close connection so 'Unacked' messages could be put back to 'Ready' state
        // Connection will be automatically reestablished on next Actuator endpoint invocation
        destroyReadConnection();

        final ImmutableAckCertainMessageResponse.Builder responseBuilder = ImmutableAckCertainMessageResponse.builder()
            .setSuccess(handleAckCertainMessage.hasMessageBeenAckd());

        if(handleAckCertainMessage.getAckedMessage() != null) {
            responseBuilder.setMessage(handleAckCertainMessage.getAckedMessage());
        }

        return responseBuilder.build();
    }

    public boolean republishMessage(final String messageId) {

        if (StringUtils.isEmpty(messageId)) {
            LOG.warn("messageId is null or empty.");
            return false;
        }

        final Integer queueSize = getQueueSize();
        if (queueSize == null) {
            LOG.warn("Queue size is null.");
            return false;
        }

        final HandleRepublishMessage handleRepublishMessage = new HandleRepublishMessage(this, messagePropertiesConverter, messageConverterService, messageId);
        for (int i = 0; i < queueSize; i++) {
            getReadRabbitTemplate().execute(handleRepublishMessage);
        }

        return handleRepublishMessage.isRepublishSuccessful();
    }

    protected static class HandleRepublishMessage implements  ChannelCallback<Void> {

        private final MessageOperator messageOperator;
        private final MessagePropertiesConverter messagePropertiesConverter;
        private final MessageConverterService messageConverterService;
        private final String messageId;

        private boolean messageRepublished;

        public HandleRepublishMessage(
            final MessageOperator messageOperator,
            final MessagePropertiesConverter messagePropertiesConverter,
            final MessageConverterService messageConverterService,
            final String messageId
        ) {
            this.messageOperator = messageOperator;
            this.messagePropertiesConverter = messagePropertiesConverter;
            this.messageConverterService = messageConverterService;
            this.messageId = messageId;
        }

        @Override
        public Void doInRabbit(Channel channel) throws Exception {
            final GetResponse response = channel.basicGet(messageOperator.getReadQueueName(), false);
            if(response == null) {
                return null;
            }

            final MessageProperties messageProps =
                messagePropertiesConverter.toMessageProperties(response.getProps(), response.getEnvelope(), "UTF-8");
            final Message message = new Message(response.getBody(), messageProps);
            final VirgilMessage virgilMessage = messageConverterService.mapMessage(message);

            if (messageId.equals(virgilMessage.getId())) {
                channel.basicAck(response.getEnvelope().getDeliveryTag(), false);

                messageOperator.getReadRabbitTemplate().convertAndSend(messageOperator.getReadExchangeName(), messageOperator.getReadBindingKey(), message);
                messageRepublished = true;
            }
            return null;
        }

        /**
         * Returns true if the message was found and ack'd, otherwise returns false
         * @return
         */
        public boolean isRepublishSuccessful() {
            return this.messageRepublished;
        }
    }

    protected static class HandleAckCertainMessage implements ChannelCallback<String> {

        private final MessageOperator messageOperator;
        private final MessagePropertiesConverter messagePropertiesConverter;
        private final MessageConverterService messageConverterService;
        private final String messageId;

        private Message ackedMessage;
        private boolean messageFound = false;

        public HandleAckCertainMessage(
            final MessageOperator messageOperator,
            final MessagePropertiesConverter messagePropertiesConverter,
            final MessageConverterService messageConverterService,
            final String messageId
        ) {
            this.messageOperator = messageOperator;
            this.messagePropertiesConverter = messagePropertiesConverter;
            this.messageConverterService = messageConverterService;
            this.messageId = messageId;
        }

        @Override
        public String doInRabbit(final Channel channel) throws Exception {
            final GetResponse response = channel.basicGet(messageOperator.getReadQueueName(), false);
            if(response == null) {
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
         * @return
         */
        public boolean hasMessageBeenAckd() {
            return this.messageFound;
        }

        /**
         * Returns ackedMessage if message has been ack'd otherwise returns null
         * @return
         */
        public Message getAckedMessage() { return this.ackedMessage; }
    }

    protected static class HandleDropMessages implements ChannelCallback<Void> {

        private final MessageOperator messageOperator;

        public HandleDropMessages(
            final MessageOperator messageOperator
        ) {
            this.messageOperator = messageOperator;
        }

        @Override
        public Void doInRabbit(final Channel channel) throws Exception {
            LOG.info("Purging the queue");
            channel.queuePurge(messageOperator.getReadQueueName());

            return null;
        }
    }

    protected static class HandleGetMessages implements ChannelCallback<Void> {

        private final MessageOperator messageOperator;
        private final MessagePropertiesConverter messagePropertiesConverter;
        private final MessageConverterService messageConverterService;

        private final List<VirgilMessage> dlqMessages;
        private final Map<String, Message> messageLookup;

        public HandleGetMessages(
            final MessageOperator messageOperator,
            final MessagePropertiesConverter messagePropertiesConverter,
            final MessageConverterService messageConverterService,
            final int numToRetrieve
        ) {
            this.messageOperator = messageOperator;
            this.messagePropertiesConverter = messagePropertiesConverter;
            this.messageConverterService = messageConverterService;

            this.dlqMessages = new ArrayList<>(numToRetrieve);
            this.messageLookup = new HashMap<>(numToRetrieve);
        }

        @Override
        public Void doInRabbit(final Channel channel) throws Exception {
            final GetResponse response = channel.basicGet(messageOperator.getReadQueueName(), false);
            if(response == null) {
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

    private String getReadQueueName() {
        return virgilPropertyConfig.getDefaultQueue().getReadName();
    }

    private String getReadExchangeName() {

        return virgilPropertyConfig.getDefaultQueue().getReadBinderProperties().getName();
    }

    private String getReadBindingKey() {
        return virgilPropertyConfig.getDefaultQueue().getRepublishBindingRoutingKey();
    }

    private RabbitTemplate getReadRabbitTemplate() {
        final QueueProperties queueProperties = virgilPropertyConfig.getDefaultQueue();
        return rabbitMqConnectionService.getRabbitTemplate(queueProperties.getReadBinderName());
    }

    private AmqpAdmin getReadAmqpAdmin() {
        final QueueProperties queueProperties = virgilPropertyConfig.getDefaultQueue();
        return rabbitMqConnectionService.getAmqpAdmin(queueProperties.getReadBinderName());
    }

    private void destroyReadConnection() {
        final QueueProperties queueProperties = virgilPropertyConfig.getDefaultQueue();

        rabbitMqConnectionService.destroyConnectionsByName(queueProperties.getReadBinderName());
    }

    // VisibleForTesting
    void setMessageCache(final Map<String, Message> messageCache) {
        this.messageCache = messageCache;
    }
}
