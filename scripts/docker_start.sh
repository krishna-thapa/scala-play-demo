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

echo "List all the current running docker containers"
docker ps -q
echo "If there are errors while running docker compose up, best to kill all running containers using the command:"
echo "docker kill \$(docker ps -q)"

echo "To resolve the Postgres error: :5432: bind: address already in use"
echo "sudo ss -lptn 'sport = :5432'"
echo "sudo kill <pid>"

# Start the containers using docker compose file
docker-compose up

