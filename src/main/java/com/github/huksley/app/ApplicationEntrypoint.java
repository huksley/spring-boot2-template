package com.github.huksley.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entrypoint to starting configuration. This file should be in root package.
 */
@SpringBootApplication
public class ApplicationEntrypoint {

	public static void main(String[] args) {
		SpringApplication.run(ApplicationEntrypoint.class, args);
	}
}
