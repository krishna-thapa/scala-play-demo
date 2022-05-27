package helper

import org.scalatest.compatible.Assertion
import org.scalatest.flatspec.AnyFlatSpec

trait GenericFlatSpec extends AnyFlatSpec {

  // For the generic test cases
  def runGenericTest(
    assertion: => Assertion,
    counter: Int,
    subject: "searchAuthors()",
    desc: "result from search authors"
  ): Unit = {
    val testDesc: String = s"$desc (test no: $counter)"
    if (counter == 0) subject should testDesc in {
      assertion
    }
    else
      it should testDesc in {
        assertion
      }
  }

}
