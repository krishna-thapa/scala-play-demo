CREATE TABLE fav_quotations (
    id serial PRIMARY KEY,
    user_id UUID NOT NULL,
    csv_id text NOT NULL,
    fav_tag boolean default true
);