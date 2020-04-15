package com.indeed.virgil.spring.boot.starter.config;

import com.indeed.virgil.spring.boot.starter.services.DefaultMessageConverter;
import com.indeed.virgil.spring.boot.starter.services.IMessageConverter;
import com.indeed.virgil.spring.boot.starter.services.MessageConverterService;
import com.indeed.virgil.spring.boot.starter.services.MessageOperator;
import com.indeed.virgil.spring.boot.starter.services.RabbitMqConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(VirgilPropertyConfig.class)
class VirgilConfig {

    @Autowired
    private VirgilPropertyConfig virgilPropertyConfig;

    @Bean
    RabbitMqConnectionService rabbitMqConnectionService() {
        return new RabbitMqConnectionService(virgilPropertyConfig);
    }

    @Bean
    public MessageOperator messageOperator(
        final RabbitMqConnectionService rabbitMqConnectionService,
        final MessageConverterService messageConverterService
    ) {
        return new MessageOperator(virgilPropertyConfig, rabbitMqConnectionService, messageConverterService);
    }

    @Bean
    public MessageConverterService messageConverterService(
        final IMessageConverter messageConverter
    ) {
        return new MessageConverterService(messageConverter);
    }

    @Bean
    @ConditionalOnMissingBean
    public IMessageConverter messageConverter() {
        return new DefaultMessageConverter();
    }
}
