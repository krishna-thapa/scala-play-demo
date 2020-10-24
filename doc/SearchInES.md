# Scala + PlayFramework + Elasticsearch + Docker

## Purpose of the project:
Use of the ElasticSearch which is a search engine based on Apache Lucene, a free and open-source information retrieval software library. It provides a distributed, full-text search engine with an HTTP web interface and schema-free JSON documents.

Elasticsearch exposes [REST APIs](https://www.elastic.co/guide/en/elasticsearch/reference/current/rest-apis.html) that are used by the UI components and can be called directly to configure and access Elasticsearch features.

ElasticSearch is a document oriented. It stores and indexes documents. Indexing creates or updates documents. After indexing, you can search, sort, and filter complete documents—not rows of columnar data. This is a fundamentally different way of thinking about data and is one of the reasons ElasticSearch can perform a complex full-text search.

Documents are represented as JSON objects. JSON serialization is supported by most programming languages and has become the standard format used by the NoSQL movement.

## Library used:
- [elastic4s](https://github.com/sksamuel/elastic4s) is a library that can describe operations to ES with Scala DSL.
- `elastic4s-json-play` Is not required (described later), but if you are dealing with play framework and play-json, it is a good idea to include it as an option.

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

## [Install jq in Ubuntu](https://www.howtoinstall.me/ubuntu/18-04/jq/)

## Resources:
- https://www.baeldung.com/elasticsearch-full-text-search-rest-api
- https://sksamuel.github.io/elastic4s/
- https://qiita.com/n-gondo123/items/63c067ca9ada30e240c0

## TODO:
- Write Docker and run ES in Docker instead of Local env: https://www.elastic.co/guide/en/elasticsearch/reference/7.3/docker.html
- Write some test cases: https://github.com/sksamuel/elastic4s#example-application