package models

import org.scalatestplus.play._
import concurrent.duration._
import models.Timing.randomIncreasingTimes

class TimingSpec extends PlaySpec {
  private val floor: Duration = 1 second
  private val ceil: Duration = (20 seconds)
  private def rit(n: Int) = randomIncreasingTimes(n, floor, ceil)
  private def withinEnds(ts: IndexedSeq[Duration]) = {
    ts.head must be >= floor
    ts.last must be <= ceil
  }

  "Random increasing times" must {
    "be n size" in {
      val n = 5
      rit(n) must have size n
    }
    "be increasing" in {
      rit(5).reduceLeft[Duration] {
        case (t1, t2) =>
          t1 must be < t2
          t2
      }
    }
    "be within the ends" in {
      withinEnds(rit(5))
    }
    "be size 1 when n is 1" in {
      val n = 1
      rit(n) must have size n
    }
    "be within ends when n is 1" in {
      withinEnds(rit(1))
    }
  }
}