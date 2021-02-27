package com.indeed.virgil.example.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static final String TOPIC_EXCHANGE_NAME = "virgil-exchange";
    public static final String ROUTING_KEY = "test.#";
    public static final String DLQ_ROUTING_KEY = "dlq.#";

    static final String queueName = "virgil-queue";

    static final String dlqQueueName = "virgil-dlq";

    @Bean
    Queue queue() {
        return new Queue(queueName, false);
    }

    @Bean("dlq")
    Queue dlqQueue() {
        return new Queue(dlqQueueName, false);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(TOPIC_EXCHANGE_NAME);
    }

    @Bean
    Binding binding(final Queue queue, final TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    Binding dlqBinding(@Qualifier("dlq") final Queue dlqQueue, final TopicExchange exchange) {
        return BindingBuilder.bind(dlqQueue).to(exchange).with(DLQ_ROUTING_KEY);
    }
}
