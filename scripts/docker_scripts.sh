#!/bin/bash

docker=$(which docker)
if [ -z "$docker" ]; then
  die "Please install docker in your system!"
fi

dockerCompose=$(which docker-compose)
if [ -z "$dockerCompose" ]; then
  die "Please install docker compose in your system!"
fi

echo "First command to perform: $1"
if [ "$1" = "-k" ]; then
    echo "Deleting all the running docker containers!!"
    docker kill $(docker ps -q)
elif [[ "$1" = "ps" ]]; then
    echo "List all the current running docker containers"
    docker ps -q
elif [[ "$1" = "list" ]]; then
    echo "List few docker commands"
    echo "docker system prune -a"
    echo "docker volume prune"
else
    echo "Not a valid docker command:$1"
fi