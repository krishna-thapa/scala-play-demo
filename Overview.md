# Over-view of the project

## Run the Project using sbt
```bash
sbt clean compile test run
```
And then go to <http://localhost:9000> to see the running web application.

## References links
- Markdown Cheatsheet: https://github.com/tchapi/markdown-cheatsheet
- Base for scala play project: https://github.com/playframework/play-samples/tree/2.8.x/play-scala-starter-example

## Technologies/libraries used so far
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

## Tickets to do un Front-end
- Create a Vue.js project inside the same repo of inspirational-quote using vue ui 

## Major issues faces so far
- Splitted routes are not recompiled: https://stackoverflow.com/questions/55289199/the-generated-route-files-of-play-framework-are-re-generated-automatically-even
- Disable the CSRF filter for a specific route in the routes file: add the nocsrf modifier tag before your route (for POST, PUT and DELETE)
- Use of enumeration with slick in play framework
    - Update a column with type enumeration using play-slick: Need to define the custom column type: https://stackoverflow.com/questions/47944361/play-slick-updating-enumeration-column
    - Implement an implicit Writes or Format for a case class including an Enumeration: https://github.com/jethrogillgren/play-samples/blob/workingversion/play-scala-hello-world-tutorial/app/models/Search.scala