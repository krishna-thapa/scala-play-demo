## [The Play cache API](https://www.playframework.com/documentation/2.8.x/ScalaCache)

Play provides the global cache.

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



