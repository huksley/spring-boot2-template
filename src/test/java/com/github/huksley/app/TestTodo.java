package com.github.huksley.app;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.huksley.app.api.TodoController;
import com.github.huksley.app.jpa.TodoRepository;
import com.github.huksley.app.model.Todo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestTodo {
    private final Logger log = LoggerFactory.getLogger(getClass().getName());


    @Autowired
    TodoController todoApi;

    @Autowired
    TodoRepository todoDb;

    @Autowired
    ObjectMapper json;

    @Before
    public void setup() {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testBeanUtilsCopyProperties() {
        Todo update = Todo.builder().done(true).description("Hello").build();
        Todo existing = Todo.builder().done(false).description("World!").build();
        BeanUtils.copyProperties(update, existing, "id", "created", "updated", "version");
        Assert.assertTrue(existing.getDone());
    }

    @Test
    public void testTodoApi() throws IOException {
        Todo t1 = Todo.builder().description("This is example todo").done(false).build();
        log.info("Todo initial: {}", t1);
        t1 = todoDb.save(t1);
        Assert.assertNotNull(t1);
        log.info("Todo created: {}");
        log.info("Todo creation date: {} class {}", t1.getCreated(), t1.getCreated().getClass());
        Todo found = todoApi.crudFindById(t1.getId());
        Assert.assertNotNull(found);

        String t1Json = json.writeValueAsString(t1);
        String foundJson = json.writeValueAsString(found);
        log.info("Todo created JSON: {}", t1Json);
        log.info("Todo created and retrieved JSON: {}", foundJson);

        log.info("Todo created and retrieved: {}", found);
        log.info("Todo created and retrieved date: {} class {}", found.getCreated(), found.getCreated().getClass());
        Assert.assertEquals(found, t1);

        Assert.assertEquals(foundJson, t1Json);

        Todo foundAndParsed = json.readValue(foundJson, Todo.class);
        Assert.assertEquals(found, foundAndParsed);

        found.setDone(true);
        found.setDoneTime(Calendar.getInstance());
        todoDb.save(found);

        List<Todo> todos = todoApi.crudFindAll();
        Assert.assertNotNull(todos);
        Assert.assertTrue(todos.size() > 0);
        Assert.assertNotNull(todos.contains(found));

        log.info("Added through API: {}", todoApi.crudAdd(Todo.builder().done(false).description("Added through API").build()));
        log.info("Updated through API: {}", todoApi.crudUpdate(found.id, Todo.builder().done(found.getDone()).doneTime(found.getDoneTime()).description("New description").build()));
    }
}
