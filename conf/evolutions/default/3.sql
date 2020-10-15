--- !Ups

CREATE TABLE user_details_table (
    id serial PRIMARY KEY,
    first_name varchar(255) NOT NULL,
    last_name varchar(255) NOT NULL,
    email varchar(255) NOT NULL,
    password varchar(255) NOT NULL,
    created_date date NOT NULL,
    is_admin boolean default true
);

--- !Downs

DROP TABLE if exists user_details_table CASCADE;