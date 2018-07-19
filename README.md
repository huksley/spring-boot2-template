# Spring Boot 2 sample application

Opinionated Spring Boot 2 template.
Contains following functionality:

  * Spring Boot 2 
  * Web classic API (not functional)
  * Actuator enabled (/management/info and /management/health endpoints ONLY)
  * Redis configured (disabled as cache by default)
  * Postgres configured (enabled by default)
  * Swagger2 API docs enabled
  * Spring Security enabled (test:123)
  * Web interface is done as VueJS with static build by gulp
  * Sample login and user information forms
  * Docker build enabled
  * Docker compose setup with everything (TBD)
  
## Endpoints

  * Application (protectedh) - http://localhost:8087/
  * Swagger UI - http://localhost:8087/swagger-ui.html
  * API OpenSchema - http://localhost:8087/swagger.json
  * Information endpoint (protected) - http://localhost:8087/management/info
  * Health endpoint (unprotected) - http://localhost:8087/management/health
  * User info page - http://localhost:8087/auth
  * Logout - http://localhost:8087/auth/logout
  * Login form - http://localhost:8087/auth/login
  
## Development

Follow these steps for development environment setup:

1. Start Postgres in docker before launching this projects.

```bash
docker run --name postgre -e POSTGRES_PASSWORD=123 -e POSTGRES_USER=user -e POSTGRES_DB=test -p 5432:5432 -d postgres
```

2. Start Redis in docker (optional)

```bash
docker run --name redis -p 6379:6379 -d redis redis-server --appendonly yes
```

## Production deployment

