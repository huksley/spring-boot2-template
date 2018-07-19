package com.github.huksley.app.system;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * Test auth (single user with USER and ADMIN role)
 */
@Component
@ConditionalOnProperty(name = "auth.type", havingValue = "test", matchIfMissing = true)
public class SecurityAuthenticatorTest implements SecurityAuthenticator {
	Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
	Environment env;
	
    @Override
    public Authentication authenticate(SecurityLoginProvider logins, Authentication unauth) throws AuthenticationException {
        String name = unauth.getName();
        String password = unauth.getCredentials().toString();
        log.info("Logging in {}", name);
        Authentication auth = null;
        
        String testUser = env.getProperty("test.user", "test");
        String testPassword = env.getProperty("test.password", "123");
        String testRoles = env.getProperty("test.password", "USER,ADMIN");
        
        if (name.equals(testUser) && password.equals(testPassword)) {
        	ArrayList<GrantedAuthority> roles = new ArrayList<>();
            for (String r: testRoles.split("\\,")) {
                roles.add(new SimpleGrantedAuthority("ROLE_" + r.trim()));
            }
                
            auth = new UsernamePasswordAuthenticationToken(name, password, roles);
        }
        
        if (auth != null && !auth.isAuthenticated()) {
            auth.setAuthenticated(true);
        }

        if (auth == null) {
            throw new BadCredentialsException("Invalid username or password");
        }

        return auth;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
