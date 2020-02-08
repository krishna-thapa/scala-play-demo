# Over-view of the project

## Markdown Cheatsheet
- https://github.com/tchapi/markdown-cheatsheet

## Technologies/libraries used so far
- Scala as back-end programming language
- SBT
- Play framework with AkkaHttp
- Slick
- Postgres
- scalafmt for scala formatting 
- Swagger Api management
- Docker compose
- Git
- Github Action for CI/CD tool

## Technologies to be added in the future
- Test framework: ScalaTest or Specs2
- Cats library
- Pure Config and Typesafe Config
- Spray JSON
- Lagom for microservices 
- External API services 
- AWS services implementation

## Tickets to do
- Make genre as enum (http requests not working atm)
- Make genre enum as list of genre
- Make genre as option to hold empty 
- Docker compose
- fix id issue that should start from 1 when table is dropped

## Major issues faces so far
- Splitted routes are not recompiled: https://stackoverflow.com/questions/55289199/the-generated-route-files-of-play-framework-are-re-generated-automatically-even
- Disable the CSRF filter for a specific route in the routes file: add the nocsrf modifier tag before your route (for POST, PUT and DELETE)
- Use of enumeration with slick in play framework
    - Update a column with type enumeration using play-slick: Need to define the custom column type: https://stackoverflow.com/questions/47944361/play-slick-updating-enumeration-column
    - Implement an implicit Writes or Format for a case class including an Enumeration: https://github.com/jethrogillgren/play-samples/blob/workingversion/play-scala-hello-world-tutorial/app/models/Search.scala