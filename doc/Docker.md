## Docker
Docker is an open-source containerization platform that allows you to quickly build, test, and deploy applications as portable containers that can run virtually anywhere. A container represents a runtime for a single application and includes everything the software needs to run.

Docker is an integral part of modern software development and DevOps continuous integration and deployment pipelines.

## How to Install Docker on Ubuntu 20.04
Link [here](https://linuxize.com/post/how-to-install-and-use-docker-on-ubuntu-20-04/)

## Install Docker compose on Ubuntu 20.04
Link [here](https://www.digitalocean.com/community/tutorials/how-to-install-and-use-docker-compose-on-ubuntu-20-04)

## docker-compose cheatsheet
Link [here](https://devhints.io/docker-compose)

```
docker-compose start
docker-compose stop

----------
docker-compose pause
docker-compose unpause

----------
docker-compose ps
docker-compose up


To stop the cluster: 
docker-compose down

To destroy the cluster and the data volumes, just type:
docker-compose down -v
```
## Install ElasticSearch with Docker
Link [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.3/docker.html#docker-prod-cluster-composefile)

### Elasticsearch in Docker
Is also available as Docker images. The images use centos:7 as the base image.
To inspect status of the cluster in Docker:
```
curl http://127.0.0.1:9200/_cat/health
curl -X GET http://127.0.0.1:9200/_cat/indices
curl -X GET http://127.0.0.1:9200/quotes/_doc/_search | jq
```