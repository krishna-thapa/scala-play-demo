# Pull base image
FROM openjdk:8

MAINTAINER krishna THapa <krishna.thapa91@gmail.com>

# Env variables
ARG SBT_VERSION=1.3.13

# Env variables for postgres coming from docker-compose file
ARG POSTGRES_DB
ARG POSTGRES_USER
ARG POSTGRES_PASSWORD
ENV POSTGRES_DB $POSTGRES_DB
ENV POSTGRES_USER $POSTGRES_USER
ENV POSTGRES_PASSWORD $POSTGRES_PASSWORD

#give ARG PLAY_ENV a default value = dev or production
ARG PROJECT_ENV
ENV PROJECT_ENV $PROJECT_ENV

# Install curl
RUN \
  apt-get update && \
  apt-get -y install curl

# Install sbt
RUN \
  curl -L -o sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get update && \
  apt-get install sbt

# Define working directory and copy all the projects to the container
WORKDIR /inspirational-quote-api
ADD . /inspirational-quote-api

# Run the sbt commands once the docker is run
CMD sbt clean compile run