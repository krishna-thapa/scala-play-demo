## Docker and Docker compose

Docker is an open-source containerization platform that allows you to quickly build, test, and deploy applications as 
portable containers that can run virtually anywhere. A container represents a runtime for a single application and 
includes everything the software needs to run.

Docker is an integral part of modern software development and DevOps continuous integration and deployment pipelines.

Compose is a tool for defining and running multi-container Docker applications.
With Compose, you use a YAML file to configure your application’s services.
Then, with a single command, you create and start all the services from your configuration.
Compose works in all environments: production, staging, development, testing, as well as CI workflows.

## Build and run the project using containers

- Install a docker and docker-compose in the machine
- Create a directory anywhere to hold all the config and docker-compose file
- Copy the `docker-compose.yml` file in the directory
- Copy the `.env` file to the same directory
- Copy `data/Quotes.csv` file to the respective mounted volume directory for the postgres container's volume
  - for macOS: copy the CSV file to the same directory under the new folder `/pgdata`
  - for Ubuntu: copy the CSV file to the `/var/lib/postgresql/data/`, create the path if it doesn't exit
- Run the command: `docker-compose up`


## Build and Push the docker image to GitHub Container Registry

- Use the `Dockerfile` to create the docker image of the main scala play project
- `Dockerfile` uses the open jdk version 11 as the base image and install sbt tool to run the commands for clean, test and run once the image is run in the machine
- `.dockerignore` file is used to ignore all the targets, git related and documentations files
- Rest of the files are copied over the image directory that will be build and run the project
- `.github/workflows/scala.yml` file runs when the new code is merged to the master branch of this repository
- `.github/workflows/dependencyCheck.yml` file runs a script that will use [sbt-dependency-check](https://github.com/albuch/sbt-dependency-check) to check the vulnerabilities and print them in console outputs. It can be configured to run using boolean parameter so that it won't run on every time the branch is merged to master.
- When any code is merged to the master, first it will do format checking and then runs the sbt commands for clean, compile and test
- When the `inspirational-quote-api` is released, `.github/workflows/pushImage.yml` file will run and it will build and publish the image with the same tag given in release. It will only run when the repo is released and make sure to give the proper tag since the same version tag is used in the docker image.
- Once the first job to build and compile success, it will start to build the image using the `Dockerfile` file and push to the GitHub Container Registry using the [build-push-action](https://github.com/docker/build-push-action/tree/releases/v1) action.
- You can find these packages under the Packages section of this repository: https://github.com/krishna-thapa/inspirational-quote-api
- You can configure the image name, and the tags while building a new image. You have to provide your own personal access token (PAT) that should be stored in github secrets, and it should have at least access for read, write, or delete package scope. [More information](https://docs.github.com/en/packages/guides/migrating-to-github-container-registry-for-docker-images)
- **Note**: Make sure that you are using GitHub Container Registry, not the GitHub Packages Docker registry. The GitHub Container Registry supersedes the existing Packages Docker registry and is optimized to support some unique needs of containers. The registry url should be `ghcr.io/OWNER/IMAGE_NAME` not `docker.pkg.github.com/OWNER/REPOSITORY/IMAGE_NAME`.
- **Note**: Package versions of a public repository cannot be deleted by the maintainer if you are using GitHub Packages Docker registry and also it cannot be pulled anonymously even if it is present in public repository, have to log in first. So highly recommend using GitHub Container Registry. I had to configure the repository as a private so that I can delete the packages and then converted back to public, see more [details here](https://stackoverflow.com/questions/59103177/how-to-delete-remove-unlink-unversion-a-package-from-the-github-package-registry/59105581#59105581).

#### Future improvements
- [ ] Vulnerabilities that is comes out from `.github/workflows/dependencyCheck.yml` can be sent over to email or to any online logging platform or group channel like in Slack or Discord
- [x] Upgrade newer version of [build-push-action](https://github.com/marketplace/actions/build-and-push-docker-images)
- [x] Make a separate action file to build and publish the image when the repo is release with the version tags, instead of building and pushing everytime there is a new merge in the master branch.

## Docker file

### Run all the databases using docker container
1. Postgres container with the volume for persistent the data
2. CSV data is copied to the mounted volume that is read by the Postgres container volume and copy the fields to the postgres table while the docker is loaded
3. Redis container with the volume for persistent the data
4. ElasticSearch container with the volume for persistent the data
5. Kibana and Filebeat ?

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

In this project, I am creating a .env file that will hold all the defined environment variables. 
These variables should be declared in the root level where the docker-compose file is present. 
These variables should be initialized in the server or in cloud server. 
The docker-compose file will read the variables and each variable that is declared in the docker service and 
read the variable without passing through Dockerfile. 
