## The Play cache API
- [Play Cache](https://www.playframework.com/documentation/2.8.x/ScalaCache)
- [Getting started with play-redis](https://github.com/KarelCemus/play-redis-samples/tree/master/hello_world)

## Use of Play-redis library
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
- [Install Redis on Mac](https://gist.github.com/tomysmile/1b8a321e7c58499ef9f9441b2faa0aa8)
- [Install Redis on Ubuntu](https://www.digitalocean.com/community/tutorials/how-to-install-and-secure-redis-on-ubuntu-18-04)
- [Introduction to Redis](https://auth0.com/blog/introduction-to-redis-install-cli-commands-and-data-types/)
- [Redis commands](https://redis.io/commands)

## Use of the Redis as a play cache 
- [Use of library module](https://github.com/KarelCemus/play-redis/blob/2.6.1/doc/30-how-to-use.md)

## Redis commands
```
sudo systemctl disable redis-server     // Don't launch at boot.
sudo systemctl enable redis-server      // Launch at boot.
sudo systemctl start redis-server       // Launch it now.
sudo systemctl stop redis-server        // Stop it now.
sudo systemctl status redis-server      // To check if is running

/etc/init.d/redis-server stop
/etc/init.d/redis-server start
/etc/init.d/redis-server restart

brew services start redis 
redis-cli  

KEYS *  //Get all the lists

DEL cache-random-quote
DEL cache-quoteOfTheDay

LRANGE cache-random-quote 0 -1
LRANGE cache-quoteOfTheDay 0 -1
```
