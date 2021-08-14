#!/bin/bash

HOST="localhost"
PORT=5432
DB="inspiration_db"
TABLE="quotes"
FILE="data/Quotes-test.csv"

echo "Executing the script to migrate ${FILE} to the postgres table ${TABLE} in database ${DB} running in docker container!!"

echo "Checking if your system has postgres installed.."
echo ""

psql=$(which psql)
if [ -z "$psql" ]; then
  echo "psql is not found, please visit for ubuntu here: https://www.postgresql.org/download/linux/ubuntu/"
  echo "psql is not found, please visit for mac here: https://www.postgresql.org/download/macosx/"
fi

echo "Postgres is running at host ${HOST}"
echo "Restarting the sequence for CSVID field and truncating the table"
echo "Use the password defined for the admin role"
echo ""
psql -h ${HOST} -p ${PORT} -U admin -d ${DB} -c "ALTER SEQUENCE serial_csv_id RESTART WITH 101;" -c "TRUNCATE ${TABLE} RESTART IDENTITY;" -c "\COPY ${TABLE}(quote,author,genre) from '${FILE}' DELIMITER ';' CSV HEADER;"