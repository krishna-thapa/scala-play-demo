## Quote of the day

### Requirements:
- New API endpoint that will response the random quote with date (current date without time)
- Should be able to take date as a path parameter 
- Should store past five days quote in the cache and should be able to retrieve using the same api with date as a path parameters
- Quotes should be stored in the cache storage for up to one year(365 records) so that a quote of the day shouldn't be show up again for one whole year

### Solutions implemented:
- `/quoteOfTheDay?date=<date-in-milliseconds>` - Takes the date as a path parameter which is an optional. First it will convert the milliseconds date in the date only format. If the date is not present or invalid path date, then it will output the current date.
- It will try to search the cache key-value record where key matches to the path date.
- Cache stores the past 5 days of quote of the day in key-value pair, date being the string key and quote object as a string in the value.
 