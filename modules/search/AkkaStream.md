## Introduction: Reactive Streams
The Reactive Streams initiative defines a interoperating specification for what is essentially dual-channel streaming: one channel for pushing elements of data, and another to propagate back demand signals to upstream senders.

The [akka-streams library](https://doc.akka.io/docs/akka/current/stream/index.html) is built atop of the Akka actor framework, which adheres to the [reactive streams manifesto](https://www.reactive-streams.org/). The Akka Streams API allows us to easily compose data transformation flows from independent steps. Moreover, all processing is done in a reactive, non-blocking backpressure, and asynchronous way.

### Articles
- [Guide to Akka Streams](https://www.baeldung.com/akka-streams)
- [Introduction to Akka Streams](https://medium.com/@arcagarwal/introduction-to-akka-streams-5155bd070e37)

## Problem: Bulk Indexing Data
In this project, we have a huge amount of data(not really) on the Postgres database that is imported from the CSV file. The data act as a core database for the `quotes` table. Supplementing this would be some kind of specialized search index(ElasticSearch) that serves as a view upon that canonical data for when tradeoffs need to be made with speed, full-text search, and SQL.

- We have a table in PostgreSQL populated with `quotes` data that needs to be indexed in Elasticsearch for nebulously defined search reasons.
- We can’t assume we can hold the entire table in memory.
- We have some transformations/enriching of table records that need to be executed on the fly.
- Indexing should be reasonably fast (and tunable) without overwhelming the Elasticsearch cluster.

## Solution
Solution for this simple case is to `stream` the table records from PostgreSQL to Elasticsearch as our bulk indexing operation.

- First Thing’s First: Load Into PostgreSQL -> This is done using the Play evolution that does the database migration by loading the CSV file into the Postgres table when the application is initially started 
- Setting Up Dependencies -> Play comes with Akka system, actors and steam dependencies. Have to add extra stream libraries for the [elastic search dependency](https://github.com/sksamuel/elastic4s) that we are using in this project. 
- Streaming Out From Slick -> Play-slick comes with the Subscriber and Publisher that is used as an Akka stream. Call `db.stream` to get a DatabasePublisher reference, which doesn’t begin execution until something forces it, in our case, it would be the ElasticSearch Sink that will initiate the stream as Akka Stream act as lazy evaluation. Have to create an akka-streams Source from the Slick DatabasePublisher – this gently slides us into akka-streams.
- Processing Elements with akka-streams Flow -> we can add some processing/business logic steps to perform in flight for each element coming from the Source quotes dataset. We can chain as many as flow that we would like to have. 
- **TODO** Add new flow that uses some API to add details for an author to each quote. It can be like adding image, or some information regarding author. Can use play ws library to get the API response and add in new class to return future response. Maybe use [MediaWiki](https://www.mediawiki.org/wiki/API:Main_page)
                                                


## Resources