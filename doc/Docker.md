## Docker
Docker is an open-source containerization platform that allows you to quickly build, test, and deploy applications as portable containers that can run virtually anywhere. A container represents a runtime for a single application and includes everything the software needs to run.

Docker is an integral part of modern software development and DevOps continuous integration and deployment pipelines.

### Install Docker on Ubuntu 20.04
Link [here](https://linuxize.com/post/how-to-install-and-use-docker-on-ubuntu-20-04/)

### Install Docker compose on Ubuntu 20.04
Link [here](https://www.digitalocean.com/community/tutorials/how-to-install-and-use-docker-compose-on-ubuntu-20-04)

### docker-compose cheatsheet
Link [here](https://devhints.io/docker-compose)

```
docker-compose start
docker-compose stop

docker-compose pause
docker-compose unpause

docker-compose ps
docker-compose up

// To stop the cluster: 
docker-compose down

// To destroy the cluster and the data volumes
// (Be careful as it will delete the volumn):
docker-compose down -v
```

## Install Postgres Server in Docker

- [Docker compose up with Postgres tips](https://hashinteractive.com/blog/docker-compose-up-with-postgres-quick-tips/)
- [Use volumes in Docker](https://docs.docker.com/storage/volumes/)

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


## Install ElasticSearch Server in Docker
Link [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.3/docker.html#docker-prod-cluster-composefile)

Data volumes will persist, so it’s possible to start the cluster again with the same data using `docker-compose up`. To destroy the cluster and the data volumes, just type `docker-compose down -v`.


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

[How To Configure Redis + Redis Commander + Docker](https://hackernoon.com/how-to-configurate-redis-redis-commander-docker-616136f2)

- create volume `redis-data`: This is where all redis data will be stored, even if container is restarted, data will be there.
- command: ["redis-server", "--appendonly", "yes"] - I start redis in persistent storage mode
- REDIS_HOSTS=local:redis:6379 - tells redis commander how to connect to redis

### Connect to Redis cli running in docker
- Get the Container id of Redis container running in docker

**TODO: Need to protect redis commander interface with login/password using redis-commander. See above link**

```
docker ps
```

- Connect to the redis-cli
```
docker exec -it <container-id> redis-cli
```

- Basic commands:
```
// Get all the lists
KEYS * 

// Delete the list
DEL cache-random-quote
DEL cache-quoteOfTheDay

// Get all the stored values inside the list
LRANGE cache-random-quote 0 -1
LRANGE cache-quoteOfTheDay 0 -1
```