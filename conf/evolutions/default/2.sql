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
    user_id UUID NOT NULL REFERENCES user_details_table ON DELETE CASCADE,
    csv_id text NOT NULL,
    fav_tag boolean default true,
    FOREIGN KEY(user_id) REFERENCES user_details_table (id)
);

--- !Downs

DROP SEQUENCE if exists serial_csv_id CASCADE;
DROP TABLE if exists quotes CASCADE;
DROP TABLE if exists fav_quotations CASCADE;
