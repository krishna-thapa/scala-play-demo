#!/bin/bash

# Source the common script file that will have common functions
source ./scripts/common.sh

# Source the environment variables
source .env

sbt=$(which sbt)
if [ -z "sbt" ]; then
  die "Please install sbt in your system!"
fi


# Run sbt command to start up the project
sbt clean compile run ~
