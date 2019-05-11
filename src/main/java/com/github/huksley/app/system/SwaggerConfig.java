package com.github.huksley.app.system;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import org.apache.catalina.ssi.ByteArrayServletOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.ApiKeyVehicle;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Configures and properly proxies Swagger UI and Swagger JSON.
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Autowired
    Environment env;
    
	@SuppressWarnings("deprecation")
    @Bean
	public SecurityConfiguration security() {
		return new SecurityConfiguration(null, null, null, null, null, ApiKeyVehicle.HEADER, "X-Auth-Token", ",");
	}

	private ApiKey apiKey() {
		return new ApiKey(SecurityConfigurer.HEADER_AUTH, SecurityConfigurer.HEADER_AUTH, "header");
	}

	private SecurityContext securityContext() {
		return SecurityContext.builder().securityReferences(defaultAuth()).forPaths(PathSelectors.regex("/*")).build();
	}

	List<SecurityReference> defaultAuth() {
		AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
		AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
		authorizationScopes[0] = authorizationScope;
		return Lists.newArrayList(new SecurityReference(SecurityConfigurer.HEADER_AUTH, authorizationScopes));
	}
	
    @Bean
    public Docket api() { 
        return new Docket(DocumentationType.SWAGGER_2)  
          .select().paths(
              Predicates.and(
                  // Exclude error
                  Predicates.not((s) -> s.equals("/error")),
                  // Exclude management APIs
                  Predicates.not((s) -> s.equals("/management")),
                  Predicates.not((s) -> s.equals("/management.json")),
                  Predicates.not((s) -> s.startsWith("/management/"))
              )
          )
          .build()
          .securitySchemes(Lists.newArrayList(apiKey()))
	      .securityContexts(Lists.newArrayList(securityContext()))
          .apiInfo(apiInfo())
          // Always produce localhost, it will be removed by filter
          .host("localhost");
    }

    public final static String SPRINGFOX_ISSUE_1835_PREFIX = "<Json>";
    public final static String SPRINGFOX_ISSUE_1835_SUFFIX = "</Json>";

    public static class SwaggerFixFilter implements Filter {
        private final Environment env;

        public SwaggerFixFilter(Environment env) {
            this.env = env;
        }

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse res = (HttpServletResponse) response;

            String json = null;
            try (ByteArrayServletOutputStream s = new ByteArrayServletOutputStream()) {
                HttpServletResponseWrapper w = new HttpServletResponseWrapper(res) {
                    @Override
                    public void setContentType(String type) {
                    }

                    @Override
                    public void setHeader(String name, String value) {
                    }

                    @Override
                    public void addHeader(String name, String value) {
                    }

                    @Override
                    public void flushBuffer() throws IOException {
                    }

                    @Override
                    public PrintWriter getWriter() throws IOException {
                        throw new UnsupportedOperationException("getWriter, use getOutputStream");
                    }

                    @Override
                    public ServletOutputStream getOutputStream() throws IOException {
                        return s;
                    }
                };
                chain.doFilter(request, w);
                json = new String(s.toByteArray(), "UTF-8");
            }

            // Change Swagger JSON - remove host
            json = json.replace(",\"host\":\"localhost\",", ",");

            String behindProxy = req.getHeader("X-Real-IP");
            if (behindProxy == null) {
                behindProxy = req.getHeader("X-Forwarded-For");
            }

            String path = env.getProperty("server.contextPath", "/");
            String rpath = System.getenv("REAL_CONTEXT_PATH");
            if (rpath != null) {
                path = rpath;
            }
            if (behindProxy != null && path != null) {
                json = json.replace(",\"basePath\":\"/\",", ",\"basePath\":\"" + path + "\",");
            }

            res.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);

            if (json.startsWith(SPRINGFOX_ISSUE_1835_PREFIX)) {
                json = json.substring(SPRINGFOX_ISSUE_1835_PREFIX.length());
            }

            if (json.endsWith(SPRINGFOX_ISSUE_1835_SUFFIX)) {
                json = json.substring(0, json.length() - SPRINGFOX_ISSUE_1835_SUFFIX.length());
            }
            res.getOutputStream().write(json.getBytes("UTF-8"));
        }

        @Override
        public void destroy() {
        }
    }

    @Bean
    public FilterRegistrationBean createApiFilter() {
        FilterRegistrationBean b = new FilterRegistrationBean(new SwaggerFixFilter(env));
		b.setName("SwaggerJSONFilter");
        b.setUrlPatterns(Arrays.asList(new String[] { "/v2/api-docs", "/api/openapi.json" }));
        return b;
    }
    
    private ApiInfo apiInfo() {
        ApiInfo apiInfo = new ApiInfo(
            env.getProperty("swagger.title", "API"),
            env.getProperty("swagger.description", "API"),
            env.getProperty("swagger.version", "0.1"),
            null,
            new springfox.documentation.service.Contact(
                env.getProperty("swagger.contact", "Example company & friends"),
                env.getProperty("swagger.contact.url", "https://example.com"), 
                env.getProperty("swagger.contact.email", "contact@example.com")
            ),
            null,
            null,
                Collections.emptyList() // should not be null
        );
        return apiInfo;
    }
}