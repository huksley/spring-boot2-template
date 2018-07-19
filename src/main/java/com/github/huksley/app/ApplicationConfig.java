package com.github.huksley.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.context.support.ServletRequestHandledEvent;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Generic application configuration and beans. This file should be in root package.
 */
@Configuration
@EnableAsync
@EnableScheduling
@ComponentScan
@EnableJpaRepositories
@EntityScan
public class ApplicationConfig {
	Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	protected ApplicationEventPublisher publisher;

	@EventListener
	protected void onEvent(Object ev) {
	    if (ev instanceof ServletRequestHandledEvent) {
	        // Don`t care
	    } else {
	        log.info("Got event {}", ev);
	    }
	}
	
	@EventListener
    protected void onEvent(ContextClosedEvent ev) {
        log.info("Context stopped {}", ev);
    }
	
	@Bean
	@Primary
    public ObjectMapper objectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        om.enable(MapperFeature.DEFAULT_VIEW_INCLUSION);
        om.enable(SerializationFeature.INDENT_OUTPUT);
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return om;
    }
	
	@Bean
	public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
	    MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
	    ObjectMapper objectMapper = new ObjectMapper();
	    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	    jsonConverter.setObjectMapper(objectMapper);
	    return jsonConverter;
	}
}