package com.github.huksley.app.api;

import com.github.huksley.app.jpa.TodoRepository;
import com.github.huksley.app.model.Todo;
import com.github.huksley.app.system.CrudControllerBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Example API controller endpoint
 */
@RestController
@RequestMapping("/api/todo")
@Api(description = "Todo management")
@Secured("ROLE_USER")
public class TodoController extends CrudControllerBase<Todo> {
    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    TodoRepository repo;

    @Autowired(required = false)
    CacheManager cache;
    
    @Override
    public JpaRepository<Todo, String> repo() {
        return repo;
    }

    @Cacheable(cacheNames = "todo", key = "#p0")
    @Override
    @ApiOperation("Return single object")
    @GetMapping(path = { "/{id}" }, produces = "application/json")
    public Todo crudFindById(String id) {
        log.info("Get Todo by id: {}", id);
        return super.crudFindById(id);
    }

    @CacheEvict(cacheNames = "todo", key = "#p0")
    @ApiOperation("Update object")
    @PatchMapping(path = "/{id}", produces = "application/json", consumes = "application/json")
    public Todo crudUpdate(@PathVariable("id") String id, @RequestBody Todo update) {
        return super.crudUpdate(id, update);
    }

    @CacheEvict(cacheNames = "todo", key = "#p0")
    @ApiOperation("Delete object by id")
    @DeleteMapping(path = "/{id}", produces = "application/json", consumes = "application/json")
    public Map<String, Object> crudDelete(@PathVariable("id") String id) {
        return super.crudDelete(id);
    }

    /**
     * Declare overrides for methods to be secured by @{@link Secured} by annotation above
     */
    @Override
    @ApiOperation("Create object")
    @PostMapping(path = "/", produces = "application/json", consumes = "application/json")
    public Todo crudAdd(@RequestBody Todo obj) {
        return super.crudAdd(obj);
    }

    @ApiOperation("Filter")
    @GetMapping(path = { "/filter" }, produces = "application/json")
    public List<Todo> crudFilter(@RequestParam(required = false) Boolean completedToday, @RequestParam(required = false) Boolean completed) {
        // FIXME: must use JPA queries, not findAll
        Calendar now = Calendar.getInstance();
        Calendar startDay = now;
        Calendar endDay = now;
        List<Todo> all = repo().findAll();
        log.info("Found all todos: ", all.size());
        return all.stream().filter((t) -> {
                     // Should be completed
            return (completed != null ? t.getDone() == completed : true) &&
                    // Completed today
                    (completedToday != null ? t.getDone() && t.getDoneTime() != null && t.getDoneTime().after(startDay) && t.getDoneTime().before(endDay) : false);
        }).collect(Collectors.toList());
    }
}
