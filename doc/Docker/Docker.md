## Install Docker and Docker compose

### Install Docker on Ubuntu 20.04

Link [here](https://linuxize.com/post/how-to-install-and-use-docker-on-ubuntu-20-04/)

### Install Docker compose on Ubuntu 20.04

Link [here](https://www.digitalocean.com/community/tutorials/how-to-install-and-use-docker-compose-on-ubuntu-20-04)

### docker-compose cheatsheet

Link [here](https://devhints.io/docker-compose)

### Quick commands:

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
  `\dt`
- See the table description
  `\d <table_name>`

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

[How To Configure Redis + Redis Commander + Docker](https://hackernoon.com/how-to-configurate-redis-redis-commander-docker-616136f2)

- create volume `redis-data`: This is where all redis data will be stored, even if container is restarted, data will be there.
- command: ["redis-server", "--appendonly", "yes"] - Start redis in persistent storage mode
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
DEL cache-quoteOfTheDay

// Get all the stored values inside the list
LRANGE cache-random-quote 0 -1
LRANGE cache-quoteOfTheDay 0 -1
```

### Docker commands

```
// Create a Docker image from Dockerfile:
docker build -t inspirational-quote-api:latest .

// See docker images with id
docker image la -a

// Create a tag with dockerhub username:
docker tag b6e1d7960e75 krishnathapa/inspirational-quote-api:v0.1

// Push to the dockerhub
docker push krishnathapa/inspirational-quote-api:v0.1

// Remove imgage
docker image rm <iamge ids>

// see all the containers
docker container ls --all

// Stop container and remove
docker stop <container id>
docker rmi 

// Error response from daemon: conflict: unable to delete b6e1d7960e75 (must be forced) - image is referenced in multiple repositories
// First remove all the tag and then delete the docker image
docker rmi inspirational-quote-api:v0.1
docker rmi b6e1d7960e75

// sh into the docker running container:
docker exec -it c142a1ec9f31 /bin/sh
```

### How to Do a Clean Restart of a Docker Instance

```
// Stop the container(s) using the following command:
docker-compose down

// Delete all containers using the following command:
docker rm -f $(docker ps -a -q)

// Delete all volumes using the following command:
docker volume rm $(docker volume ls -q)

// Restart the containers using the following command:
docker-compose up -d
```

## Known errors while setting up docker:

- [Docker Container exited with code 137](https://www.petefreitag.com/item/848.cfm#:~:text=As%20it%20turns%20out%20this,in%20and%20terminated%20the%20process.)
- Problem faced while working [sbt-native-packager](https://sbt-native-packager.readthedocs.io/en/latest/): While running the `sbt docker:publishLocal`, there was an error regarding swagger package. I believe this error is related with swagger library, and I don't think there is a solution unless the library is removed from the project. [Error issue page](https://github.com/iheartradio/play-swagger/issues/190).
- PostgreSQL Database directory appears to contain a database; Skipping initialization - empty `pgdata` directory and run again `docker-compose up`
