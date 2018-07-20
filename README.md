# Spring Boot 2 sample application

Opinionated Spring Boot 2 template.
Contains following functionality:

  * Spring Boot 2 
  * Web classic API (no webflux, sorry)
  * Actuator enabled (/management/info and /management/health endpoints ONLY)
  * Redis configured (but disabled as cache by default)
  * Postgres configured (enabled by default)
  * Swagger2 API docs enabled
  * Spring Security enabled (simple login as test:123)
  * Web interface is done as ReactJS app (create-react-app)
  * Sample welcome, login and logout forms
  * Docker build enabled
  * Docker compose setup with everything 
  * JWT security token generation
  
## Endpoints

  * Application (protected) - http://localhost:8087/
  * Swagger UI - http://localhost:8087/swagger-ui.html
  * API OpenSchema - http://localhost:8087/swagger.json
  * Information endpoint (protected) - http://localhost:8087/management/info
  * Health endpoint (unprotected) - http://localhost:8087/management/health
  * UI - http://localhost:8087/
  * UI (react app) - http://localhost:3000/
  
![Screenshots](screenshots.png)
  
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

3. Go to src/main/resources/frontend folder and execute watch build UI:

```bash
yarn install
yarn run build
```

4. Run app in Idea IDE

5. (optional) Better way to develop is to run app and ui along side. All API endpoints are proxied front frontend webpack dev server to backend.

```bash
cd src/main/resources/frontend
yarn run start
```

open http://localhost:3000 and it will be automatically reloaded during modification.

## Production deployment

Everything is ready to run as container in Docker Compose setup.
Execute:

```bash
mvn package
docker-compose up -d
```

