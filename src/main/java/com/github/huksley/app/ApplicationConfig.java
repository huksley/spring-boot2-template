package com.github.huksley.app;

import com.github.huksley.app.system.RedisAvailable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.redis.RedisHealthIndicator;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.context.support.ServletRequestHandledEvent;

import java.util.TimeZone;

/**
 * Generic application configuration and beans. This file should be in root package.
 */
@Configuration
@EnableAsync
@EnableScheduling
@ComponentScan
@EnableJpaRepositories
@EntityScan
@EnableCaching
public class ApplicationConfig {
	Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	protected ApplicationEventPublisher publisher;

	@Autowired
	protected Environment env;

    /**
     * Force using UTC timezone throughout the app.
     * Good for consistent datetime and timezone representation throughout the app.
     */
    static {
        if (!"false".equals(System.getenv("FORCE_UTC"))) {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        }
    }

    /**
     * Listener for every event in Spring Framework,
     * during development only to understand interesting events to track aftewards
     */
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
	@Conditional(RedisAvailable.class)
	public RedisConnectionFactory redisConnectionFactory() {
		RedisStandaloneConfiguration rc = new RedisStandaloneConfiguration();
		rc.setHostName(env.resolvePlaceholders("${redis.host:localhost}"));
		rc.setPort(Integer.parseInt(env.resolvePlaceholders("${redis.port:6379}")));
		JedisConnectionFactory cf = new JedisConnectionFactory(rc);
		log.info("Establishing connection to {}:{}", cf.getHostName(), cf.getPort());
		return cf;
	}

	@Bean
	@Conditional(RedisAvailable.class)
	public RedisHealthIndicator getRedisHealth(RedisConnectionFactory cf) {
		RedisHealthIndicator h = new RedisHealthIndicator(cf);
		return h;
	}

	/**
	 * Need to configure {@link org.springframework.data.redis.serializer.JdkSerializationRedisSerializer}
	 * with classloader because of devtools recompilation.
	 */
	@Bean
	@Conditional(RedisAvailable.class)
	public RedisCacheConfiguration defaultCacheConfig() {
		return RedisCacheConfiguration.defaultCacheConfig(getClass().getClassLoader());
	}

	/**
	 * Used for @Autowired CacheManager manager.
	 */
	@Bean
	@Conditional(RedisAvailable.class)
	public CacheManager cacheManager(RedisConnectionFactory cf) {
		log.info("Creating Redis cache manager");
		RedisCacheManager cacheManager = RedisCacheManager.builder(cf).cacheDefaults(defaultCacheConfig()).build();
		return cacheManager;
	}
}