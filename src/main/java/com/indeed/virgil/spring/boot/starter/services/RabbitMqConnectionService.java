package com.indeed.virgil.spring.boot.starter.services;

import com.indeed.virgil.spring.boot.starter.config.VirgilPropertyConfig;
import com.indeed.virgil.spring.boot.starter.config.VirgilPropertyConfig.BinderProperties;
import com.indeed.virgil.spring.boot.starter.config.VirgilPropertyConfig.QueueProperties;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.AbstractConnectionFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * This service will create the necessary connections to manage RabbitMQ in a dynamic way. In V2, this service will be used to dynamically
 * manage multiple-rabbitMQ connections
 * <p>
 * This service assumes that all connections will be destroyed in the same thread that created them, since this is the underlying principal
 * that is allowing us to view the queue without disturbing it.
 */
public class RabbitMqConnectionService {

    private final VirgilPropertyConfig virgilPropertyConfig;

    private final ThreadLocal<Map<String, AbstractConnectionFactory>> cachingConnectionFactoryLookup = new ThreadLocal<>();
    private final ThreadLocal<Map<String, AmqpAdmin>> amqpAdminLookup = new ThreadLocal<>();
    private final ThreadLocal<Map<String, RabbitTemplate>> rabbitTemplateLookup = new ThreadLocal<>();

    /**
     *
     * @param virgilPropertyConfig Virgil Properties
     */
    public RabbitMqConnectionService(
        final VirgilPropertyConfig virgilPropertyConfig
    ) {
        this.virgilPropertyConfig = virgilPropertyConfig;
    }

    /**
     *
     * @param queueName Queue Property Key, this is not the actual name of the queue
     * @return BindingKey
     */
    public String getReadBindingKey(final String queueName) {
        final QueueProperties queueProperties = virgilPropertyConfig.getQueueProperties(queueName);
        return queueProperties.getRepublishBindingRoutingKey();
    }

    /**
     *
     * @param queueName Queue Property Key, this is not the actual name of the queue
     * @return RabbitTemplate
     */
    public RabbitTemplate getReadRabbitTemplate(final String queueName) {
        final QueueProperties queueProperties = virgilPropertyConfig.getQueueProperties(queueName);
        return getRabbitTemplate(queueProperties.getReadBinderName());
    }

    /**
     *
     * @param queueName Queue Property Key, this is not the actual name of the queue
     * @return
     */
    public AmqpAdmin getReadAmqpAdmin(final String queueName) {
        final QueueProperties queueProperties = virgilPropertyConfig.getQueueProperties(queueName);
        return getAmqpAdmin(queueProperties.getReadBinderName());
    }

    /**
     *
     * @param queueName Queue Property Key, this is not the actual name of the queue
     */
    public void destroyReadConnection(final String queueName) {
        final QueueProperties queueProperties = virgilPropertyConfig.getQueueProperties(queueName);

        destroyConnectionsByName(queueProperties.getReadBinderName());
    }

    /**
     *
     * @param binderName Name of the RabbitMq Exchange
     * @return
     */
    public AmqpAdmin getAmqpAdmin(final String binderName) {
        AmqpAdmin amqpAdmin = getCachedAmqpAdmin(binderName);
        if (amqpAdmin != null) {
            return amqpAdmin;
        }

        amqpAdmin = new RabbitAdmin(getConnectionFactory(binderName));
        updateCachedAmqpAdmin(binderName, amqpAdmin);

        return amqpAdmin;
    }

    public RabbitTemplate getRabbitTemplate(final String binderName) {
        RabbitTemplate rabbitTemplate = getCachedRabbitTemplate(binderName);
        if (rabbitTemplate != null) {
            return rabbitTemplate;
        }

        rabbitTemplate = new RabbitTemplate(getConnectionFactory(binderName));
        updateCachedRabbitTemplate(binderName, rabbitTemplate);

        return rabbitTemplate;
    }

