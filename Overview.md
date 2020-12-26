# Overview of the project

## To build and run the project

### Prerequisite
1. Download and install Java, Scala and sbt tool
2. Download and install your choice of an IDEA(no need for just to run the project)
3. Download and install a docker and docker compose for your OS to run databases virtually

### Steps to run the project locally
1. Git clone or download the project from github
2. Import the sbt project using your choice of IDEA
3. Run sbt command: `sbt clean compile test`
4. Run Docker compose command to start and run databases: `docker-compose up`
5. Run the sbt command to run the project in localhost: `sbt run`
6. Goto localhost 9000 with swagger API management: `http://localhost:9000/docs/swagger-ui/index.html?url=/assets/swagger.json`

### Structure of the project
Project is divided using the sbt sub-projects modules. These project has four sub-modules inside the modules folder. Each module beside the `common` is play application that has its own routes configuration file and own `build.sbt` file. Each play module has its own controller, service and test. All the common models and methods are defined in the `common` sbt project. All the configuration and dependency of the modules can be found in the root `build.sbt` file.

* `auth`: will contain all the specific code for the authorization and authentication service using postgres database.
* `quotes`: will contain all the specific code for the CRUD operation in quote service using postgres and Redis cache database.
* `search`: will contain all the specific code for the search service using elastic search database.
* `common`: will contain all the common code shared between the other subprojects.

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

## References links
- Markdown Cheatsheet: https://github.com/tchapi/markdown-cheatsheet
- Base for scala play project: https://github.com/playframework/play-samples/tree/2.8.x/play-scala-starter-example

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
- RDBMS database scheme
    - Postgres
    - Slick as Functional Relational Mapping
- CI/CD tools
    - Docker compose
    - Git
    - Github Action for CI/CD tool
-Front-end language and framework
    - Javascript as programming language
    - Vue.js as framework

## Technologies to be added in the future
- Test framework: ScalaTest or Specs2
- Cats library
- Pure Config and Typesafe Config
- Spray JSON
- Lagom for microservices 
- External API services 
- AWS services implementation

## Tickets to do in Back-end
- <del>Setup Postgres and write sql file to create database and table in the postgres
- Write sample sql queries to insert data into the table
- Upload sample data in JSON and CSV format 
- Upload CSV data into the postgres table 
- Implement Slick: **Don’t add jdbc dependency on the built.sbt with slick**
- 
- Make genre as enum (http requests not working atm)
- Use scalaftm for scala code formatting (create new config file to define custom formatting rules)
- Set-up Github Action for CI/CD tools: create a yml to run sbt test and compile every time code is merged to master 
- Use scalaftm stb task keys to run formatting check in guthub action </del>
- Make genre enum as list of genre: might not be needed
- Make genre as option to hold empty: for JSON data as it doesn't have genre field
- Docker compose
- fix id issue that should start from 1 when table is dropped
- Play around with JSON: store the data in Postgres as NoSQL 
- Do random read operation using NoSQL JSON data file 
- Introduce feature of making favorite in selected quotes

## Major issues faces so far
- Split routes are not recompiled: https://stackoverflow.com/questions/55289199/the-generated-route-files-of-play-framework-are-re-generated-automatically-even
- Disable the CSRF filter for a specific route in the routes file: add the nocsrf modifier tag before your route (for POST, PUT and DELETE)
- Use of enumeration with slick in play framework
    - Update a column with type enumeration using play-slick: Need to define the custom column type: https://stackoverflow.com/questions/47944361/play-slick-updating-enumeration-column
    - Implement an implicit Writes or Format for a case class including an Enumeration: https://github.com/jethrogillgren/play-samples/blob/workingversion/play-scala-hello-world-tutorial/app/models/Search.scala
    

