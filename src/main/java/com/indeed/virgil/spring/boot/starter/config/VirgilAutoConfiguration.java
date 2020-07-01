package com.indeed.virgil.spring.boot.starter.config;

import com.indeed.virgil.spring.boot.starter.endpoints.VirgilEndpointsConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Loads Configuration for Virgil.
 */
@Configuration
@Import({
    VirgilEndpointsConfiguration.class,
    VirgilConfig.class
})
public class VirgilAutoConfiguration {
}
