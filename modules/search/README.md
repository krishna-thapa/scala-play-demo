# Scala + PlayFramework + Elasticsearch + Docker

## Purpose of the project:

### Search functionality on authors
- Create an API endpoint that takes the author name and returns the first top 10 matched names from the Author columns in `quotations` table. Minimum length for the input text is 3.  
- Author search in the `quotations` table using Postgres `like` command. Returns first 10 distinct matched result. 
- Can be searched using lastname or any matched 3 letters in anywhere in the full name context

### Search functionality on quotes
- For the full text search on the quote, using the ElasticSearch database 
- Instead of installing the ES client locally, use the docker container 
- Create an API endpoint that takes the number of records in the path parameter and run the random API endpoint from the `quotations` table to get the records defined by the requested int parameter. Then it will store those data in the ES database under the index named: `quotes`. **This API should only be called by Admin role**. 
    - If the index is already present in ES then when you call create API endpoint again, it will delete the old index and create a new one. 
    - **TODO**: For the better performance on writing the data to an ES index for example if the input data records have size of 100000 then we need to use Akka streams batch to write in ES index. 
    - Create an API endpoint to delete the index from an ES. It will take the index name as a string in a pth parameter. **This should be used only for testing env and can only be done by Admin role.**
    - Create an API endpoint for the text search on the quote column. It should take a request body that has user input search text string and offset and limit integers. Offset and limit will be used for pagination for the UI front side. In the back-end I have used match with prefix ES API method that will match any text to the quote column and rules with the sorted score level. 
    - Minimum length for the text search is 3 bu word with 2 letters and space will be counted and will returns the result.
    - Future work: Can convert the Search as a microservice using lagom for the micro-service architecture 
    
## Use of technologies
Use of the ElasticSearch which is a search engine based on Apache Lucene, a free and open-source information retrieval software library. It provides a distributed, full-text search engine with an HTTP web interface and schema-free JSON documents.

Elasticsearch exposes [REST APIs](https://www.elastic.co/guide/en/elasticsearch/reference/current/rest-apis.html) that are used by the UI components and can be called directly to configure and access Elasticsearch features.

ElasticSearch is a document oriented. It stores and indexes documents. Indexing creates or updates documents. After indexing, you can search, sort, and filter a complete document. No need to go through each row of columnar data. This is a fundamentally different way of thinking about data and is one of the reasons ElasticSearch can perform a complex full-text search.

Documents are represented as JSON objects. JSON serialization is supported by most programming languages and has become the standard format used by the NoSQL movement.

## Library used:
- [elastic4s](https://github.com/sksamuel/elastic4s) is a library that can describe operations to ES with Scala DSL.
- `elastic4s-json-play` Use if you are dealing with play framework and play-json, it is a good idea to include it as an option.

## Start ElasticSearch/Kibana:
```
sudo systemctl start elasticsearch
sudo systemctl start kibana
```

## Stop ElasticSearch:
```
sudo systemctl stop elasticsearch
sudo systemctl stop kibana.service
```

## GET
```
curl -XGET 'http://localhost:9200/'
curl -X GET http://localhost:9200/_cat/indices
curl -X GET http://localhost:9200/quotes/_doc/_search | jq
```

## DELETE
```
curl -X DELETE http://localhost:9200/sink2  
```

### [Install jq in Ubuntu](https://www.howtoinstall.me/ubuntu/18-04/jq/)

## Resources:
- https://www.baeldung.com/elasticsearch-full-text-search-rest-api
- https://sksamuel.github.io/elastic4s/
- https://qiita.com/n-gondo123/items/63c067ca9ada30e240c0

## Test using Docker:
- Use of [Set of utility classes to make integration testing with dockerised services in Scala easy.](https://github.com/whisklabs/docker-it-scala)
- Use of [Java API client for Docker](https://github.com/docker-java/docker-java) 
- Write some test cases for [Elastic4s](https://github.com/sksamuel/elastic4s#example-application)