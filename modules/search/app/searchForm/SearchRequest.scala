package searchForm

case class SearchRequest(
    text: String,
    offset: Int = 0,
    limit: Int = 5
)
