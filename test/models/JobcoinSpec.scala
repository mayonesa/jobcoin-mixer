package models

import org.scalatestplus.play._

class JobcoinSpec extends PlaySpec {
  "Random summands" must {
    val sum = Jobcoin(24)
    def summands(n: Int) = sum.randomSummands(n)

    "be n size" in {
      val n = 10
      summands(n) must have length n
    }
    "add up to sum" in {
      summands(4).reduce(_ + _) mustBe sum
    }
    "be made up of non-negatives" in {
      summands(7).filter(_.isNeg) mustBe empty
    }
    "be size 1 when n = 1" in {
      val n = 1
      summands(n) must have length n
    }
    "be equal to sum when n = 1" in {
      summands(1).head mustBe sum
    }
    "add up to sum when sum is very precise" in {
      val sum = Jobcoin(10.6543454343234543)
      sum.randomSummands(10).reduce(_ + _) mustBe sum
    }
  }

  import Jobcoin.nonNegMin

  "Positive-minimum" must {
    "filter our non-positives and equal small positive" in {
      val min = Jobcoin(0E-20)
      nonNegMin(Jobcoin(-.0001), Jobcoin(0), min, Jobcoin(5)) mustBe min
    }
    "select minimum" in {
      val min = Jobcoin(5)
      nonNegMin(Jobcoin(6), Jobcoin(9), Jobcoin(7), min, Jobcoin(5.5)) mustBe min
    }
  }
}