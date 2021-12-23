package forms

import forms.RequestForm.CustomQuoteForm
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.data.Form

class RequestFormSpec extends AnyFlatSpec {

  behavior of "RequestForm"
  it should "accept quote as a string text" in {
    val res: Form[CustomQuoteForm] = RequestForm.quotesQueryForm.bind(Map("quote" -> "Random quote"))
    res.hasErrors shouldBe false
  }

  it should "requires quote filed as non-empty text" in {
    val res: Form[CustomQuoteForm] = RequestForm.quotesQueryForm.bind(Map("quote" -> ""))
    res.hasErrors shouldBe true
    res.errors("quote").head.message shouldBe "error.required"
  }

  it should "author field can be optional" in {
    val res: Form[CustomQuoteForm] = RequestForm.quotesQueryForm.bind(Map("quote" -> "Random quote", "author" -> ""))
    res.hasErrors shouldBe false
    res.value.flatMap(_.author) shouldBe None
  }

  it should "genre field should be coming from enum of genres" in {
    val res: Form[CustomQuoteForm] =
      RequestForm.quotesQueryForm.bind(Map("quote" -> "Random quote", "genre" -> "wrong"))
    res.hasErrors shouldBe true
    res.errors("genre").head.message shouldBe "Database is empty with that genre: wrong}"
  }
}
