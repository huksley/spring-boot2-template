package com.github.huksley.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestReqresGet {

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @EqualsAndHashCode()
    @ToString(callSuper = true)
    static class User {
        int id;
        String email;

        @JsonProperty("first_name")
        String firstName;

        @JsonProperty("last_name")
        String lastName;


        @JsonProperty("avatar")
        String avatarImageUrl;
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @EqualsAndHashCode()
    @ToString(callSuper = true)
    static class UserPageData {
        int page;
        @JsonProperty("per_page")
        int perPage;
        int total;

        @JsonProperty("total_pages")
        int totalPages;

        @Builder.Default
        List<User> data = new ArrayList<>();
    }

    @Autowired
    private TestRestTemplate rest;
    Logger log = LoggerFactory.getLogger(TestReqresGet.class);

    @Test
    public void testApi() {
        UserPageData result = rest.getForObject("https://reqres.in/api/users", UserPageData.class);
        log.info("Got result: {}", result);

        Assert.assertNotNull(result);

        Assert.assertNotNull(result.data);
        result.data.stream().forEach(u -> {
            log.info("User: {}", u);
        });
    }
}
