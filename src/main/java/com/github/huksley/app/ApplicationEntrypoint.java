package com.github.huksley.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.redis.RedisHealthIndicatorAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;

/**
 * Entrypoint to starting configuration. This file should be in root package.
 */
@SpringBootApplication(
	exclude = { RedisAutoConfiguration.class,
		RedisHealthIndicatorAutoConfiguration.class,
		RedisRepositoriesAutoConfiguration.class
	}
)
public class ApplicationEntrypoint {

	public static void main(String[] args) {
		SpringApplication.run(ApplicationEntrypoint.class, args);
	}
}
