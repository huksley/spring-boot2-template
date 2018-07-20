package com.github.huksley.app.jpa;

import com.github.huksley.app.model.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Example JPA todo storage entrypoint
 */
@RepositoryRestResource
public interface TodoRepository extends JpaRepository<Todo, String> {
}
