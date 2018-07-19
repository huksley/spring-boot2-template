package com.github.huksley.app.system;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.Principal;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Single entry security endpoint.
 * Provides, auth, css, json for current security state.
 */
@Controller
@Api(description = "Authentication and login, logout forms.")
public class SecurityEndpointController {
    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired(required = false)
    Principal auth;

    @Autowired(required = false)
    AuthenticationProvider provider;

    @Autowired(required = false)
    AuthenticationManager manager;
    
    @Autowired
    Environment env;

    @ApiOperation("Handles auth requests")
    @RequestMapping(path = "/auth/**", method = { RequestMethod.GET, RequestMethod.POST })
    public void auth(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SecurityContext ctx = SecurityContextHolder.getContext();
        Authentication auth = (Authentication) request.getUserPrincipal();
        String authType = env.getProperty("PROCESS_AUTH", "test");

        // Obtain current request token
        String token = request.getHeader(SecurityConfigurer.HEADER_AUTH);
        if (token == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie ck: cookies) {
                    if (ck.getName().equals(SecurityConfigurer.COOKIE_AUTH)) {
                        token = ck.getValue();
                    }
                }
            }
        }

        MDC.def().log(log).trace("Serving auth endpoint {}, auth {}, provider {} manager {}", request.getServletPath(), auth, provider, manager, token);
        response.addHeader("Cache-Control", "no-cache");
        response.addHeader("Vary", "Cookie");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html");

        PrintWriter out = response.getWriter();
        String what = request.getServletPath();
        if (what != null && (what.equals("/auth") || what.equals("/auth/"))) {
            what = null;
        } else
        if (what != null && what.startsWith("/auth/")) {
            what = what.substring("/auth/".length());
        }

        String accept = request.getHeader("Accept");
        boolean acceptsForm = accept != null && (accept.indexOf("text/*") >= 0 || accept.indexOf("text/html") >= 0 || accept.indexOf("*/*") >= 0);
        String xAuth = request.getHeader("X-Auth-Request");
        acceptsForm = acceptsForm && ("true".equals(xAuth) || "1".equals(xAuth));
        if (!acceptsForm && what == null) {
        	if (auth != null) {
        	    response.sendRedirect("/auth/info");
        	} else {
        	    response.sendRedirect("/auth/token");
        	}
        } else
        if (what != null && (what.equals("info") || (!acceptsForm && what.equals("success")))) {
            String s = "";
            boolean var = false;
            boolean amd = Boolean.parseBoolean(request.getParameter("amd"));
            if (amd) {
            	s += "(function (name, func) { if (window.define) { define(name, [], func); } else { window[name] = func(); } })('auth', function () { return ";
            } else
            if (request.getParameter("var") != null) {
            	s += "var " + request.getParameter("var") + " = ";
            	var = true;
            }
            s += "{\n";
            s += "\"type\": \"" + authType + "\",\n";
            
            if (auth != null) {
                s += "\"login\": \"" + auth.getName() + "\",\n";
                s += "\"type\": \"" + auth.getClass().getSimpleName() + "\",\n";
                if (auth instanceof Authentication) {
                    Authentication a = (Authentication) auth;
                    String l = "";
                    for (GrantedAuthority aa: a.getAuthorities()) {
                        if (!l.equals("")) {
                            l += ", ";
                        }
                        l += "\"" + aa.getAuthority() + "\"";
                    }
                    s += "\"roles\": [ " + l + " ],\n";
                } else {
                    s += "\"roles\": [],\n";
                }
            }
            
            // https://stackoverflow.com/questions/13261794/display-error-messages-in-spring-login
            HttpSession session = request.getSession(false);
            if (session != null) {
                Throwable last = (Throwable) session.getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
                if (last != null) {
                    s += "\"error\": \"" + (last.getMessage() != null ? last.getMessage() : last.toString()) + "\",\n";
                } else {
                    s += "\"error\": null,\n";
                }
                
                Throwable cause = last != null && last.getCause() != null ? last.getCause() : last;
                if (cause != null) {
                    s += "\"errorException\": \"" + cause.toString() +  "\",\n";
                }
            }

            s += "\"remoteIp\": \"" + request.getRemoteAddr() + "\",\n";

            if (request.getHeader("X-Real-IP") != null) {
                s += "\"realIp\": \"" + request.getHeader("X-Real-IP") + "\",\n";
            } else {
                s += "\"realIp\": null,\n";
            }

            s += "\"userAgent\": \"" + request.getHeader("User-Agent") + "\",\n";

            if (token != null) {
                s += "\"token\": \"" + token + "\",\n";
            } else {
                s += "\"token\": null,\n";
            }

            s += "\"_timestamp\": " + System.currentTimeMillis();
            s += " \n}";
            if (amd) {
            	s += "});";
            }
            if (var || amd) {
            	response.setContentType("application/javascript");
            } else {
            	response.setContentType("application/json");
            }
            out.write(s);
        } else
        if (acceptsForm && what != null && what.equals("success")) {
            response.sendRedirect(env.getProperty("forward.login.success", "/auth/"));
        } else
        if (what != null && what.equals("css")) {
            String s = null;
            try (InputStream is = getClass().getResourceAsStream("/static/auth/template.css")) {
                s = new String(StreamUtils.copyToByteArray(is), "UTF-8");
            }
            
            s += "\n\n.authtype_" + authType + " {\n"
                    +
                    "    display: block;\n" +
                    "}";
            
            if (auth != null) {
                for (GrantedAuthority aa : auth.getAuthorities()) {
                    s += "\n\n.role_" + aa.getAuthority().replace("ROLE_", "") + " {\n" +
                            "    display: block;\n" +
                            "}";
                }
            }

            response.setContentType("text/css");
            out.write(s);
        } else
        if (acceptsForm && what == null) {
            if (auth != null) {
                String s = null;
            	try (InputStream is = getClass().getResourceAsStream("/static/auth/user.html")) {
                	s = new String(StreamUtils.copyToByteArray(is), "UTF-8");
                }
                s = s.replace("<title>", "<BASE HREF=\"/auth/forms/user.html\"><title>");
                out.write(s);
            } else {
                response.sendRedirect("/auth/login");
            }
        } else
        if (acceptsForm && what.equals("login")) {
            String s = null;
        	try (InputStream is = getClass().getResourceAsStream("/static/auth/login.html")) {
        		s = new String(StreamUtils.copyToByteArray(is), "UTF-8");
        	}
        	s = s.replace("<title>", "<BASE HREF=\"/auth/login.html\"><title>");
            out.write(s);
        } else
        if (acceptsForm && what.equals("token") && auth != null) {
            response.sendRedirect("/auth/");
        } else
        if (!acceptsForm && what.equals("token") && auth != null) {
            response.setContentType("application/javascript");
            response.getWriter().write("{ \"success\": true }");
            response.flushBuffer();
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not authorized");
            response.setContentType("application/javascript");
            response.getWriter().write("{ \"error\": \"Not authorized\" }");
            response.flushBuffer();
        }
    }
}