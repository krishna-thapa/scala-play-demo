--- !Ups

CREATE SEQUENCE serial_csvid START 101;

CREATE TABLE quotations (
    id serial,
    csvid text NOT NULL PRIMARY KEY UNIQUE DEFAULT 'CSV'||nextval('serial_csvid'::regclass)::text,
    quote varchar(1000) NOT NULL,
    author varchar(255) NOT NULL,
    genre varchar(100)
);

-- Have to be Absolute Path for the csv file
COPY quotations(quote,author,genre) FROM '/Users/santosh/gitHome/Inspirational-quote-api/data/Quotes-test.csv' DELIMITER ';;' CSV HEADER;

--- !Downs

DROP SEQUENCE if exists serial_csvid CASCADE;
DROP TABLE if exists quotations CASCADE;
