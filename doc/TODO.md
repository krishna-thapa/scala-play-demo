## TODO in coming days

- [x] Add MonogDb to update user picture
- [ ] Update the Slickpg for use of array and text search: https://github.com/tminglei/slick-pg and also for the use of model pg_trgm
- [ ] Fix the cron job for daily call for the quote of the day API
- [x] Create a dependency injection for the mocked WIKI API
- [ ] Make use of Play WebSocket and create a simple chat app in the addition of the main app of Quotes or anything else

### Update the Elastic search of the quotes with auto suggestion and completion

- [x] https://www.elastic.co/guide/en/elasticsearch/reference/7.12/search-suggesters.html#completion-suggester
- [x] https://dev.to/ankitutekar/series/12283
- [x] Use of CompletionSuggestion/PhraseSuggestion from elastic4s to do advance

### Update the Akka Stream with Alpakka

- [x] https://doc.akka.io/docs/alpakka/current/data-transformations/csv.html

### Store CSV file in public online cloud storage
- S3
- DropBox
- Google Drive

### Add sbt dependency check 