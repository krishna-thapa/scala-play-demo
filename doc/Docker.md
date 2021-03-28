## Run the project using containers
- Install a docker and docker-compose in the machine
- Create a directory anywhere to hold all the config and docker-compose file
- Copy the `docker-compose.yml` file in the directory 
- Copy the `.env` file to the same directory 
- Copy `data/Quotes.csv` file to the respective mounted volume directory for the postgres container's volume
  - for macOS: copy the CSV file to the same directory under the new folder `/pgdata`
  - for Ubuntu: copy the CSV file to the `/var/lib/postgresql/data/`, create the path if it doesn't exit
- Run the command: `docker-compose up`

## Docker and Docker compose
Docker is an open-source containerization platform that allows you to quickly build, test, and deploy applications as portable containers that can run virtually anywhere. A container represents a runtime for a single application and includes everything the software needs to run.

Docker is an integral part of modern software development and DevOps continuous integration and deployment pipelines.

Compose is a tool for defining and running multi-container Docker applications. With Compose, you use a YAML file to configure your application’s services. Then, with a single command, you create and start all the services from your configuration. Compose works in all environments: production, staging, development, testing, as well as CI workflows.

## Build and Push the docker image to GitHub Container Registry
- Use the `Dockerfile` to create the docker image of the main scala play project
- `Dockerfile` uses the open jdk version 8 as the base image and install sbt tool to run the commands for clean, test and run once the image is run in the machine
- `.dockerignore` file is used to ignore all the targets, git related and documentations files
- Rest of the files are copied over the image directory that can build and run the project
- `.github/workflows/scala.yml` file runs when the new code is merged to the master branch of this repository 
- When any code is merged to the master, first it will do format checking and then runs the sbt commands for clean, compile and test
- When the `inspirational-quote-api` is released, `.github/workflows/pushImage.yml` file will run and it will build and publish the image with the same tag given in release. It will only run when the repo is released and make sure to give the proper tag since the same version tag is used in the docker image. 
- Once the first job to build and compile success, it will start to build the image using the `Dockerfile` file and push to the GitHub Container Registry using the [build-push-action](https://github.com/docker/build-push-action/tree/releases/v1) action.
- You can find these packages under the Packages section of this repository: https://github.com/krishna-thapa/inspirational-quote-api
- You can configure the image name, and the tags while building a new image. You have to provide your own personal access token (PAT) that should be stored in github secrets, and it should have at least access for read, write, or delete package scope. [More information](https://docs.github.com/en/packages/guides/migrating-to-github-container-registry-for-docker-images)
- **Note**: Make sure that you are using GitHub Container Registry, not the GitHub Packages Docker registry. The GitHub Container Registry supersedes the existing Packages Docker registry and is optimized to support some unique needs of containers. The registry url should be `ghcr.io/OWNER/IMAGE_NAME` not `docker.pkg.github.com/OWNER/REPOSITORY/IMAGE_NAME`. 
- **Note**: Package versions of a public repository cannot be deleted by the maintainer if you are using GitHub Packages Docker registry and also it cannot be pulled anonymously even if it is present in public repository, have to log in first. So highly recommend using GitHub Container Registry. I had to configure the repository as a private so that I can delete the packages and then converted back to public, see more [details here](https://stackoverflow.com/questions/59103177/how-to-delete-remove-unlink-unversion-a-package-from-the-github-package-registry/59105581#59105581).  

#### Future improvements
- Upgrade newer version of [build-push-action](https://github.com/marketplace/actions/build-and-push-docker-images)
- ~~Make a separate action file to build and publish the image when the repo is release with the version tags, instead of building and pushing everytime there is a new merge in the master branch.~~

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

## Docker file

### Run all the databases using docker container
1. Postgres container with the volume for persistent the data
2. CSV data is copied to the mounted volume that is read by the Postgres container volume and copy the fields to the postgres table while the docker is loaded 
3. Redis container with the volume for persistent the data
4. ElasticSearch container with the volume for persistent the data

### Create a docker container for the main web project
1. Create a Dockerfile for the main Scala Play project that creates a docker image 
2. Image is build and push to the docker hub account
3. Update the docker-compose file with the Scala-play web app image that runs in the correct port and has all the dependencies in other database container
4. To make sure all the docker containers are connected, have to update the host names of each database in the main play project. So instead of using localhost, have to implement the docker service name. In this project, these names are should be updated in the application.conf file under the conf folder.
5. All the unnecessary files and storage data are removed while building an image using `.dockerignore`.
6. All the environment variables used in the docker-compose file can be moved to the Dockerfile
7. COPY method to upload the CSV file data to the Postgres table    

### Options regarding uploading CSV file
1. We can upload the CSV file directly to the image while building using the docker build command. It can only be copied before the image is build. Which means that we don't have to upload the CSV file to the host machine and while the image is run, it can copy the CSV file from the local container folder to the postgres table. Drawback would be the size of the image would be large and can slow when uploading and processing.
2. We can simply copy the CSV files to the host machine where the docker container is running. It will be a different compare to linux and mac os. Basically we need to mount the docker's volume to the host directory where we can copy the CSV file, and the docker will read and mount in its local volume. Then it can be read and uploaded to the postgres table.  
   
### Pass an environment variables in docker
1. Can pass an environment variable from a host to the container 
2. Can pass an environment variable from a container to the host   
3. Can pass an environment variable from one container to the another

In this project, I am creating a .env file that will hold all the defined environment variables. These variables should be declared in the root level where the docker-compose file is present. These variables should be initialized in the server or in cloud server. The docker-compose file will read the variables and each variable that is declared in the docker service and read the variable without passing through Dockerfile. 

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