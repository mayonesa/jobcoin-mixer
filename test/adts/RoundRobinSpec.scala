package adts

import org.scalatestplus.play._
import scala.Vector

class RoundRobinSpec extends PlaySpec {
  "1-element round robin next" must {
    val e = 1
    val rr = RoundRobin(Vector(e))

    "be equal to the element on the 1st call" in {
      rr.next mustBe e
      rr.current mustBe e
    }
    "be equal to the element on the 2nd call" in {
      rr.next mustBe e
      rr.current mustBe e
    }
  }

  "2-element round robin next" must {
    val v = Vector(10, 30)
    val rr = RoundRobin(v)

    "be equal to the 1st element on the 1st call" in {
      val e = v(0)
      rr.next mustBe e
      rr.current mustBe e
    }
    "be equal to the 2nd element on the 2nd call" in {
      val e = v(1)
      rr.next mustBe e
    }
    "be equal to the 1st element on the 3rd call" in {
      val e = v(0)
      rr.next mustBe e
      rr.current mustBe e
    }
    "be equal to the 2nd element on the 4th call" in {
      val e = v(1)
      rr.next mustBe e
      rr.current mustBe e
    }
    "be equal to the 1st element on the 5th call" in {
      val e = v(0)
      rr.next mustBe e
      rr.current mustBe e
    }
    "be equal to the 2nd element on the 6th `next` call" in {
      val e = v(1)
      rr.next mustBe e
    }
    "be equal to the 2nd element on the 6th `current` call" in {
      val e = v(1)
      rr.current mustBe e
    }
  }
}