    private AbstractConnectionFactory getConnectionFactory(final String binderName) {
        AbstractConnectionFactory cachedAbstractConnectionFactory = getCachedConnectionFactory(binderName);
        if (cachedAbstractConnectionFactory != null) {
            return cachedAbstractConnectionFactory;
        }

        final BinderProperties binderProperties = virgilPropertyConfig.getBinderProperties(binderName);
        if (binderProperties == null) {
            return null;
        }

        final CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();

        final String addresses = binderProperties.getRabbitProperties().determineAddresses();
        cachingConnectionFactory.setAddresses(addresses);

        cachingConnectionFactory.setUsername(binderProperties.getRabbitProperties().determineUsername());
        cachingConnectionFactory.setPassword(binderProperties.getRabbitProperties().determinePassword());

        cachingConnectionFactory.setVirtualHost(binderProperties.getRabbitProperties().determineVirtualHost());
        cachingConnectionFactory.setPublisherReturns(true);

        updateCachedConnectionFactory(binderName, cachingConnectionFactory);

        return cachingConnectionFactory;
    }

    /**
     * Destroys the ConnectionFactory associated with the BinderName along with removing the cached AmqpAdmin and RabbitTemplate from cache
     *
     * @param binderName Name of the RabbitMq Exchange
     */
    public void destroyConnectionsByName(final String binderName) {
        //flush cached amqpAdmin and rabbitTemplate instances
        if (amqpAdminLookup.get() != null) {
            amqpAdminLookup.get().remove(binderName);
        }

        if (rabbitTemplateLookup.get() != null) {
            ((CachingConnectionFactory) rabbitTemplateLookup.get().get(binderName).getConnectionFactory()).destroy();
            rabbitTemplateLookup.get().remove(binderName);
        }

        //destroy cachingConnectionFactory
        final CachingConnectionFactory connectionFactory = (CachingConnectionFactory) getCachedConnectionFactory(binderName);
        if (connectionFactory != null) {
            connectionFactory.destroy();

            //remove cachingConnectionFactory instance from cache
            cachingConnectionFactoryLookup.get().remove(binderName);
        }
    }

    @Nullable
    private AmqpAdmin getCachedAmqpAdmin(final String binderName) {
        Map<String, AmqpAdmin> lookup = amqpAdminLookup.get();
        if (lookup == null) {
            amqpAdminLookup.set(new HashMap<>());
            lookup = amqpAdminLookup.get();
        }

        return lookup.getOrDefault(binderName, null);
    }

    private void updateCachedAmqpAdmin(final String binderName, final AmqpAdmin amqpAdmin) {
        if (amqpAdminLookup.get() == null) {
            amqpAdminLookup.set(new HashMap<>());
        }
        amqpAdminLookup.get().put(binderName, amqpAdmin);
    }

    @Nullable
    private RabbitTemplate getCachedRabbitTemplate(final String binderName) {
        Map<String, RabbitTemplate> lookup = rabbitTemplateLookup.get();
        if (lookup == null) {
            rabbitTemplateLookup.set(new HashMap<>());
            lookup = rabbitTemplateLookup.get();
        }

        return lookup.getOrDefault(binderName, null);
    }

    private void updateCachedRabbitTemplate(final String binderName, final RabbitTemplate rabbitTemplate) {
        if (rabbitTemplateLookup.get() == null) {
            rabbitTemplateLookup.set(new HashMap<>());
        }
        rabbitTemplateLookup.get().put(binderName, rabbitTemplate);
    }

    @Nullable
    private AbstractConnectionFactory getCachedConnectionFactory(final String binderName) {
        Map<String, AbstractConnectionFactory> lookup = cachingConnectionFactoryLookup.get();
        if (lookup == null) {
            cachingConnectionFactoryLookup.set(new HashMap<>());
            lookup = cachingConnectionFactoryLookup.get();
        }

        return lookup.getOrDefault(binderName, null);
    }

    private void updateCachedConnectionFactory(final String binderName, final AbstractConnectionFactory abstractConnectionFactory) {
        if (cachingConnectionFactoryLookup.get() == null) {
            cachingConnectionFactoryLookup.set(new HashMap<>());
        }
        cachingConnectionFactoryLookup.get().put(binderName, abstractConnectionFactory);
    }
}
