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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

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
            assertThat(queueMap.size()).isEqualTo(1);
        }

        @Test
        void shouldLoadQueueWithReadName() {
            //Arrange

            //Act
            final Map<String, QueueProperties> queueMap = virgilPropertyConfig.getQueues();

            //Assert
            final QueueProperties queueEntry = queueMap.entrySet().iterator().next().getValue();
            assertThat(queueEntry.getReadName()).isEqualTo("readQueue");
        }

        @Test
        void shouldLoadQueueWithReadBinderName() {
            //Arrange

            //Act
            final Map<String, QueueProperties> queueMap = virgilPropertyConfig.getQueues();

            //Assert
            final QueueProperties queueEntry = queueMap.entrySet().iterator().next().getValue();
            assertThat(queueEntry.getReadBinderName()).isEqualTo("rabbit123");
        }

        @Test
        void shouldLoadQueueWithRepublishName() {
            //Arrange

            //Act
            final Map<String, QueueProperties> queueMap = virgilPropertyConfig.getQueues();

            //Assert
            final QueueProperties queueEntry = queueMap.entrySet().iterator().next().getValue();
            assertThat(queueEntry.getRepublishName()).isEqualTo("republishQueue");
        }

        @Test
        void shouldLoadQueueWithRepublishBinderName() {
            //Arrange

            //Act
            final Map<String, QueueProperties> queueMap = virgilPropertyConfig.getQueues();

            //Assert
            final QueueProperties queueEntry = queueMap.entrySet().iterator().next().getValue();
            assertThat(queueEntry.getRepublishBinderName()).isEqualTo("rabbit123");
        }

        @Test
        void shouldLoadSingleBinder() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            assertThat(binderMap.size()).isEqualTo(1);
        }

        @Test
        void shouldLoadBinderWithName() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            assertThat(binderEntry.getName()).isEqualTo("virgil-test-exchange");
        }

        @Test
        void shouldLoadBinderWithTypeRabbit() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            assertThat(binderEntry.getType()).isEqualTo("rabbit");
        }

        @Test
        void shouldLoadBinderWithRabbitSettingsAddresses() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            assertThat(binderEntry.getRabbitProperties().getAddresses()).isEqualTo("localhost:11111");
        }

        @Test
        void shouldLoadBinderWithRabbitSettingsHost() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            assertThat(binderEntry.getRabbitProperties().getHost()).isEqualTo("localhost");
        }

        @Test
        void shouldLoadBinderWithRabbitSettingsPort() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            assertThat(binderEntry.getRabbitProperties().getPort()).isEqualTo(5672);
        }

        @Test
        void shouldLoadBinderWithRabbitSettingsUsername() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            assertThat(binderEntry.getRabbitProperties().getUsername()).isEqualTo("guest");
        }

        @Test
        void shouldLoadBinderWithRabbitSettingsPassword() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            assertThat(binderEntry.getRabbitProperties().getPassword()).isEqualTo("guestPass");
        }

        @Test
        void shouldLoadBinderWithRabbitSettingsVirtualHost() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            assertThat(binderEntry.getRabbitProperties().getVirtualHost()).isEqualTo("/");
        }

        @Test
        void shouldReturnListOfQueues() {

            //Act
            final List<String> result = virgilPropertyConfig.getQueueNames();

            //Assert
            assertThat(result).containsExactly("primary");
        }
    }

    @Nested
    @EnableConfigurationProperties(VirgilPropertyConfig.class)
    @TestPropertySource(locations = "classpath:single-queue-with-port-config.properties")
    public class SingleQueueWithPortConfiguration {

        @Autowired
        VirgilPropertyConfig virgilPropertyConfig;

        @Test
        void shouldLoadSingleQueue() {
            //Arrange

            //Act
            final Map<String, QueueProperties> queueMap = virgilPropertyConfig.getQueues();

            //Assert
            assertThat(queueMap.size()).isEqualTo(1);
        }

        @Test
        void shouldLoadBinderWithName() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            assertThat(binderEntry.getName()).isEqualTo("virgil-test-exchange");
        }

        @Test
        void shouldLoadBinderWithTypeRabbit() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            assertThat(binderEntry.getType()).isEqualTo("rabbit");
        }

        @Test
        void shouldLoadBinderWithRabbitSettingsAddresses() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            assertThat(binderEntry.getRabbitProperties().determineAddresses()).isEqualTo("my-rabbit-host:5672");
        }

        @Test
        void shouldLoadBinderWithRabbitSettingsHost() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            assertThat(binderEntry.getRabbitProperties().determineHost()).isEqualTo("my-rabbit-host");
        }

        @Test
        void shouldLoadBinderWithRabbitSettingsPort() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            assertThat(binderEntry.getRabbitProperties().determinePort()).isEqualTo(5672);
        }

        @Test
        void shouldLoadBinderWithRabbitSettingsUsername() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            assertThat(binderEntry.getRabbitProperties().determineUsername()).isEqualTo("guest");
        }

        @Test
        void shouldLoadBinderWithRabbitSettingsPassword() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            assertThat(binderEntry.getRabbitProperties().determinePassword()).isEqualTo("guestPass");
        }

        @Test
        void shouldLoadBinderWithRabbitSettingsVirtualHost() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            assertThat(binderEntry.getRabbitProperties().determineVirtualHost()).isEqualTo("my-vhost");
        }

        @Test
        void shouldReturnListOfQueues() {

            //Act
            final List<String> result = virgilPropertyConfig.getQueueNames();

            //Assert
            assertThat(result).containsExactly("primary");
        }
    }

    @Nested
    @EnableConfigurationProperties(VirgilPropertyConfig.class)
    @TestPropertySource(locations = "classpath:single-queue-without-port-config.properties")
    public class SingleQueueWithoutPortConfiguration {

        @Autowired
        VirgilPropertyConfig virgilPropertyConfig;

        @Test
        void shouldLoadSingleQueue() {
            //Arrange

            //Act
            final Map<String, QueueProperties> queueMap = virgilPropertyConfig.getQueues();

            //Assert
            assertThat(queueMap.size()).isEqualTo(1);
        }

        @Test
        void shouldLoadBinderWithName() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            assertThat(binderEntry.getName()).isEqualTo("virgil-test-exchange");
        }

        @Test
        void shouldLoadBinderWithTypeRabbit() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            assertThat(binderEntry.getType()).isEqualTo("rabbit");
        }

        @Test
        void shouldLoadBinderWithRabbitSettingsAddresses() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            assertThat(binderEntry.getRabbitProperties().determineAddresses()).isEqualTo("my-rabbit-host-3:5672");
        }

        @Test
        void shouldLoadBinderWithRabbitSettingsHost() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            assertThat(binderEntry.getRabbitProperties().determineHost()).isEqualTo("my-rabbit-host-3");
        }

        @Test
        void shouldLoadBinderWithRabbitSettingsPort() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            assertThat(binderEntry.getRabbitProperties().determinePort()).isEqualTo(5672);
        }

        @Test
        void shouldLoadBinderWithRabbitSettingsUsername() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            assertThat(binderEntry.getRabbitProperties().determineUsername()).isEqualTo("guest3");
        }

        @Test
        void shouldLoadBinderWithRabbitSettingsPassword() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            assertThat(binderEntry.getRabbitProperties().determinePassword()).isEqualTo("guestPass3");
        }

        @Test
        void shouldLoadBinderWithRabbitSettingsVirtualHost() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            assertThat(binderEntry.getRabbitProperties().determineVirtualHost()).isEqualTo("my-vhost3");
        }

        @Test
        void shouldReturnListOfQueues() {

            //Act
            final List<String> result = virgilPropertyConfig.getQueueNames();

            //Assert
            assertThat(result).containsExactly("primary");
        }
    }

    @Nested
    @EnableConfigurationProperties(VirgilPropertyConfig.class)
    @TestPropertySource(locations = "classpath:multi-queue-config.properties")
    public class MultiQueueConfiguration {

        @Autowired
        VirgilPropertyConfig virgilPropertyConfig;

        @Test
        void shouldAllQueue() {
            //Arrange

            //Act
            final Map<String, QueueProperties> queueMap = virgilPropertyConfig.getQueues();

            //Assert
            assertThat(queueMap.size()).isEqualTo(2);
        }

        @Test
        void shouldLoadBinderWithName() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            assertThat(binderEntry.getName()).isEqualTo("virgil-test-exchange");
        }

        @Test
        void shouldLoadBinderWithTypeRabbit() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            assertThat(binderEntry.getType()).isEqualTo("rabbit");
        }

        @Test
        void shouldLoadBinderWithRabbitSettingsAddresses() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            assertThat(binderEntry.getRabbitProperties().determineAddresses()).isEqualTo("localhost:11111");
        }

        @Test
        void shouldLoadBinderWithRabbitSettingsHost() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            assertThat(binderEntry.getRabbitProperties().determineHost()).isEqualTo("localhost");
        }

        @Test
        void shouldLoadBinderWithRabbitSettingsPort() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            assertThat(binderEntry.getRabbitProperties().determinePort()).isEqualTo(11111);
        }

        @Test
        void shouldLoadBinderWithRabbitSettingsUsername() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            assertThat(binderEntry.getRabbitProperties().determineUsername()).isEqualTo("guest");
        }

        @Test
        void shouldLoadBinderWithRabbitSettingsPassword() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            assertThat(binderEntry.getRabbitProperties().determinePassword()).isEqualTo("guestPass");
        }

        @Test
        void shouldLoadBinderWithRabbitSettingsVirtualHost() {
            //Arrange

            //Act
            final Map<String, BinderProperties> binderMap = virgilPropertyConfig.getBinders();

            //Assert
            final BinderProperties binderEntry = binderMap.entrySet().iterator().next().getValue();
            assertThat(binderEntry.getRabbitProperties().determineVirtualHost()).isEqualTo("/");
        }

        @Test
        void shouldReturnListOfQueues() {

            //Act
            final List<String> result = virgilPropertyConfig.getQueueNames();

            //Assert
            assertThat(result).contains("primary", "secondary");
        }
    }
}
