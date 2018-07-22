package com.github.huksley.app.system;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClient;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Test auth (single user with USER and ADMIN role)
 */
@Component
@ConditionalOnProperty(name = "auth.type", havingValue = "cognito", matchIfMissing = false)
public class SecurityAuthenticatorCognito implements SecurityAuthenticator {
	Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
	Environment env;

	@Value("${auth.cognito.client.id}")
    String cognitoClientId;

	@Value("${auth.cognito.pool.id}")
    String cognitoPoolId;

    @Value("${auth.cognito.region}")
    String cognitoRegion;

    AWSCognitoIdentityProvider cognito;

    protected void initCognito() {
        if (cognito == null) {
            log.info("Building cognito, region {}", cognitoRegion);
            AWSCognitoIdentityProviderClientBuilder b = AWSCognitoIdentityProviderClientBuilder.standard();
            b.setRegion(cognitoRegion);
            cognito = b.build();
        }
    }
	
    @Override
    public Authentication authenticate(SecurityLoginProvider logins, Authentication unauth) throws AuthenticationException {
        initCognito();

        SecurityConfigurer.ExtendedWebAuthenticationDetails det = (SecurityConfigurer.ExtendedWebAuthenticationDetails) unauth.getDetails();

        String username = unauth.getName();
        String password = unauth.getCredentials().toString();
        log.info("Logging in {} clientId {} poolId {}", username, cognitoClientId, cognitoPoolId);
        Authentication auth = null;

        Map<String,String> authParams = new HashMap<>();
        authParams.put("USERNAME", username);
        authParams.put("PASSWORD", password);

        AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest()
                .withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .withAuthParameters(authParams)
                .withClientId(cognitoClientId)
                .withUserPoolId(cognitoPoolId);

        try {
            AdminInitiateAuthResult authResponse = cognito.adminInitiateAuth(authRequest);
            if (StringUtils.isEmpty(authResponse.getChallengeName())) {
                log.info("Logged in as {} token {}", username, authResponse.getAuthenticationResult().getAccessToken());
                AdminListGroupsForUserResult groupsResponse = cognito.adminListGroupsForUser(new AdminListGroupsForUserRequest().withUserPoolId(cognitoPoolId).withUsername(username));
                List<SimpleGrantedAuthority> groups = groupsResponse.getGroups().stream().map((g) -> new SimpleGrantedAuthority("ROLE_" + g.getGroupName())).collect(Collectors.toList());
                auth = new UsernamePasswordAuthenticationToken(username,
                        authResponse.getAuthenticationResult().getAccessToken(),
                        groups);
            } else
            if (ChallengeNameType.NEW_PASSWORD_REQUIRED.name().equals(authResponse.getChallengeName())) {
                String newPassword = det.getRequest().getParameter("newPassword");
                if (newPassword == null) {
                    log.info("Password must be changed before login {}", username);
                    throw new CredentialsExpiredException("INFO: Password must be changed");
                } else {
                    Map<String,String> challengeResponses = new HashMap<>();
                    challengeResponses.put("USERNAME", username);
                    challengeResponses.put("PASSWORD", password);
                    challengeResponses.put("NEW_PASSWORD", newPassword);

                    AdminRespondToAuthChallengeRequest changePassword = new AdminRespondToAuthChallengeRequest()
                            .withChallengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
                            .withChallengeResponses(challengeResponses)
                            .withClientId(cognitoClientId)
                            .withUserPoolId(cognitoPoolId)
                            .withSession(authResponse.getSession());

                    try {
                        AdminRespondToAuthChallengeResult changePasswordResponse = cognito.adminRespondToAuthChallenge(changePassword);
                        if (StringUtils.isEmpty(changePassword.getChallengeName())) {
                            log.info("Logged in as {} token {}", username, changePasswordResponse.getAuthenticationResult().getAccessToken());
                            AdminListGroupsForUserResult groupsResponse = cognito.adminListGroupsForUser(new AdminListGroupsForUserRequest().withUserPoolId(cognitoPoolId).withUsername(username));
                            List<SimpleGrantedAuthority> groups = groupsResponse.getGroups().stream().map((g) -> new SimpleGrantedAuthority("ROLE_" + g.getGroupName())).collect(Collectors.toList());
                            auth = new UsernamePasswordAuthenticationToken(username,
                                    changePasswordResponse.getAuthenticationResult().getAccessToken(), groups);
                        } else {
                            throw new CredentialsExpiredException("Unsupported challenge on change password " + authResponse.getChallengeName() + " for " + username);
                        }
                    } catch (InvalidPasswordException e) {
                        throw new CredentialsExpiredException("Invalid new password");
                    }
                }
            } else {
                throw new BadCredentialsException("Unsupported challenge " + authResponse.getChallengeName() + " for " + username);
            }
        } catch (UserNotFoundException e) {
            throw new BadCredentialsException("ERROR: " + e.getMessage(), e);
        } catch (NotAuthorizedException e) {
            throw new BadCredentialsException("ERROR: " + e.getMessage(), e);
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
