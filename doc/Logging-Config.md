## Initial Aim
- Use of proper logs system throughout the project
- Logging is a very simple idea of writing to some storage events, data or anything about what happens during your application’s execution.
- Each log message is made of, at the very least: timestamp, level and the message itself.

### use of logs in Play 
- Play provides an API for logging which is accessed through the `Logger` object and uses `Logback` as the default logging engine.
- The Logger API is based on `SLF4J`, and so Logger is based on the `org.slf4j.Logger` interface.
- All loggers inherit from a root logger. Logger inheritance allows you to configure a set of loggers by configuring a common ancestor.
- Log levels are used to classify the severity of log messages.
- [Log levels](https://www.playframework.com/documentation/2.8.x/ScalaLogging#Log-levels)

### Configuration in this project
- As we are using logback we need to provide a config file for it, which is stored under `/conf/logback.xml`
- Logger is set to level `INFO` will log any request of level `INFO` or higher `(INFO, WARN, ERROR)` but will ignore requests of lower severities `(DEBUG, TRACE)`.
- Appender is the class, that will actually write your logs somewhere. Here we are making appender for console logs and for a file appender logs that will save all the generated logs under file `/logs`.
- Part of the appender configuration is encoder. Encoders convert whatever you passed to the logger into OutputStream, which is what actually gets written by the appender.
- The most relevant property of the encoder is a pattern.

## Todo and fix
- Redirect all the logs coming from Docker compose up to the new log files under the same folder. [Stackoverflow](https://stackoverflow.com/questions/35414495/save-docker-compose-logs-to-a-file)
- ~~Log file is not generated as configured in the logback config file. There is a folder under the project root, but it won't produce any file while the app is running.~~ It was hidden by Intellij that have to fixed in preferences. 
- Since the project will be running from the docker image, not sure where will the logs file will be saved in the server machine. I think instead of saving in the project root directory, can be saved under: `/var/lib`. Might have to research where is the best place to save the log files.
- Might have to research for the third party services that provides the logging UI build-in platforms like [finagle](https://twitter.github.io/finagle/).

## Resources
- [Introduction to logging in Scala](https://engineering.footballradar.com/introduction-to-logging-in-scala/)
- [The Logging API in Play](https://www.playframework.com/documentation/2.8.x/ScalaLogging#Log-levels)
