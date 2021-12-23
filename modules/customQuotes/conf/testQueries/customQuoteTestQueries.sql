CREATE TABLE custom_quotations (
    id serial PRIMARY KEY,
    user_id INTEGER NOT NULL,
    quote varchar(1000) NOT NULL,
    author varchar(255),
    genre varchar(100),
    stored_date date NOT NULL,
    own_quote boolean default true
);
