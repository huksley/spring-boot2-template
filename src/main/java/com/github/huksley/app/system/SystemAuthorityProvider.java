package com.github.huksley.app.system;

import org.springframework.security.core.Authentication;

/**
 * Injection interface for system auth. 
 */
public interface SystemAuthorityProvider {

	Authentication getSystemAuthority(Object invoker);
}
