package com.github.huksley.app.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * Spring Security implementation which delegates to {@link SecurityAuthenticator} implementation.
 */
@Component
public class SecurityLoginProvider implements AuthenticationProvider {
	Logger log = LoggerFactory.getLogger(getClass());
	
    @Autowired
    Environment env;
    
    @Autowired
    SecurityAuthenticator auth;
	
	@Override
	public Authentication authenticate(Authentication unauth) throws AuthenticationException {
	    return auth.authenticate(this, unauth);
	}
	
	@Override
	public boolean supports(Class<?> authClass) {
	    return auth.supports(authClass);
	}
}