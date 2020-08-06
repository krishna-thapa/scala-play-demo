# Over-view of the project

## Initialise the postgres database
- Run the script file under `/scripts/setup-db`
- Script should create the database and run the `dbsetup.sql`
- Data from `data/Quotes.csv` file should be loaded to the tables that are coded in the `dbsetup.sql`

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

## Major issues faces so far
- Split routes are not recompiled: https://stackoverflow.com/questions/55289199/the-generated-route-files-of-play-framework-are-re-generated-automatically-even
- Disable the CSRF filter for a specific route in the routes file: add the nocsrf modifier tag before your route (for POST, PUT and DELETE)
- Use of enumeration with slick in play framework
    - Update a column with type enumeration using play-slick: Need to define the custom column type: https://stackoverflow.com/questions/47944361/play-slick-updating-enumeration-column
    - Implement an implicit Writes or Format for a case class including an Enumeration: https://github.com/jethrogillgren/play-samples/blob/workingversion/play-scala-hello-world-tutorial/app/models/Search.scala
    
## Coming improvements:
- Change the created date to Instant type
- Put validation in the create custom and update record
- Test using H2

## Epic to resolve not to get random record that has been called like within past 500 records
- See [play caching](https://www.playframework.com/documentation/2.8.x/ScalaCache)
- Use of stack to store and remove the old one once the new are added and give size 

## Epic to put the functionality for search (filter, pagination, auto-complete)
- Text search using postgres
- https://github.com/tminglei/slick-pg
- https://www.compose.com/articles/mastering-postgresql-tools-full-text-search-and-phrase-search/ 

## Epic to insert a new genre and store list of genre and get all distinct genre from all tables