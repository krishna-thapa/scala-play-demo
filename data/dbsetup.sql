\c inspiration_db

DROP TABLE if exists quotations;
DROP TABLE if exists custom_quotations;
DROP TABLE if exists fav_quotations;
DROP SEQUENCE if exists serial_id;

-- csvid will start from 101 with prefix of csv on it
CREATE SEQUENCE serial_id START 101;

CREATE TABLE quotations (
    id serial,
    csvid text NOT NULL PRIMARY KEY UNIQUE DEFAULT 'CSV'||nextval('serial_id'::regclass)::text,
    quote varchar(1000) NOT NULL,
    author varchar(255) NOT NULL,
    genre varchar(100)
);

CREATE TABLE custom_quotations (
    id serial PRIMARY KEY,
    quote varchar(1000) NOT NULL,
    author varchar(255) NOT NULL,
    genre varchar(100),
    storeddate date NOT NULL,
    ownquote boolean default true
);

CREATE TABLE fav_quotations (
    id serial PRIMARY KEY,
    csvid text NOT NULL,
    favtag boolean default true
);

-- Have to be full path for the csv file
COPY quotations(quote,author,genre) FROM '/Users/santosh/gitHome/Inspirational-quote-api/data/Quotes.csv' DELIMITER ';' CSV HEADER;
