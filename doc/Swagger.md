## [Swagger](https://swagger.io/)

- [Swagger API spec generator for Play]https://github.com/iheartradio/play-swagger)
- Describes the responses using the [Swagger API](https://swagger.io/docs/specification/2-0/describing-responses/)
- [Swagger with Play](https://medium.com/@sahebmotiani/swagger-with-play-all-you-need-to-know-d9147089d990)
- [Swagger UI dependency](https://mvnrepository.com/artifact/org.webjars/swagger-ui)

## Problems 
- I have used two route files and have added both of them in a single play routes so that two controller will have two different route files. I think the compiler doesn't seem to run properly if the controller are under the same package. Splitting the controllers in 2 different package fixed the recompilation loop. I have to use the `routes.controlller` method to redirect the page. 
- [Similar problem](https://stackoverflow.com/questions/55289199/the-generated-route-files-of-play-framework-are-re-generated-automatically-even) 