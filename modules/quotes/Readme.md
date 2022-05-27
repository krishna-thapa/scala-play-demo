# Quote of the day

This module should present all the api end-points for the quotes service. It should use the Postgres as a database storage and Redis cache storage that should both be running on the docker. Docker container should have local driver volumes that will persistent the data for both the storage.

First the data has to be loaded in the Postgres database from the csv file. CSV file represents the main source for the quotes' data that has to be loaded in the database. This CSV file has to be copied in the mounted volume path so that docker can have access to the file. Once it has been transferred to the mounted volume folder, we can simply run the Postgres COPY method to load the CSV content into the tables through database migration using Play evolution.(Steps are in doc/Docker.md file)

### Docker configuration

```
POSTGRES_DB: inspiration_db
POSTGRES_USER: admin
POSTGRES_PASSWORD: admin
```

These environment variables are passed to docker container that will create a database with named: `inspiration_db` and crete a user `admin` with password of `admin`.

All the environment variables are declared in the `.env` file that is present under the root folder of the project. These environment variables has to be passed from main application docker container using the docker-compose file. When the docker-compose file is loaded, it will read all the environment variables from `.env` file and pass those variables to the respective docker services, that can be read in the project.

### Use of Auth module

Auth service is used to add authorization layer for the following endpoints from this service and beside following three, anyone can call the api.

- GET:  quote/allQuotes -> Only user with an admin role can call this api since it will retrieve all the quotes from database
- GET:  quote/quotesOfTheDay -> Either a logged in user or normal user that has no account can call this API endpoint. Depending on the logged in info, this api endpoint will return different response.
- POST: quote/fav/:csvId -> Only the logged-in user can perform this call since it will call the method that will add the quote csv id into the fav_quotes table with the user id. Each user will have their own favorites quites in the database.
- GET:  quote/favQuotes -> Each logged-in user can view their favorite tagged quotes.

### Use of Redis Cache

Have implemented [play cache](https://www.playframework.com/documentation/2.8.x/ScalaCache) and [redis cache library](https://github.com/KarelCemus/play-redis) for this service to store the quotes csv id in the memory caching. Play provides a CacheApi implementation based on Caffeine for in-processing and have third-party plugins for distributed caching. (See more info under /doc/Redis.md)

**Play provides the global cache.**

## Implementation as a cache storage

- To resolve not to get a random record which has been called like within last 500 calls
- Initial plan: Use of stack to store and remove the old one once the new are added and give size. Since redis doesn't provide the stack as the data type, went for looking either list or set type.
- Added a simple redis storage as a list collection that stores csvId of the record every time the endpoint is called
- Has if else statement to check the size and if it reaches the max size then it will delete the first added record in the index 0 and will add the new recordId in the last index
- Was planning to use the Set collection instead of list but Set doesn't have a delete method with index functionality
- List will store the duplicate ids, and it [doesn't provide the contains boolean method like Set does](https://stackoverflow.com/questions/9312838/checking-if-a-value-exists-in-a-list-already-redis/25368572)
- Decided to use list since it has sorted order of the ids while storing them, so I can delete the first index by giving 0 as index number. To filter the ids I can convert the redis list to Scala list and use contains method on top of it.
- If the record is already on the list, either recursive the controller method or redirect the api call with the same call
- Use the [Play Redirect routes](https://stackoverflow.com/questions/55289199/the-generated-route-files-of-play-framework-are-re-generated-automatically-even) to call the same controller method if the record is already on the list

## API end-points for this service

1. GET:  quote/random

* Get a random quote from a quotes table
* There is no authorization(anyone can call this API endpoint)

2. GET:  quote/quoteOfTheDay

- Get a quote of the day from quotes table
- There is no authorization(anyone can call this API endpoint)
- `/quoteOfTheDay?date=<date-in-milliseconds>` Can take date as a path parameter, it is an optional. Date has to be within the last 5 days, and it will respond the quote of that day. First it will convert the milliseconds date in the date only format. If the date is not present or invalid path date, then it will output the current date.
- When called, first it checks if there is already a quote of the day stored in redis cache with the key of the content date. If there is then it will convert the string to quote JSON and sends to response body.
- If not then it will get the random quote and checks if it has been called before in the redis list with the variable name: `cache-quoteOfTheDay`
- If it has not been called before, it will store the new quote in the cache as a string, and a current date as a key with expiration of max 5 days
- If it has been called, it will call new random quote and compares to list again and again until api endpoint gets the quote that has not been called and updates the redis cache list by adding the id at the last index
- Redis list size can be configured(application.conf), which indicates how long the random quotes has to be proper distinct compare to the previous called quote by the project
- Once the Index reaches to the max defined size, it will delete the first index quote id and add the new random id at the last index entry

3. GET:  quote/quotesOfTheDay

- Get last 5 quote of the day
- This API endpoint can be called either by logged in user or normal user that doesn't have account
- If it is called by the user that doesn't have an account, first it will try to get all the cached records that matches the regex in key(max should be 5) and returns them to the response body so there will be no connection to the database. If the redis cache is empty, it will run the `quote/quoteOfTheDay` endpoint and call the same method again to get the newly cached quote from Redis.
- If the endpoint is called by the logged in user, it will get all the cached quotes from matching regex key and all the favourite quotes from `fav_quotations` table that matches to the user's id and that has boolean `favTag` as true. Then it will update the Redis cached quotes with the new boolean field `isFavQuote` that represents those quotes are tagged as favourite for that user.
- Gets all quotes of the day stored in the Redis cache using patter matching on keys

4. GET:  quote/randomTen

- Get a random 10 quotes from a quotes table
- There is no authorization(anyone can call this API endpoint)

5. GET:  quote/allQuotes

- Get all the quotes from quotes table
- Only the Admin role can call this endpoint

6. POST: quote/fav/:csvId

- Add a favorite quote in the fav_quotes table
- Only th logged-in user can perform this action
- Each record in fav_quotes table has user id and csv id and a boolean parameter to indicate the favourite quote
- Each user can view only the records that has their id

7. GET:  quote/favQuotes

- Get all the quotes that are marked as favorite in the fav_quotes table
- Only th logged-in user can perform this action

8. GET:  quote/:genre

- Get a genre selected random quote from a quotes table

## Use of Play scheduler for cron schedule

## Further improvements on

- Add paginiation using the offset value for getting all the records instead
- Use of Postgres Full-Text search to enable the Search service from Postgres instead of using ElasticSearch
- Make use of extended Slick pg library to add the array of string in terms of genres
- Fix the cron schedule job
- Run evolution sql scripts for test container (find out how the copy can be differed with test and actual project)

## Actor inject dependency

- https://abhsrivastava.github.io/2017/11/03/Actors-With-Guice/
- http://www.icharm.me/use-akka-quartz-scheduler-for-cron-schedule-in-play-framework-2-7.html
- https://www.playframework.com/documentation/2.8.x/ScalaDependencyInjection
- https://stackoverflow.com/questions/33889224/play-2-4-how-to-inject-akka-actors-using-guice

## Test using scala-docker

- [spin redis from docker-it-scala](https://stackoverflow.com/questions/48510658/spin-redis-from-docker-it-scala/49436687)
