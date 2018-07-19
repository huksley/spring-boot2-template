package com.github.huksley.app.system;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Authentication via db
 */
@Component
@ConditionalOnProperty(name = "auth.type", havingValue = "db", matchIfMissing = false)
public class SecurityAuthenticatorDatabase implements SecurityAuthenticator {
	Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
	Environment env;
	
	@Autowired
	JdbcTemplate jdbc;
	
	@Value("${auth.db.query:null}")
	String userQuery = null;
	
	@Value("${auth.db.column.password:'password'}")
	String passwordColumn = "password";
	
	@Value("${auth.db.column.locked:null}")
	String lockedColumn = null;
	
	@Autowired(required = false)
	BCryptPasswordEncoder encoder;
	
    @Override
    public Authentication authenticate(SecurityLoginProvider logins, Authentication unauth) throws AuthenticationException {
        String name = unauth.getName();
        String password = unauth.getCredentials().toString();
        log.info("Logging in {}", name);
        Authentication auth = null;
        
        BCryptPasswordEncoder enc = encoder;
        if (enc == null) {
            enc = new BCryptPasswordEncoder(15);
        }
        
        Map<String, Object> result = jdbc.query(userQuery, new Object[] { name }, new ResultSetExtractor<Map<String, Object>>() {
            @Override
            public Map<String, Object> extractData(ResultSet rs) throws SQLException, DataAccessException {
                HashMap<String,Object> m = new HashMap<>();
                m.put("password", rs.getString(passwordColumn));
                m.put("locked", "1".equals(rs.getString(lockedColumn)) || Boolean.parseBoolean(rs.getString(lockedColumn)));
                return m;
            }
        });
        
        if (result != null) {
        	ArrayList<GrantedAuthority> roles = new ArrayList<>();
            // FIXME: todo   
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
