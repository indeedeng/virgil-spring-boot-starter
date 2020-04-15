package com.indeed.virgil.spring.boot.starter.config;

import com.indeed.virgil.spring.boot.starter.config.VirgilPropertyConfig.BinderProperties;
import com.indeed.virgil.spring.boot.starter.config.VirgilPropertyConfig.QueueProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Map;

@SpringJUnitConfig
public class TestVirgilPropertyConfig {


    @Nested
    @EnableConfigurationProperties(VirgilPropertyConfig.class)
    @TestPropertySource(locations = "classpath:single-queue-config.properties")
    public class SingleQueueConfiguration {

        @Autowired
        VirgilPropertyConfig virgilPropertyConfig;

        @Test
        void shouldLoadSingleQueue() {
            //Arrange

            //Act
            final Map<String, QueueProperties> queueMap = virgilPropertyConfig.getQueues();

            //Assert
            Assertions.assertEquals(1, queueMap.size());
        }

        @Test
        void shouldLoadQueueWithReadName() {
            //Arrange

            //Act
            final Map<String, QueueProperties> queueMap = virgilPropertyConfig.getQueues();

            //Assert
            final QueueProperties queueEntry = queueMap.entrySet().iterator().next().getValue();
            Assertions.assertEquals("readQueue", queueEntry.getReadName());
        }

        @Test
        void shouldLoadQueueWithReadBinderName() {
            //Arrange

            //Act
            final Map<String, QueueProperties> queueMap = virgilPropertyConfig.getQueues();

            //Assert
            final QueueProperties queueEntry = queueMap.entrySet().iterator().next().getValue();
            Assertions.assertEquals("rabbit123", queueEntry.getReadBinderName());
        }

        @Test
        void shouldLoadQueueWithRepublishName() {
            //Arrange

            //Act
            final Map<String, QueueProperties> queueMap = virgilPropertyConfig.getQueues();

            //Assert
            final QueueProperties queueEntry = queueMap.entrySet().iterator().next().getValue();
            Assertions.assertEquals("republishQueue", queueEntry.getRepublishName());
        }

        @Test
        void shouldLoadQueueWithRepublishBinderName() {
            //Arrange

            //Act
            final Map<String, QueueProperties> queueMap = virgilPropertyConfig.getQueues();

            //Assert
            final QueueProperties queueEntry = queueMap.entrySet().iterator().next().getValue();
            Assertions.assertEquals("rabbit123", queueEntry.getRepublishBinderName());
        }

        @Test
        void shouldLoadSingleBinder() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            Assertions.assertEquals(1, binderMap.size());
        }

        @Test
        void shouldLoadBinderWithName() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            Assertions.assertEquals("virgil-test-exchange", binderEntry.getName());
        }

        @Test
        void shouldLoadBinderWithTypeRabbit() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            Assertions.assertEquals("rabbit", binderEntry.getType());
        }

        @Test
        void shouldLoadBinderWithRabbitSettingsAddresses() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            Assertions.assertEquals("localhost:11111", binderEntry.getRabbitSettings().getAddresses());
        }

        @Test
        void shouldLoadBinderWithRabbitSettingsHost() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            Assertions.assertEquals("localhost", binderEntry.getRabbitSettings().getHost());
        }

        @Test
        void shouldLoadBinderWithRabbitSettingsPort() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            Assertions.assertEquals(5672, binderEntry.getRabbitSettings().getPort());
        }

        @Test
        void shouldLoadBinderWithRabbitSettingsUsername() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            Assertions.assertEquals("guest", binderEntry.getRabbitSettings().getUsername());
        }

        @Test
        void shouldLoadBinderWithRabbitSettingsPassword() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            Assertions.assertEquals("guestPass", binderEntry.getRabbitSettings().getPassword());
        }

        @Test
        void shouldLoadBinderWithRabbitSettingsVirtualHost() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            Assertions.assertEquals("/", binderEntry.getRabbitSettings().getVirtualHost());
        }

    }

}
