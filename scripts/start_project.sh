#!/bin/bash

# Source the common script file that will have common functions
source ./scripts/common.sh

# Source the environment variables
source .env

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
echo "Stopping all the running containers, make sure that this is okay for your machine"
docker kill $(docker ps -q)

# Open the new terminal window and run sbt command to start up the project
x-terminal-emulator -e sbt clean compile run ~

# Start the containers using docker compose file
docker-compose up

