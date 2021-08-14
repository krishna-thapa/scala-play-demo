# Overview of the project

## To build and run the project

### Prerequisite
1. Java JDK (>8.0), Scala and sbt tool
2. IDEA(only required to play around with the project locally)
3. Docker and docker compose for your OS to run databases in container
4. Postgres command tool to run the CSV data migration

### Steps to run the project locally using sbt run
1. Git clone or download the project from [github](https://github.com/krishna-thapa/inspirational-quote-api)
2. Import the sbt project using your choice of IDEA
3. Uncomment section for local deployment in `.env` file and comment for other environments
4. Comment out `inspirationa-quote-api` from line 5-23 in `docker-compose.yml` file, this is the docker image of the project itself
5. Make changes volume local for Postgres container as per the operating system - is explained in comment under Postgres volume in `docker-compose.yml`
6. Run Docker compose command to start and run databases: `docker-compose up`
7. Run the script to migrate the CSV file: `./csv_migration.sh`, make sure you have psql command tool installed
8. Run sbt command: `sbt clean compile test`
9. Run the sbt command to run the project in localhost: `sbt run`
10. Goto localhost 9000 to see swagger API management: `http://localhost:9000/docs/swagger-ui/index.html?url=/assets/swagger.json`

### Steps to run the project locally using docker containers
1. Download `docker-compose.yml` file only from the repo
2. Configure Postgres volume depending on your OS
3. Run `docker-compose up`
4. Wait for the `inspirational-quote-api` to start on local server in `HTTP on /0.0.0.0:9000`   
5. Run the script to migrate the CSV file: `./csv_migration.sh`, make sure you have psql command tool installed
6. Swagger API management: `http://localhost:9000/docs/swagger-ui/index.html?url=/assets/swagger.json`
7. Logs management in Kibana: `http://localhost:5601`, read more doc on `/doc/Docker-logs-ELK-stack.md`.

### Use of `.env` file
1. While running locally with sbt run instead of running docker image of `inspirationa-quote-api`
    - Have to uncomment all the environment variables that are declared for the local environment 
    - Host name for each of the container used (databases) has to be `localhost`
    - Can declare username and password that can be used for local deployment 
2. While running whole project as a docker image of `inspirationa-quote-api`
    - Have to uncomment all the environment variables that are declared for the dev environment
    - Host name for each of the container used (databases) has to be name of the docker container: `container_name`
    - Can declare username and password that can be used for dev deployment 
3. **TODO**: Can make use of the dependency injection by using environment name as a condition to insert different config for the containers

### Structure of the project
Project is divided using the sbt sub-projects modules. These project has four sub-modules inside the modules' folder. Each module beside the `common` is play application that has its own routes configuration file and own `build.sbt` file. Each play module has its own controller, service and test. All the common models and methods are defined in the `common` sbt project. All the configuration and dependency of the modules can be found in the root `build.sbt` file.

* `auth` will contain all the specific code for the authorization and authentication service using postgres database.
* `quotes` will contain all the specific code for the CRUD operation in quote service using postgres and Redis cache database.
* `search` will contain all the specific code for the search service using elastic search database.
* `common` will contain all the common code shared between the other subprojects.

This is the basic structure of the whole project:
```
inspirational-quote-api
 └ build.sbt
 └ app
    └ controllers
      └ CustomQuote.scala
    └ scala.daos
    └ forms
    └ table
    └ model
    └ util
 └ conf
   └ application.conf
   └ root routes
   └ docker config 
   └ play evolutions
 └ project
   └ build.properties
   └ plugins.sbt
   └ Dependencies.scala
 └ modules
   └ auth
     └ build.sbt
     └ app
       └ controllers
         └ AuthController.scala
       └ model
       └ util
     └ conf
       └ Auth.routes
     └ project
     └ test
   └ search
     └ ...
   └ quotes
     └ ...
   └ common
     └ ...
```

## Technologies/libraries used so far: Need to update
- Back-end language
    - Scala as back-end programming language
    - SBT for built tool
- Web framework
    - Play framework with AkkaHttp as web framework
- Tools
    - scalafmt for scala formatting
    - ScalaTest for test framework
    - Swagger for Api management
- Database scheme
    - Postgres for RDBMS
    - Slick as Functional Relational Mapping
    - Redis for cache store management
    - ElasticSearch for indices storage 
    - Kibana and Filebeat for the log management tools
- CI/CD tools
    - Docker compose
    - Git
    - Github Action for CI/CD tool
-Front-end language and framework
    - Javascript as programming language
    - Vue.js as framework
