## [The Play cache API](https://www.playframework.com/documentation/2.8.x/ScalaCache)

**Play provides the global cache.**

Play provides a CacheApi implementation based on Caffeine for in-processing and have third-party plugins for distributed caching:
- [Redis](https://github.com/KarelCemus/play-redis)

## [Getting started with play-redis](https://github.com/KarelCemus/play-redis-samples/tree/master/hello_world)

First, import the play-redis library.
```
libraryDependencies ++= Seq(
  // play framework cache API
  "com.github.karelcemus" %% "play-redis" % version.value,
  // runtime DI
  PlayImport.guice,
  // runtime DI
  PlayImport.cacheApi
)
```

enable the RedisCacheModule. See `application.conf`
```
play.modules.enabled += play.api.cache.redis.RedisCacheModule
```


## Install Redis and start the server
- [Install Redis on Mac](https://blog.usejournal.com/how-to-install-redis-on-catalina-mac-os-5dc173b08bf6)
- [Install Redis on Ubuntu](https://www.digitalocean.com/community/tutorials/how-to-install-and-secure-redis-on-ubuntu-18-04)
- [Introduction to Redis](https://auth0.com/blog/introduction-to-redis-install-cli-commands-and-data-types/)
- [Redis commands](https://redis.io/commands)

## Use of the Redis as a play cache 
- [Use of library module](https://github.com/KarelCemus/play-redis/blob/2.6.1/doc/30-how-to-use.md)

## Redis commands
```
/etc/init.d/redis-server stop
/etc/init.d/redis-server start
/etc/init.d/redis-server restart

brew services start redis 
redis-cli  
KEYS *  //Get all the lists
DEL <list/set/map name>
LRANGE cache-random-quote 0 -1
LRANGE cache-quoteOfTheDay 0 -1
```

## Implementation as a cache storage
- To resolve not to get a random record that has been called like within last 500 calls
- Initial plan: Use of stack to store and remove the old one once the new are added and give size. Since redis doesn't provide the stack as the data type, went for looking either list or set type.
- Added a simple redis storage as a list collection that stores csvId of the record every time the endpoint is called 
- Has if else statement to check the size and if it reaches the max size then it will delete the first added record in the index 0 and will add the new recordId in the last index
- Was planning to use the Set collection instead of list but Set doesn't have delete with index functionality
- List will store the duplicate ids, and it [doesn't provide the contains boolean method like Set does](https://stackoverflow.com/questions/9312838/checking-if-a-value-exists-in-a-list-already-redis/25368572)
- Decided to use list since it has sorted order of the ids while storing them, so I can delete the first index by giving 0 as index number. To filter the ids I can convert the redis list to Scala list and use contains method on top of it. 
- If the record is already on the list, either recursive the controller method or redirect the api call with the same call
- Use the [Play Redirect routes](https://stackoverflow.com/questions/55289199/the-generated-route-files-of-play-framework-are-re-generated-automatically-even) to call the same controller method if the record is already on the list

## Further improvements on
- Look into how it can be stored and how to check the contains in efficient manner 
- Time limit and speed and where to store the codes
