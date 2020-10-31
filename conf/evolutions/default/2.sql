--- !Ups

CREATE SEQUENCE serial_csvid START 101;

CREATE TABLE quotations (
    id serial,
    csvid text NOT NULL PRIMARY KEY UNIQUE DEFAULT 'CSV'||nextval('serial_csvid'::regclass)::text,
    quote varchar(1000) NOT NULL,
    author varchar(255) NOT NULL,
    genre varchar(100)
);

-- First have to manually import the csv file to the docker container by mounting the local folder to the docker pgdata path, see docker-compose yml file
-- Copy the CSV that is stored in the docker container where postgres is running
COPY quotations(quote,author,genre) FROM '/var/lib/postgresql/data/pgdata/Quotes-test.csv' DELIMITER ';;' CSV HEADER;

--- !Downs

DROP SEQUENCE if exists serial_csvid CASCADE;
DROP TABLE if exists quotations CASCADE;
