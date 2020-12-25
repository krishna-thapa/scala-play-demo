--- !Ups

CREATE TABLE custom_quotations (
    id serial PRIMARY KEY,
    quote varchar(1000) NOT NULL,
    author varchar(255) NOT NULL,
    genre varchar(100),
    stored_date date NOT NULL,
    own_quote boolean default true
);

CREATE TABLE fav_quotations (
    id serial PRIMARY KEY,
    user_id INTEGER NOT NULL,
    csv_id text NOT NULL,
    fav_tag boolean default true
);

--- !Downs

DROP TABLE if exists custom_quotations CASCADE;
DROP TABLE if exists fav_quotations CASCADE;