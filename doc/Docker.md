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

To destroy the cluster and the data volumes:
docker-compose down -v
```

## Install ElasticSearch Server in Docker
Link [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.3/docker.html#docker-prod-cluster-composefile)

### Elasticsearch in Docker
The images use centos:7 as the base image.

Run the `docker-compose up` to start the docker and to inspect status of the cluster in Docker:
```
curl http://127.0.0.1:9200
curl http://127.0.0.1:9200/_cat/health
curl -X GET http://127.0.0.1:9200/_cat/indices
curl -X GET http://127.0.0.1:9200/quotes/_doc/_search | jq
```

To see the ES match API, [refer here](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-match-query.html)

## Install Redis Server in Docker
[Configure and Run a Docker Container for Redis and Use it for Python](https://medium.com/better-programming/dockerizing-and-pythonizing-redis-41b1340979de)

### Connect to Redis cli running in docker
- Get the Container id of Redis container running in docker
```
docker ps
```

- Connect to the redis-cli
```
docker exec -it <container-id> redis-cli
```

- To see all the stored keys in the Redis server:
```
KEYS *
```

## Install Postgres Server in Docker
[DOCKER COMPOSE UP WITH POSTGRES QUICK TIPS](https://hashinteractive.com/blog/docker-compose-up-with-postgres-quick-tips/)

### Connect to the Postgres database running in docker
- First start the docker container with postgres image up and running
`docker-compose up`
- Connect to the postgres through terminal
`psql -h localhost -p 5432 -U  admin postgres`
- Password for admin role is *admin*
- Connect to the right database
`\c inspiration_db`
- See all the tables 
`\c dt`
