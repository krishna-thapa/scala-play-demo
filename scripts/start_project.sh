#!/bin/bash

# Source the common script file that will have common functions
source ./scripts/common.sh

# Source the environment variables
source .env

export POSTGRES="pg-docker"
export ES="elasticsearch"
export MONGO="mongodb-quotes"
export MONGO_EXPRESS="mongo-express-quotes"
export KIBANA="kibana"
export FILEBEAT="filebeat"

docker=$(which docker)
if [ -z "$docker" ]; then
  die "Please install docker in your system!"
fi

dockerCompose=$(which docker-compose)
if [ -z "$dockerCompose" ]; then
  die "Please install docker compose in your system!"
fi

sbt=$(which sbt)
if [ -z "sbt" ]; then
  die "Please install sbt in your system!"
fi

# Stop all the containers if they are running
echo "Stopping the containers: ${POSTGRES} ${ES} ${MONGO} ${MONGO_EXPRESS} ${KIBANA} ${FILEBEAT}"
docker stop ${POSTGRES} ${ES} ${MONGO} ${MONGO_EXPRESS} ${KIBANA} ${FILEBEAT}

# Open the new terminal window and run sbt command to start up the project
x-terminal-emulator -e sbt clean compile run

# Start the containers using docker compose file
docker-compose up

