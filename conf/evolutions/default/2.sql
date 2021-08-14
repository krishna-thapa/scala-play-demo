--- !Ups

CREATE SEQUENCE serial_csv_id START 101;

CREATE TABLE quotes (
    id serial,
    csv_id text NOT NULL PRIMARY KEY UNIQUE DEFAULT 'CSV'||nextval('serial_csv_id'::regclass)::text,
    quote varchar(1000) NOT NULL,
    author varchar(255),
    genre varchar(100)
);

CREATE TABLE fav_quotations (
    id serial PRIMARY KEY,
    user_id INTEGER NOT NULL,
    csv_id text NOT NULL,
    fav_tag boolean default true
);

--- !Downs

DROP SEQUENCE if exists serial_csv_id CASCADE;
DROP TABLE if exists quotes CASCADE;
DROP TABLE if exists fav_quotations CASCADE;
