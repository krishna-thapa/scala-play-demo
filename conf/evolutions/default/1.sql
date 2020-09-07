--- !Ups

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

--- !Downs

DROP TABLE if exists custom_quotations CASCADE;
DROP TABLE if exists fav_quotations CASCADE;