CREATE SEQUENCE serial_csv_id START 101;

CREATE TABLE quotes (
    id serial,
    csv_id text NOT NULL PRIMARY KEY UNIQUE DEFAULT 'CSV'||nextval('serial_csv_id'::regclass)::text,
    quote varchar(1000) NOT NULL,
    author varchar(255) NOT NULL,
    genre varchar(100)
);