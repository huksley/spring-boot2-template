package com.github.huksley.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Run this class from IDE instead of @link {@link ApplicationEntrypoint} so that test class path
 * will be availale.
 *
 * Specify VM arguments for using test profile:
 * <code>
 *     -Dspring.profiles.active=test
 * </code>
 **/
public class TestApplicationEntrypoint {

	public static void main(String[] args) {
		SpringApplication.run(ApplicationEntrypoint.class, args);
	}
}
