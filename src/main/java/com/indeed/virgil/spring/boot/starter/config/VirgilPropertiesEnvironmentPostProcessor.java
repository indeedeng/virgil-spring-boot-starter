package com.indeed.virgil.spring.boot.starter.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.indeed.virgil.spring.boot.starter.util.EndpointConstants.DROP_ALL_MESSAGES_ENDPOINT_ID;
import static com.indeed.virgil.spring.boot.starter.util.EndpointConstants.DROP_MESSAGE_ENDPOINT_ID;
import static com.indeed.virgil.spring.boot.starter.util.EndpointConstants.ENDPOINT_DEFAULT_PATH_MAPPING;
import static com.indeed.virgil.spring.boot.starter.util.EndpointConstants.GET_DLQ_MESSAGES_ENDPOINT_ID;
import static com.indeed.virgil.spring.boot.starter.util.EndpointConstants.GET_QUEUES_ENDPOINT_ID;
import static com.indeed.virgil.spring.boot.starter.util.EndpointConstants.GET_QUEUE_SIZE_ENDPOINT_ID;
import static com.indeed.virgil.spring.boot.starter.util.EndpointConstants.PUBLISH_MESSAGE_ENDPOINT_ID;
import static com.indeed.virgil.spring.boot.starter.util.EndpointConstants.VIRGIL_PATH_PREFIX;

/**
 * Ideally, we can inject all the properties required for us to operate our Virgil Spring Boot Admin plugin without having users manually
 * enter all the stuff we did here to make it work.
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
class VirgilPropertiesEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE_NAME = "virgil";
    private static final String ENDPOINTS_PATH_MAPPING_PROPERTY = "management.endpoints.web.path-mapping.%s";
    private static final String ENDPOINTS_INCLUDE_PROPERTY = "management.endpoints.web.exposure.include";
    private static final String PROBED_ENDPOINTS_PROPERTY = "spring.boot.admin.probed-endpoints";
    private static final String EXTENSION_RESOURCE_LOCATIONS_PROPERTY = "spring.boot.admin.ui.extension-resource-locations";

    private static final String STRING_JOINER = ",";

    // Endpoint id, path mapping, cache TTL
    private static final String[][] DEFAULT_ENDPOINTS = {
        {GET_QUEUE_SIZE_ENDPOINT_ID, ENDPOINT_DEFAULT_PATH_MAPPING + GET_QUEUE_SIZE_ENDPOINT_ID},
        {GET_DLQ_MESSAGES_ENDPOINT_ID, ENDPOINT_DEFAULT_PATH_MAPPING + GET_DLQ_MESSAGES_ENDPOINT_ID},
        {PUBLISH_MESSAGE_ENDPOINT_ID, ENDPOINT_DEFAULT_PATH_MAPPING + PUBLISH_MESSAGE_ENDPOINT_ID},
        {DROP_MESSAGE_ENDPOINT_ID, ENDPOINT_DEFAULT_PATH_MAPPING + DROP_MESSAGE_ENDPOINT_ID},
        {DROP_ALL_MESSAGES_ENDPOINT_ID, ENDPOINT_DEFAULT_PATH_MAPPING + DROP_ALL_MESSAGES_ENDPOINT_ID},
        {GET_QUEUES_ENDPOINT_ID, ENDPOINT_DEFAULT_PATH_MAPPING + GET_QUEUES_ENDPOINT_ID}
    };

    private static final String VIRGIL_EXTENSION_RESOURCE_LOCATION = "classpath:META-INF/extensions/custom/";

    @Override
    public void postProcessEnvironment(final ConfigurableEnvironment environment, final SpringApplication application) {

        // Configure in the default endpoint properties (path and ttl)
        final Set<String> exposedEndpoints = new HashSet<>();
        final Set<String> probedEndpoints = new HashSet<>();
        final Map<String, Object> properties = new HashMap<>();


        for (final String[] endpointConfig : DEFAULT_ENDPOINTS) {
            final String endpointId = endpointConfig[0];
            final String endpointPath = endpointConfig[1];

            exposedEndpoints.add(endpointId);
            probedEndpoints.add(String.format("%s:%s%s", endpointId, VIRGIL_PATH_PREFIX, endpointId));

            final String endpointPathMappingProperty = String.format(ENDPOINTS_PATH_MAPPING_PROPERTY, endpointId);

            final Optional<String> mappingOverride = getProperty(endpointPathMappingProperty, environment);

            //Users can manually override an existing mapping, and we shouldn't change it
            if (!mappingOverride.isPresent()) {
                properties.put(endpointPathMappingProperty, endpointPath);
            }
        }

        // Get the user-specified endpoints if any configured and add to the list of default endpoint ids
        final String endpointIds = String.join(STRING_JOINER, exposedEndpoints)
            + getProperty(ENDPOINTS_INCLUDE_PROPERTY, environment).map(s -> STRING_JOINER + s).orElse("");

        properties.put(ENDPOINTS_INCLUDE_PROPERTY, endpointIds);

        // Get the user-specified probed endpoints if any configured and add to the list of default endpoint ids
        final String consolidatedProbedEndpoints = String.join(STRING_JOINER, probedEndpoints)
            + getProperty(PROBED_ENDPOINTS_PROPERTY, environment).map(s -> STRING_JOINER + s).orElse("");

        properties.put(PROBED_ENDPOINTS_PROPERTY, consolidatedProbedEndpoints);

        //Append resource location for this extension to potential existing property
        final String updatedExtensionResourceLocations = VIRGIL_EXTENSION_RESOURCE_LOCATION
            + getProperty(EXTENSION_RESOURCE_LOCATIONS_PROPERTY, environment).map(s -> STRING_JOINER + s).orElse("");

        properties.put(EXTENSION_RESOURCE_LOCATIONS_PROPERTY, updatedExtensionResourceLocations);

        // Finally override the properties
        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
    }

    private Optional<String> getProperty(final String propertyName, final ConfigurableEnvironment environment) {
        return environment.getPropertySources().stream()
            .filter(propertySource -> propertySource.containsProperty(propertyName))
            .findFirst()
            .map(propertySource -> propertySource.getProperty(propertyName))
            .filter(String.class::isInstance)
            .map(String.class::cast);
    }
}