# Major epic for the future work:
-[x] Add Play Cache
    - To resolve not to get random record which has been called like within past 500 records
    - Quote of the day has to be stored in cache for 5 days 
    - See [play caching](https://www.playframework.com/documentation/2.8.x/ScalaCache)
    - Use of stack to store and remove the old one once the new are added and give size
    - Look into how it can be stored and how to check the contains in efficient manner 
    - Time limit and speed and where to store the codes
    
-[x] Authorization and Authentication (might create a different microservice)
    - Use of JWT to create a token and use for authorization
    - Have to create a different database to store roles and user details in Postgres 
    - Password has to be hashed in the database 
    - Use of play session to store and validate jwt token in the backend
    - Token has to be stored in the client-server to pass them on each api calls in the header
    - Two roles and permissions for an admin and normal user  
    - JWT token should be validated for 24 hours and should have signature using play secret key from application.conf
    - Table for users will be initiated with default details for an admin
    - **TODO**: Might need to add email verification or to help when the user forget their password 

-[x] Search functionality for the project
    - Create an API endpoint that takes the author name and returns the first top 10 matched names from the Author columns in `quotations` table. Minimum length for the input text is 3.  
    - Author search in the `quotations` table using Postgres like command. Returns first 10 distinct matched result. 
    - Can be searched using lastname or any matched 3 letters in the name itself
    - **TODO**: Have to update the method so that first get the distinct authors from the table and apply the like command on top 10 data and sort the result 
    - For the full text search on the quote, using the ElasticSearch database 
    - Instead of installing the ES client locally, use the docker container 
    - Create an API endpoint that takes the number of records in the path parameter and run the random API endpoint from the `quotations` table to get the records defined by the requested int parameter. Then it will store those data in the ES database under the index named: `quotes`. **This API should only be called by Admin role**. 
    - If the index is already present in ES then when you call create API endpoint again, it will delete the old index and create a new one. 
    - **TODO**: For the better performance on writing the data to an ES index for example if the input data records have size of 100000 then we need to use Akka streams batch to write in ES index. 
    - Create an API endpoint to delete the index from an ES. It will take the index name as a string in a pth parameter. **This should be used only for testing env and can only be done by Admin role.**
    - Create an API endpoint for the text search on the quote column. It should take a request body that has user input search text string and offset and limit integers. Offset and limit will be used for pagination for the UI front side. In the back-end I have used match with prefix ES API method that will match any text to the quote column and rules with the sorted score level. 
    - Minimum length for the text search is 3 bu word with 2 letters and space will be counted and will returns the result.
    - Future work: Can convert the Search as a microservice using lagom for the micro-service architecture 

-[x] GET API using the Wordnik token and endpoints (separate microservice)
    - Get a word of the day 
    - Use different technology: ZIO, Http4s, Circe ..?
    - Run in different port and connect to font-end 

-[ ] Improvement with the Genre field
    - Make a table that holds Genre ??
    - Allows user to create a genre 
    - Record should have list of genre, wrapper in Option
    - Have to be a distinct genre in the table
    - `/quote/{genre}`: if not found then return meaningful error message body

-[ ] Update the version of Swagger to Open API 3.0.1

-[ ] Test using test containers

-[ ] Maybe write sample aws lambda functions for the demo purpose for the Serverless Architecture 

-[ ] Dockerized the whole sbt play project in the docker container and pass the environment variables to connect the databases and build the docker and publish to docker hub

-[ ] Upload images using play and MongoDB services(Start in a different module)
    - https://www.playframework.com/documentation/2.8.x/ScalaFileUpload
    - http://mongodb.github.io/mongo-java-driver/4.1/driver-scala/
    - https://dev.to/sonyarianto/how-to-spin-mongodb-server-with-docker-and-docker-compose-2lef
    - https://medium.com/@ievstrygul/wiring-scala-app-docker-container-with-mongodb-84b29c50ac5
    - See the sample play project
    
-[ ] Redis get connection lost after some time if the app is not active
    - Make Redis cache as persistence so that it won't lost the data once it get hard restart
    - https://github.com/docker-library/redis/issues/111
    
-[ ] Scheduling asynchronous tasks
    - Quote of the day has to be called every night to get the new random quote and store in Redis cache for 5 days
    - Might have to use Akka actors
    - Use Scala play [Scheduling asynchronous tasks](https://www.playframework.com/documentation/2.8.x/ScheduledTasks)
    - https://stackoverflow.com/questions/32178713/how-to-write-cron-job-in-play-framework-2-3
    - https://discuss.lightbend.com/t/how-to-create-a-cron-job-in-play-framework/4812
    - http://www.icharm.me/use-akka-quartz-scheduler-for-cron-schedule-in-play-framework-2-7.html

-[ ] Add Akka stream in elastic search service for adding batch while importing data from Postgres to Es
    - https://github.com/sksamuel/elastic4s/blob/master/docs/streams.md
    - https://www.reonomy.com/blog/post/reactive-streams-end-to-end-with-slick-akka-streams-and-elastic4s
    - https://qiita.com/kamijin_fanta/items/8d8eacfe23138eea0a26
    - https://github.com/sksamuel/elastic4s/blob/master/elastic4s-streams-akka/src/main/scala/com/sksamuel/elastic4s/akka/streams/ElasticSink.scala
    - https://stackoverflow.com/questions/45623990/elastic4s-akka-stream-sink-with-httpclient

### Consolidation 
-[x] Should allow logged in user to see only their own details in `/auth/user/{email}` and admin to look into anyone else details
-[ ] Only the ids that are present in the quotations tables should be allowed to store in the fav_quotations tables, right now any csvid can be stored in the table
-[ ] Getting `ERROR:  relation "play_evolutions" does not exist at character 72` while running docker container for postgres after applying play evolutions db migration. I can't see any error since the migration works fine and can see all the script running perfectly for now. Might have to check in more details regarding an error.  
-[ ] Put for `/customQuote/{id}` is not working, have to update the swagger implementation by removing the in parameter for formData to in body parameter with case class for the response body. Might be the effect of updating the Swagger. If we need to make it appear like a form data then we need to find alternative solution or fix
-[ ] Change the created date to Instant type
-[ ] Put validation in the create custom and update record
-[ ] Implement dependency management for sbt using: `https://github.com/scala-steward`
