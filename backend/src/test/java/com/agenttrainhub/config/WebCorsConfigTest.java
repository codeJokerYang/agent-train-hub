package com.agenttrainhub.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WebCorsConfigTest {

    @Test
    void allowsOnlyConfiguredOrigins() {
        CorsProperties properties = new CorsProperties();
        properties.setAllowedOrigins(List.of("http://localhost:5173", "https://train.example.edu"));
        UrlBasedCorsConfigurationSource source = (UrlBasedCorsConfigurationSource)
                new WebCorsConfig(properties).corsConfigurationSource();
        CorsConfiguration configuration = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/jobs"));

        assertEquals("https://train.example.edu", configuration.checkOrigin("https://train.example.edu"));
        assertNull(configuration.checkOrigin("https://evil.example"));
    }

    @Test
    void rejectsWildcardAndMalformedOriginsAtStartup() {
        CorsProperties wildcard = new CorsProperties();
        wildcard.setAllowedOrigins(List.of("*"));
        assertThrows(IllegalStateException.class, () -> new WebCorsConfig(wildcard).corsConfigurationSource());

        CorsProperties withPath = new CorsProperties();
        withPath.setAllowedOrigins(List.of("https://train.example.edu/path"));
        assertThrows(IllegalStateException.class, () -> new WebCorsConfig(withPath).corsConfigurationSource());
    }
}
