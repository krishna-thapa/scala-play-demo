#!/bin/bash

# Source the common script file that will have common functions
source ./scripts/common.sh

# Source the environment variables
source .env

google=$(which google-chrome)
if [ -z "google" ]; then
  die "Please install google-chrome in your system or use your default browser-name!"
fi

# Run sbt command to start up the test with scoverage
sbt clean coverage test coverageAggregate

# Open the html document in google chrome
google-chrome ./target/scala-2.13/scoverage-report/index.html
