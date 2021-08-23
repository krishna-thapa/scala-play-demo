#!/bin/bash

# Source the environment variables on the system
source .env
source ./scripts/common.sh

# Set up addresses for everything, allowing them to be overridden
# TODO: Have to try this script while the project is running in the docker container(change the host for postgres)

export HOST=${POSTGRES_HOST:-localhost}
export DB=${POSTGRES_DB:-inspiration_db}
export USER=${POSTGRES_USER:-admin}
export PASSWORD=${POSTGRES_PASSWORD:-admin}
export PORT=${POSTGRES_PORT:-5432}
export URL=${PROJECT_URL:-http://localhost:9000}

export TABLE=${QUOTES_TABLE:-quotes}
export FILE="data/Quotes-test.csv"

echo "Executing the script to migrate '${FILE}' to the postgres table: ${TABLE} in database: ${DB} running in docker container!!"

echo "Checking if your system has postgres installed.."
echo ""

psql=$(which psql)
if [ -z "$psql" ]; then
  echo "psql is not found, please visit for ubuntu here: https://www.postgresql.org/download/linux/ubuntu/"
  echo "psql is not found, please visit for mac here: https://www.postgresql.org/download/macosx/"
  die ""
fi

curl=$(which curl)
if [ -z "curl" ]; then
  die "Please install curl in your machine!"
fi

echo "Checking if the website is up and running"

status="$(curl -Is ${URL}/ping | head -1)"
validate=( $status )
if [ ${validate[-2]} == "200" ]; then
  echo "Postgres is running at host ${HOST}"
  echo "Project is up and running with status: OK"
  echo "Restarting the sequence for CSVID field and truncating the table"
  echo "Enter the password for the admin role, which is: ${PASSWORD}"
  echo ""
  psql -h ${HOST} -p ${PORT} -U ${USER} -d ${DB} -c "ALTER SEQUENCE serial_csv_id RESTART WITH 101;" -c "TRUNCATE ${TABLE} RESTART IDENTITY;" -c "\COPY ${TABLE}(quote,author,genre) from '${FILE}' DELIMITER ';' CSV HEADER;"
else
  die "Project is NOT RESPONDING, check by going to ${URL}"
fi
