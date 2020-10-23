package search.form

import play.api.data.Forms._
import play.api.data.Form

object SearchForm {

  val searchRequestForm: Form[SearchRequest] = Form {
    mapping(
      "text"   -> nonEmptyText(maxLength = 50),
      "offset" -> default(number, 0),
      "limit"  -> number(min = 1, max = 10)
    )(SearchRequest.apply)(SearchRequest.unapply)
  }
}
