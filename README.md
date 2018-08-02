# Spring Boot 2 sample application

Opinionated Spring Boot 2 template.
Contains following features:

  * Java 8 Oracle JDK (base image https://hub.docker.com/r/huksley/oracle-java/)
  * Spring Boot 2 
  * Web classic API (no webflux, sorry)
  * Actuator enabled (/management/info and /management/health endpoints ONLY)
  * Redis configured (but disabled as cache by default)
  * Postgres configured (enabled by default)
  * Swagger2 API docs enabled (Swagger UI)
  * Spring Security enabled (Simple login as test:123)
  * Web interface is done as ReactJS app (create-react-app)
  * Sample welcome, login and logout forms
  * Sample Todo CRUD
  * Docker build (mvn package creates docker image) 
  * Docker compose setup with everything (launches redis, postgres, app)
  * JWT based external call/command line compatible auth
  * Asciidoc & javadoc generation (CircleCI build)
  
## Endpoints

  * Application (protected) - http://localhost:8087/
  * Swagger UI - http://localhost:8087/swagger-ui.html
  * API OpenSchema - http://localhost:8087/api/openapi.json
  * Information endpoint (protected) - http://localhost:8087/management/info
  * Health endpoint (unprotected) - http://localhost:8087/management/health
  * UI (react app, dev run) - http://localhost:3000/
  * Documentation - https://huksley.github.io/spring-boot2-template/
  * Javadoc - https://huksley.github.io/spring-boot2-template/javadoc/

## Screenshots

All screenshots provided here is taken automatically, using Selenium + Geckodriver. 
See section below __Automatic UI testing__.  
                                   
![Landing page](screenshot-landing.png)
![Login form](screenshot-login.png)
![After login](screenshot-loggedin.png)
![Todo example](screenshot-todo.png)
![Management Health endpoint](screenshot-management-health.png)
![Management Info endpoint](screenshot-management-info.png)
![OpenAPI specification](screenshot-openapi-json.png)
![Swagger UI](screenshot-swagger-ui.png) 
![After logout](screenshot-loggedout.png)
 
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

## Configurable variables

Look for a list of configurable variables in docker-compose.yml

## Log streaming

Project includes configuration to send logs to GELF compatible receivers - Fluentd (EFK), Logtstach (ELK), Graylog, etc...
To activate it, provide -Dlogging.config=classpath:logback-spring-gelf.xml in JAVA_OPTS when starting application.

To distinguish logs from this particular application provide -Dgelf.container=app-name in JAVA_OPTS. This will result in custom field container_name in GELF messages.

## Automatic UI testing

For automatic UI testing we are using Selenium Java and Geckodriver (Firefox).
For more info about geckodriver see here: https://github.com/mozilla/geckodriver

To run example TestSelenium.java class, provide following environment variables:

  * BROWSER = firefox
  * BROWSER_BINARY = full path to browser binary (Firefox 52+)
  * BROWSER_DRIVER = full path to geckodriver (download recent from https://github.com/mozilla/geckodriver)

