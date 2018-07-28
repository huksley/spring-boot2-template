package com.github.huksley.app;

import com.jayway.jsonpath.matchers.JsonPathMatchers;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "security.auth.type = test",
    "security.insecure = false",
    "security.auth.test.user = test",
    "security.auth.test.password = 123",
    "security.auth.test.roles = USER,ADMIN",
    "forward.login.success=/auth/info",
    "forward.logout.finish=/auth/info"
})
public class TestSystem {
    private final Logger log = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    WebApplicationContext app;
    MockMvc mock;

    /**
     * Enable MockMvc with session support
     * https://stackoverflow.com/questions/26142631/why-does-spring-mockmvc-result-not-contain-a-cookie#26281932
     * https://stackoverflow.com/questions/14308341/how-to-login-a-user-with-spring-3-2-new-mvc-testing/47069613#47069613
     */
    @Before
    public void setupMockMvc() {
        final MockHttpServletRequestBuilder defaultRequestBuilder = MockMvcRequestBuilders.get("/dummy-path");
        mock = MockMvcBuilders.webAppContextSetup(app).
                defaultRequest(defaultRequestBuilder).
                alwaysDo(result -> setSessionBackOnRequestBuilder(defaultRequestBuilder, result.getRequest())).
                apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }

    private MockHttpServletRequest setSessionBackOnRequestBuilder(final MockHttpServletRequestBuilder requestBuilder,
                                                                  final MockHttpServletRequest request) {
        requestBuilder.session((MockHttpSession) request.getSession());
        return request;
    }

    /**
     * https://github.com/json-path/JsonPath/tree/master/json-path-assert
     * @throws Exception
     */
    @Test
    public void testSwagger() throws Exception {
        // OpenAPI (swagger spec)
        mock.perform(MockMvcRequestBuilders.get("/api/openapi.json").
                    accept("application/json")).
                andExpect(MockMvcResultMatchers.status().isOk()).
                andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("\"swagger\":\"2.0\""))).
                andExpect(MockMvcResultMatchers.content().string(JsonPathMatchers.isJson()));
    }

    @Test
    public void testManagementHealth() throws Exception {
        // OpenAPI (swagger spec)
        mock.perform(MockMvcRequestBuilders.get("/management/health").
                accept("application/json")).
                andExpect(MockMvcResultMatchers.status().isOk()).
                andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("UP"))).
                andExpect(MockMvcResultMatchers.content().string(JsonPathMatchers.isJson()));
    }

    @Test
    @WithMockUser(roles = { "USER", "ADMIN" })
    public void testManagementInfo() throws Exception {
        // OpenAPI (swagger spec)
        mock.perform(MockMvcRequestBuilders.get("/management/info").
                accept("application/json")).
                andExpect(MockMvcResultMatchers.status().isOk()).
                andExpect(MockMvcResultMatchers.content().string(JsonPathMatchers.isJson()));
    }

    @Test
    public void testInteractiveLogin() throws Exception {
        MockHttpServletResponse redir = mock.perform(MockMvcRequestBuilders.post("/auth/authenticate").
                contentType("application/x-www-form-urlencoded").
                content("username=test&password=123").
                accept("application/json")).
                andExpect(MockMvcResultMatchers.status().is3xxRedirection()).
                andReturn().getResponse();

        String location = redir.getHeader("Location");
        Assert.assertEquals("/auth/success", location);

        String json = mock.perform(MockMvcRequestBuilders.get(location).
                accept("application/json")).
                andExpect(MockMvcResultMatchers.status().isOk()).
                andExpect(MockMvcResultMatchers.content().string(JsonPathMatchers.isJson())).
                andReturn().getResponse().getContentAsString();

        log.info("Got logged in user info JSON: {}", json);

        MockHttpServletResponse logout = mock.perform(MockMvcRequestBuilders.get("/auth/logout").
                accept("application/json")).
                andExpect(MockMvcResultMatchers.status().is3xxRedirection()).
                andReturn().getResponse();

        location = redir.getHeader("Location");
        Assert.assertEquals("/auth/success", location);

        json = mock.perform(MockMvcRequestBuilders.get(location).
                accept("application/json")).
                andExpect(MockMvcResultMatchers.status().isOk()).
                andExpect(MockMvcResultMatchers.content().string(JsonPathMatchers.isJson())).
                andReturn().getResponse().getContentAsString();

        log.info("Got logged out user info JSON: {}", json);
    }
}
