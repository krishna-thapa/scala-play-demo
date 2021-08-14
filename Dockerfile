# Pull base image
FROM openjdk:8

MAINTAINER krishna Thapa <krishna.thapa91@gmail.com>

# Setup adapted from https://github.com/hseeberger/scala-sbt/blob/master/debian/Dockerfile
RUN \
  apt-get update -q && \
  apt-get upgrade -qq && \
  apt-get install -y git && \
  rm -rf /var/lib/apt/lists/*

# Any RUN command after an ARG is set has that value in it as an environment variable and thus
# invalidates layer cache, so only declaring these ARGs when they're used

ARG SBT_VERSION=1.3.13

# Install sbt
RUN \
  curl -L "https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.tgz" | tar zxf - -C /usr/share  && \
  cd /usr/share/sbt/bin && \
  rm sbt.bat sbtn-x86_64-apple-darwin sbtn-x86_64-pc-linux sbtn-x86_64-pc-win32.exe && \
  ln -s /usr/share/sbt/bin/sbt /usr/local/bin/sbt

# Define working directory and copy all the projects to the container
WORKDIR /inspirational-quote-api
ADD . /inspirational-quote-api

# Run the sbt commands once the docker is run
CMD sbt clean compile run