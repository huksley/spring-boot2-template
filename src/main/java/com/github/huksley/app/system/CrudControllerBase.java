package com.github.huksley.app.system;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.github.huksley.app.BaseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.google.common.base.Preconditions;

import io.swagger.annotations.ApiOperation;

/**
 * Simple CRUD API base for REST API
 */
@NoRepositoryBean
public abstract class CrudControllerBase<T extends BaseEntity> {
    Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Must be overriden to provide JPA repository to access objects.
     * @return
     */
    public abstract JpaRepository<T, String> repo();


    /**
     * /api/[type]/{id}
     * Gets information about object by {@link BaseEntity#id}
     * @param id ID of object
     * @return POJO of object
     */
    @ApiOperation("Return single object")
    @GetMapping(path = { "/{id}" }, produces = "application/json")
    public T crudFindById(String id) {
        Optional<T> o = repo().findById(id);
        if (o.isPresent()) {
            return o.get();
        } else {
            throw new ResourceNotFoundException("id = " + id);
        }
    }

    /**
     * Returns list of all objects from repo.
     * @return List
     */
    @ApiOperation("Return list of all objects")
    @GetMapping(path = { "/list" }, produces = "application/json")
    public List<T> crudFindAll() {
        return repo().findAll().stream().sorted((a,b) -> b.getUpdated().compareTo(a.getUpdated())).collect(Collectors.toList());
    }

    /**
     * De
     * @param id
     * @return
     */
    @ApiOperation("Delete object by id")
    @DeleteMapping(path = "/{id}", produces = "application/json", consumes = "application/json")
    public Map<String, Object> crudDelete(@PathVariable("id") String id) {
    	Preconditions.checkNotNull(id);
    	if (repo().findById(id) != null) {
    		repo().deleteById(id);
    		Map<String,Object> counts = new HashMap<>();
    		counts.put("count", repo().count());
    		return counts;
    	} else {
    	    throw new ResourceNotFoundException("id = " + id);
    	}
    }

    @ApiOperation("Create object")
    @PostMapping(path = "/", produces = "application/json", consumes = "application/json")
    public T crudAdd(@RequestBody T obj) {
    	Preconditions.checkNotNull(obj);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Preconditions.checkNotNull(auth);
        return repo().saveAndFlush(obj);
    }

    @ApiOperation("Update object")
    @PatchMapping(path = "/{id}", produces = "application/json", consumes = "application/json")
    public T crudUpdate(@PathVariable("id") String id, @RequestBody T update) {
    	Preconditions.checkNotNull(id);
    	Preconditions.checkState(update.getId() == null || update.getId().equals(id));
    	Optional<T> existing = repo().findById(id);
    	if (existing.isPresent()) {
        	BeanUtils.copyProperties(update, existing.get(), "id", "created", "updated", "version");
        	T updated = repo().saveAndFlush(existing.get());
        	return updated;
    	} else {
    	    throw new ResourceNotFoundException("id = " + id);
    	}
    }
}