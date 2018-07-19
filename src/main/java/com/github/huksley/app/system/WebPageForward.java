package com.github.huksley.app.system;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Appropriate forwarding and processing inside system.
 */
@Controller
@RequestMapping("/")
@Api(description = "Browser forwarding and utility endpoints")
public class WebPageForward {
    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    Environment env;
    
    @Autowired
    ApplicationContext context;
      
    @ApiOperation("Redirects / elsewhere")
    @GetMapping("/")
    public void root(HttpServletResponse response) throws IOException {
        response.sendRedirect(env.getProperty("forward.root", "/index.html"));
    }
}