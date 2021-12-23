## Simple CRUD project using Scala with Play framework and Slick ORM to access Postgres Database

## API endpoints 

1. **GET** `/customQuote/all` -> Get all the quotes from a custom build quote table
2. **GET** `/customQuote/random` -> Get a random quote from a custom build quote table
3. **GET** `/customQuote/{id}` -> Get a selected quote from a custom build quote table
4. **DELETE** `/customQuote/{id}` -> Delete a selected quote from a custom build quote table
5. **PUT** `/customQuote/{id}` -> Update a existing custom quote record in the table
6. **POST** `/customQuote` -> Create a new custom quote to the table