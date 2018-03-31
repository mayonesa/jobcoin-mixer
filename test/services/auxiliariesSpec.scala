package services

import org.scalatestplus.play._
import services.auxiliaries.randomSummands

class auxiliariesSpec extends PlaySpec {
  val sum = 24
  private def summands(n: Int) = randomSummands(n, sum)

  "Random summands" must {
    "be n size" in {
      val n = 10
      summands(n) must have length n
    }
    "add up to sum" in {
      summands(4).sum mustBe sum
    }
    "be made up of non-negatives" in {
      summands(7).filter(_ < 0) mustBe empty
    }
    "be size 1 when n = 1" in {
      val n = 1
      summands(n) must have length n
    }
    "be equal to sum when n = 1" in {
      summands(1).head mustBe sum
    }
  }
}