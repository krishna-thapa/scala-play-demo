CREATE SEQUENCE serial_csv_id START 101;

CREATE TABLE quotes (
    id serial,
    csv_id text NOT NULL PRIMARY KEY UNIQUE DEFAULT 'CSV'||nextval('serial_csv_id'::regclass)::text,
    quote varchar(1000) NOT NULL,
    author varchar(255) NOT NULL,
    genre varchar(100)
);

INSERT INTO quotes VALUES
    (1, 'CSV101', 'Old age is no place for sissies.', 'Bette Davis', 'age'),
    (2, 'CSV102', 'I want to make sure my family''s straight.', 'Lil Wayne', 'family'),
    (3, 'CSV103', 'The world is starving for original and decisive leadership.', 'Bette McGill', 'leadership');