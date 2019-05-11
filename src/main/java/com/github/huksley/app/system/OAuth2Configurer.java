package com.github.huksley.app.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class OAuth2Configurer {
    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private Environment env;

    @Bean
    public OAuth2AuthorizedClientService authorizedClientService() {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository());
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        String clients = env.getProperty("oauth2.clients", "google, github");

        List<ClientRegistration> registrations = Arrays.asList(clients.split("\\,")).stream()
            .map(c -> getRegistration(c.trim()))
            .filter(registration -> registration != null)
            .collect(Collectors.toList());

        return new InMemoryClientRegistrationRepository(registrations);
    }

    private static String CLIENT_CONFIG_PREFIX = "oauth2";

    protected ClientRegistration getRegistration(String client) {
        String clientId = env.getProperty(CLIENT_CONFIG_PREFIX + "." + client + ".client-id");

        if (clientId == null) {
            throw new IllegalArgumentException("Can`t find configuration for " + client);
        }

        String clientSecret = env.getProperty(CLIENT_CONFIG_PREFIX + "." + client + ".client-secret");

        if (client.equals("google")) {
            log.info("Configuring Google, {}", clientId);
            return CommonOAuth2Provider.GOOGLE.getBuilder(client).
                clientId(clientId).
                clientSecret(clientSecret).
                //redirectUriTemplate("{baseUrl}/oauth2/code/{registrationId}").
                build();
        } else
        if (client.equals("github")) {
            log.info("Configuring GitHub, {}", clientId);
            return CommonOAuth2Provider.GITHUB.getBuilder(client).
                clientId(clientId).
                clientSecret(clientSecret).
                //redirectUriTemplate("{baseUrl}/oauth2/code/{registrationId}").
                build();
        } else {
            throw new IllegalArgumentException("Unsupported client: " + client);
        }
    }
}
