## TODO in coming days

* [ ]  Complete the refactoring with Future use for all the modules
* [ ]  Update the Slickpg: https://github.com/tminglei/slick-pg
  * [ ]  Use of Postgres Full-Text search to enable the Search service from Postgres instead of using ElasticSearch
  * [ ]  Make use of extended Slick pg library to add the array of string in terms of genres
* [ ]  Fix the cron job for daily call for the quote of the day API
* [ ]  Fix and add more test cases

### New modules to add

* [ ]  Add new module for the load testing using Akka Gatling library
* [ ]  Add the Akka Kafka to add the system for notification:- https://www.linkedin.com/pulse/building-high-performance-notification-engine-using-pure-otun?trk=articles_directory
* [ ]  Make use of Play WebSocket and create a simple chat app in the addition of the main app of Quotes or anything else

## Already completed tasks

- [X]  Add MonogDb to update user picture
- [X]  Create a dependency injection for the mocked WIKI API
- [X]  Add pagination using the offset and limit value for getting all the record
- [X]  Update the Elastic search of the quotes with auto suggestion and completion
- [X]  Update the Akka Stream with Alpakka

## Store CSV file in public online cloud storage

- S3
- DropBox
- Google Drive
