\c inspiration_db

DROP TABLE if exists quotations;
DROP TABLE if exists custom_quotations;

CREATE TABLE quotations (
    id serial PRIMARY KEY,
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

-- Have to be full path for the csv file
COPY quotations(quote,author,genre) FROM '/Users/santosh/gitHome/scala-play-demo/data/Quotes.csv' DELIMITER ';' CSV HEADER;
