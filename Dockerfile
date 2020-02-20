FROM openjdk:8

MAINTAINER krishna THapa <krishna.thapa91@gmail.com>

WORKDIR /opt/inspirational-quote-api

ADD . /opt/inspirational-quote-api

# This Dockerfile has two required ARGs to determine which base image
# to use for the JDK and which sbt version to install.

ARG SBT_VERSION=1.3.5

# Install sbt
RUN \
  curl -L -o sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get update && \
  apt-get install sbt && \
  sbt sbtVersion

EXPOSE 9000

CMD sbt clean compile run