package com.github.huksley.app;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestHello {
    private final Logger log = LoggerFactory.getLogger(getClass().getName());

    @Before
    public void setup() {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() {
        log.info("Hello from test!");
    }
}
