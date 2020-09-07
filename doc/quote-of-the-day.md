## Quote of the day

### Requirements:
- New API endpoint that will response the random quote with date (current date without time)
- Should be able to take date as a path parameter 
- Should store past five days quote in the cache and should be able to retrieve using the same api with date as a path parameters
- Quotes should be stored in the cache storage for up to one year(365 records) so that a quote of the day shouldn't be show up again for one whole year
