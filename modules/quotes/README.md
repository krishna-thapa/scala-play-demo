## Quote of the day

This module should present all the api end-points for the quotes service. It should use the Postgres as a database storage and Redis cache storage that should both be running on the docker. Docker container should have local driver volumes that will persistent the data for both the storage. 

First the data has to be loaded in the Postgres database from the csv file. CSV file represents the main source for the quotes' data that has to be loaded in the database. This CSV file has to be copy in the mounted volume path so that docker can have access to the file. Once it has been transferred to the mounted volume folder, we can simply run the Postgres COPY method to load the CSV content into the tables through database migration using Play evolution.(Steps are in doc/Docker.md file)

### Docker configuration
```
POSTGRES_DB: inspiration_db
POSTGRES_USER: admin
POSTGRES_PASSWORD: admin
```
These environment variables are passed to docker container that will create a database with named: `inspiration_db` amd crete a user `admin` with password of `admin`. Later in the future, these environment variables has to be passed from main application docker container. 

### API end-points for this service
- GET:  quote/random            -> Get a random quote from a quotes table
- GET:  quote/quoteOfTheDay     -> Get a quote of the day from quotes table
- GET:  quote/quotesOfTheDay    -> Get last 5 quote of the day
- GET:  quote/randomTen         -> Get a random 10 quotes from a quotes table
- GET:  quote/allQuotes         -> Get all the quotes from quotes table
- POST: quote/fav/:csvId        -> add a favorite quote in the fav_quotes table
- GET:  quote/favQuotes         -> Get all the quotes that are marked as favorite in the fav_quotes table
- GET:  quote/:genre            -> Get a genre selected random quote from a quotes table

### Use of Auth module
Auth service is used to add authorization layer for the three endpoints from this service and beside those three, anyone can call the api.
- GET:  quote/allQuotes -> Only user with an admin role can call this api since it will retrieve all the quotes from database
- POST: quote/fav/:csvId -> Only the logged in user can perform this call since it will call the method that will add the quote csv id into the fav_quotes table with the user id. Each user will have their own favorites quites in the database.
- GET:  quote/favQuotes -> Each logged in user can view their favorite tagged quotes. 

### Use of Redis Cache 
Have implemented [play cache](https://www.playframework.com/documentation/2.8.x/ScalaCache) and [redis cache library](https://github.com/KarelCemus/play-redis) for this service to store the quotes csv id in the memory caching. Play provides a CacheApi implementation based on Caffeine for in-processing and have third-party plugins for distributed caching. (See more info under /doc/Redis.md)

**Play provides the global cache.**

## Implementation as a cache storage
- To resolve not to get a random record which has been called like within last 500 calls
- Initial plan: Use of stack to store and remove the old one once the new are added and give size. Since redis doesn't provide the stack as the data type, went for looking either list or set type.
- Added a simple redis storage as a list collection that stores csvId of the record every time the endpoint is called 
- Has if else statement to check the size and if it reaches the max size then it will delete the first added record in the index 0 and will add the new recordId in the last index
- Was planning to use the Set collection instead of list but Set doesn't have delete with index functionality
- List will store the duplicate ids, and it [doesn't provide the contains boolean method like Set does](https://stackoverflow.com/questions/9312838/checking-if-a-value-exists-in-a-list-already-redis/25368572)
- Decided to use list since it has sorted order of the ids while storing them, so I can delete the first index by giving 0 as index number. To filter the ids I can convert the redis list to Scala list and use contains method on top of it. 
- If the record is already on the list, either recursive the controller method or redirect the api call with the same call
- Use the [Play Redirect routes](https://stackoverflow.com/questions/55289199/the-generated-route-files-of-play-framework-are-re-generated-automatically-even) to call the same controller method if the record is already on the list

1. GET:  quote/random 
- 


### Requirements:
- New API endpoint that will response the random quote with date (current date without time)
- Should be able to take date as a path parameter 
- Should store past five days quote in the cache and should be able to retrieve using the same api with date as a path parameters
- Quotes should be stored in the cache storage for up to one year(365 records) so that a quote of the day shouldn't be show up again for one whole year

### Solutions implemented:
- `/quoteOfTheDay?date=<date-in-milliseconds>` - Takes the date as a path parameter which is an optional. First it will convert the milliseconds date in the date only format. If the date is not present or invalid path date, then it will output the current date.
- It will try to search the cache key-value record where key matches to the path date.
- Cache stores the past 5 days of quote of the day in key-value pair, date being the string key and quote object as a string in the value.
 
 
 
## Further improvements on
- Look into how it can be stored and how to check the contains in efficient manner 
- Time limit and speed and where to store the codes