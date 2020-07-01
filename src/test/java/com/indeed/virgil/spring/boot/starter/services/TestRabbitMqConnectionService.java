package com.indeed.virgil.spring.boot.starter.services;

import com.indeed.virgil.spring.boot.starter.config.VirgilPropertyConfig;
import com.indeed.virgil.spring.boot.starter.config.VirgilPropertyConfig.BinderProperties;
import com.rabbitmq.client.Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.AbstractConnectionFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked") //Uncheck warnings do not benefit tests
public class TestRabbitMqConnectionService {

    @Mock
    private VirgilPropertyConfig mockVirgilPropertyConfig;

    private RabbitMqConnectionService rabbitMqConnectionService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);

        rabbitMqConnectionService = new RabbitMqConnectionService(mockVirgilPropertyConfig);
    }

    @Nested
    class getAmqpAdmin {

        @Test
        void shouldReturnCachedAmqpAdmin() {
            //Arrange
            final String binderName = "testBinder";

            final AmqpAdmin mockAmqpAdmin = Mockito.mock(AmqpAdmin.class);

            final ThreadLocal<Map<String, AmqpAdmin>> cachedLookup = new ThreadLocal<>();
            cachedLookup.set(new HashMap<>());
            cachedLookup.get().put(binderName, mockAmqpAdmin);

            ReflectionTestUtils.setField(rabbitMqConnectionService, "amqpAdminLookup", cachedLookup);

            //Act
            final AmqpAdmin result = rabbitMqConnectionService.getAmqpAdmin(binderName);

            //Assert
            assertThat(result).isEqualTo(mockAmqpAdmin);
        }

        @Test
        void shouldReturnAmqpAdminWhenCacheIsEmpty() {
            //Arrange
            final String binderName = "testBinder";

            final RabbitProperties rabbitProperties = new RabbitProperties();
            rabbitProperties.setAddresses("example:1111");
            rabbitProperties.setHost(null);
            rabbitProperties.setPort(0);
            rabbitProperties.setUsername("username");
            rabbitProperties.setPassword("password");
            rabbitProperties.setVirtualHost("virtual");

            when(mockVirgilPropertyConfig.getBinderProperties(anyString())).thenReturn(new BinderProperties(binderName, "rabbit", rabbitProperties));

            //Act
            final AmqpAdmin result = rabbitMqConnectionService.getAmqpAdmin(binderName);

            //Assert
            assertThat(result).isNotNull();
        }

        @Test
        void shouldAddNewAmqpAdminToCache() {
            //Arrange
            final String binderName = "testBinder";

            final RabbitProperties rabbitProperties = new RabbitProperties();
            rabbitProperties.setAddresses("example:1111");
            rabbitProperties.setHost(null);
            rabbitProperties.setPort(0);
            rabbitProperties.setUsername("username");
            rabbitProperties.setPassword("password");
            rabbitProperties.setVirtualHost("virtual");

            when(mockVirgilPropertyConfig.getBinderProperties(anyString())).thenReturn(new BinderProperties(binderName, "rabbit", rabbitProperties));

            //Act
            rabbitMqConnectionService.getAmqpAdmin(binderName);

            //Assert
            final ThreadLocal<Map<String, AmqpAdmin>> lookup = (ThreadLocal<Map<String, AmqpAdmin>>) ReflectionTestUtils.getField(rabbitMqConnectionService, "amqpAdminLookup");

            assertThat(lookup.get()).containsKey(binderName);
        }
    }

    @Nested
    class getRabbitTemplate {

        @Test
        void shouldReturnCachedRabbitTemplate() {
            //Arrange
            final String binderName = "testBinder";

            final RabbitTemplate mockRabbitTemplate = Mockito.mock(RabbitTemplate.class);

            final ThreadLocal<Map<String, RabbitTemplate>> cachedLookup = new ThreadLocal<>();
            cachedLookup.set(new HashMap<>());
            cachedLookup.get().put(binderName, mockRabbitTemplate);

            ReflectionTestUtils.setField(rabbitMqConnectionService, "rabbitTemplateLookup", cachedLookup);

            //Act
            final RabbitTemplate result = rabbitMqConnectionService.getRabbitTemplate(binderName);

            //Assert
            assertThat(result).isEqualTo(mockRabbitTemplate);
        }

        @Test
        void shouldReturnRabbitTemplateWhenCacheIsEmpty() {
            //Arrange
            final String binderName = "testBinder";

            final RabbitProperties rabbitProperties = new RabbitProperties();
            rabbitProperties.setAddresses("example:1111");
            rabbitProperties.setHost(null);
            rabbitProperties.setPort(0);
            rabbitProperties.setUsername("username");
            rabbitProperties.setPassword("password");
            rabbitProperties.setVirtualHost("virtual");

            when(mockVirgilPropertyConfig.getBinderProperties(anyString())).thenReturn(new BinderProperties(binderName, "rabbit", rabbitProperties));

            //Act
            final RabbitTemplate result = rabbitMqConnectionService.getRabbitTemplate(binderName);

            //Assert
            assertThat(result).isNotNull();
        }

        @Test
        void shouldAddNewRabbitTemplateToCache() {
            //Arrange
            final String binderName = "testBinder";

            final RabbitProperties rabbitProperties = new RabbitProperties();
            rabbitProperties.setAddresses("example:1111");
            rabbitProperties.setHost(null);
            rabbitProperties.setPort(0);
            rabbitProperties.setUsername("username");
            rabbitProperties.setPassword("password");
            rabbitProperties.setVirtualHost("virtual");

            when(mockVirgilPropertyConfig.getBinderProperties(anyString())).thenReturn(new BinderProperties(binderName, "rabbit", rabbitProperties));

            //Act
            rabbitMqConnectionService.getRabbitTemplate(binderName);

            //Assert
            final ThreadLocal<Map<String, RabbitTemplate>> lookup = (ThreadLocal<Map<String, RabbitTemplate>>) ReflectionTestUtils.getField(rabbitMqConnectionService, "rabbitTemplateLookup");

            assertThat(lookup.get()).containsKey(binderName);
        }
    }

    @Nested
    class getConnectionFactory {

        @Test
        void shouldReturnCachedConnectionFactory() {
            //Arrange
            final String binderName = "testBinderName";

            final CachingConnectionFactory mockCachingConnectionFactory = Mockito.mock(CachingConnectionFactory.class);

            final ThreadLocal<Map<String, CachingConnectionFactory>> cachedLookup = new ThreadLocal<>();
            cachedLookup.set(new HashMap<>());
            cachedLookup.get().put(binderName, mockCachingConnectionFactory);

            ReflectionTestUtils.setField(rabbitMqConnectionService, "cachingConnectionFactoryLookup", cachedLookup);

            //Act
            final AbstractConnectionFactory result = getConnectionFactory(rabbitMqConnectionService, binderName);

            //Assert
            assertThat(result).isEqualTo(mockCachingConnectionFactory);
        }

        @Test
        void shouldReturnNullIfNoBinderProperties() {
            //Arrange
            final String binderName = "testBinderName";

            when(mockVirgilPropertyConfig.getBinderProperties(anyString())).thenReturn(null);

            //Act
            final AbstractConnectionFactory result = getConnectionFactory(rabbitMqConnectionService, binderName);

            //Assert
            assertThat(result).isNull();
        }

        @Test
        void shouldSetAddressesOnConnectionFactory() {
            //Arrange
            final String binderName = "testBinderName";
            final String addressesString = "example:1111";

            final RabbitProperties rabbitProperties = new RabbitProperties();
            rabbitProperties.setAddresses(addressesString);
            rabbitProperties.setUsername("username");
            rabbitProperties.setPassword("password");
            rabbitProperties.setVirtualHost("virtual");

            when(mockVirgilPropertyConfig.getBinderProperties(anyString())).thenReturn(new BinderProperties(binderName, "rabbit", rabbitProperties));

            final List<Address> expectedAddresses = Arrays.asList(Address.parseAddresses(addressesString));

            //Act
            final AbstractConnectionFactory result = getConnectionFactory(rabbitMqConnectionService, binderName);

            //Assert
            final List<Address> actualAddresses = (List<Address>) ReflectionTestUtils.getField(result, "addresses");

            assertThat(actualAddresses).containsAll(expectedAddresses);
        }

        @Test
        void shouldSetHostAndPortOnConnectionFactory() {
            //Arrange
            final String binderName = "testBinderName";

            final RabbitProperties rabbitProperties = new RabbitProperties();
            rabbitProperties.setHost("example");
            rabbitProperties.setPort(1111);
            rabbitProperties.setUsername("username");
            rabbitProperties.setPassword("password");
            rabbitProperties.setVirtualHost("virtual");


            when(mockVirgilPropertyConfig.getBinderProperties(anyString())).thenReturn(new BinderProperties(binderName, "rabbit", rabbitProperties));

            //Act
            final AbstractConnectionFactory result = getConnectionFactory(rabbitMqConnectionService, binderName);

            //Assert
            final List<Address> actualAddresses = (List<Address>) ReflectionTestUtils.getField(result, "addresses");

            assertThat(actualAddresses.get(0).getHost()).isEqualTo("example");
            assertThat(actualAddresses.get(0).getPort()).isEqualTo(1111);
        }
    }


    private static AbstractConnectionFactory getConnectionFactory(final RabbitMqConnectionService instance, final String binderName) {
        try {
            final Method method = RabbitMqConnectionService.class.getDeclaredMethod("getConnectionFactory", String.class);

            method.setAccessible(true);

            return (AbstractConnectionFactory) method.invoke(instance, binderName);

        } catch (final Exception ex) {
            fail("getConnectionFactory failed. [Message: %s]", ex.getMessage());
        }

        return null;
    }
}
