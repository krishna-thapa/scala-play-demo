\c inspiration_db

DROP TABLE if exists quotations;
DROP TABLE if exists custom_quotations;

CREATE TABLE quotations (
    id serial PRIMARY KEY,
    quote varchar(1000) NOT NULL,
    author varchar(255) NOT NULL,
    genre varchar(100)
);

CREATE TABLE custom_quotations (
    id serial PRIMARY KEY,
    quote varchar(1000) NOT NULL,
    author varchar(255) NOT NULL,
    genre varchar(100),
    storedDate date NOT NULL,
    ownQuote boolean default true
);

-- Have to be full path for the csv file
COPY quotations(quote,author,genre) FROM '/Users/santosh/gitHome/scala-play-demo/data/Quotes.csv' DELIMITER ';' CSV HEADER;


-- Sample quotes data
--INSERT INTO quotations (quote, author, genre) VALUES ('Make your life a masterpiece, imagine no limitations on what you can be, have or do.', 'Brian Tracy', 'age');
--INSERT INTO quotations (quote, author, genre) VALUES ('We may encounter many defeats but we must not be defeated.', 'Maya Angelou', '');
--INSERT INTO quotations (quote, author, genre) VALUES ('Stephen Covey', 'I am not a product of my circumstances. I am a product of my decisions.');
--INSERT INTO quotations (quote, author, genre) VALUES ('Joseph Campbell', 'We must let go of the life we have planned, so as to accept the one that is waiting for us.');
--INSERT INTO quotations (quote, author, genre) VALUES ('Theodore Roosevelt', 'Believe you can and you''re halfway there.');
--INSERT INTO quotations (quote, author, genre) VALUES ('William Shakespeare', 'We know what we are, but know not what we may be.');
--INSERT INTO quotations (quote, author, genre) VALUES ('Ronald Reagan', 'We can''t help everyone, but everyone can help someone.');
--INSERT INTO quotations (quote, author, genre) VALUES ('Carol Burnett', 'When you have a dream, you''ve got to grab it an never let go.');
--INSERT INTO quotations (quote, author, genre) VALUES ('Nido Quebein', 'Your present circumstances don''t determine where you can go; they merely determine where you start.');
--INSERT INTO quotations (quote, author, genre) VALUES ('Plato', 'Thinking: the talking of the soul with itself.');