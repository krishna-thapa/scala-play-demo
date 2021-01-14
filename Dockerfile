# Pull base image
FROM openjdk:8

MAINTAINER krishna Thapa <krishna.thapa91@gmail.com>

# Env variables
ARG SBT_VERSION=1.3.13

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