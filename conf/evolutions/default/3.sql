--- !Ups

CREATE TABLE custom_quotations (
    id serial PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES user_details_table ON DELETE CASCADE,
    quote VARCHAR(1000) NOT NULL,
    author VARCHAR(255),
    genre VARCHAR(100),
    stored_date DATE NOT NULL,
    own_quote boolean default true,
    FOREIGN KEY(user_id) REFERENCES user_details_table (id)
);

--- !Downs

DROP TABLE if exists custom_quotations CASCADE;
