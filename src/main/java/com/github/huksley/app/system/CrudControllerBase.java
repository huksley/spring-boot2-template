package com.github.huksley.app.system;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.github.huksley.app.BaseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.google.common.base.Preconditions;

import io.swagger.annotations.ApiOperation;

/**
 * Simple CRUD API base for REST API
 */
@NoRepositoryBean
public abstract class CrudControllerBase<T extends BaseEntity> {
    Logger log = LoggerFactory.getLogger(getClass());
    
    public abstract JpaRepository<T, String> repo(); 
    
    public T findExample(HttpServletRequest request) {
        return null;
    }
    
    @ApiOperation("Return list of all objects")
    @GetMapping(path = { "/list", "/list/" }, produces = "application/json")
    public List<T> crudList(HttpServletRequest request) {
        T o = findExample(request);
        if (o != null) {
            Example<T> ex = Example.of(o);
        	return repo().findAll(ex);
        } else {
            return repo().findAll();
        }
    }
    
    @ApiOperation("Delete object by id")
    @PostMapping(path = "/delete/{id}", produces = "application/json", consumes = "application/json")
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
    	return repo().saveAndFlush(obj);
    }    

    @ApiOperation("Update object")
    @PatchMapping(path = "/{id}", produces = "application/json", consumes = "application/json")
    public T crudPost(@PathVariable("id") String id, @RequestBody T update) {
    	Preconditions.checkNotNull(id);
    	T saved = repo().findById(id).get();
    	if (saved != null) {
        	BeanUtils.copyProperties(update, saved, "id", "created", "updated", "version");
        	saved = repo().saveAndFlush(saved);
        	return saved;
    	} else {
    	    throw new ResourceNotFoundException("id = " + id);
    	}
    }    
}