package com.github.huksley.app.system;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * Authentication interface
 */
public interface SecurityAuthenticator {
    
    Authentication authenticate(SecurityLoginProvider logins, Authentication unauth) throws AuthenticationException;
    
    boolean supports(Class<?> authentication);
}
