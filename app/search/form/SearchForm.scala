package search.form

import play.api.data.Forms._
import play.api.data.Form

object SearchForm {

  val searchRequestForm: Form[SearchRequest] = Form {
    mapping(
      "text"   -> nonEmptyText(minLength = 3, maxLength = 50),
      "offset" -> number(min = 0, max = 10),
      "limit"  -> default(number, 10)
    )(SearchRequest.apply)(SearchRequest.unapply)
  }
}
