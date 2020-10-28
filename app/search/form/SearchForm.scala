package search.form

import play.api.data.Forms._
import play.api.data.Form

object SearchForm {

  val searchRequestForm: Form[SearchRequest] = Form {
    mapping(
      "text"   -> nonEmptyText(maxLength = 50),
      "offset" -> number(min = 0, max = 10),
      "limit"  -> default(number, 10)
    )(SearchRequest.apply)(SearchRequest.unapply)
  }
}
