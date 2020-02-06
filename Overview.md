# Over-view of the project

## Technologies/libraries used so far
- Scala as back-end programming language
- SBT
- Play framework with AkkaHttp
- Slick
- Postgres
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
- Make genre as enum 
- Make genre enum as list of genre

## Major issues faces so far
- Splitted routes are not recompiled: https://stackoverflow.com/questions/55289199/the-generated-route-files-of-play-framework-are-re-generated-automatically-even
- Disable the CSRF filter for a specific route in the routes file: add the nocsrf modifier tag before your route (for POST, PUT and DELETE)