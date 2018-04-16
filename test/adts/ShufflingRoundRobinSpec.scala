package adts.mutable

import org.scalatestplus.play._
import adts.ShufflingRoundRobin

class ShufflingRoundRobinSpec extends PlaySpec {
  "1-element round robin" must {
    val e = 1
    val srr = ShufflingRoundRobin(Vector(e))

    "be equal to the element on the 1st call" in {
      srr.next mustBe e
      srr.current mustBe e
    }
    "be equal to the element on the 2nd call" in {
      srr.next mustBe e
      srr.current mustBe e
    }
  }

  "2-element round robin" must {
    val v = Vector(10, 30)
    val srr = ShufflingRoundRobin(v)
    var acc = 0

    "be equal to the 1st element on the 1st call" in {
      val e = v(0)
      srr.next mustBe e
      srr.current mustBe e
    }
    "be equal to the 2nd element on the 2nd call" in {
      val e = v(1)
      srr.next mustBe e
      srr.current mustBe e
    }
    "be equal to one of the elements on the 3rd call" in {
      val e = srr.next
      acc = e
      v must contain(e)
    }
    "be equal to the left-over element on the 4th call" in {
      val e = v((v.indexOf(acc) + 1) % 2)
      srr.next mustBe e
      srr.current mustBe e
    }
    "be equal to one of the elements on the 5th call" in {
      val e = srr.next
      acc = e
      v must contain(e)
    }
    "be equal to the left-over element on the 6th call" in {
      val e = v((v.indexOf(acc) + 1) % 2)
      srr.next mustBe e
      srr.current mustBe e
    }
  }

  "1-element `current(newVal)`" must {
    val e = 1
    val srr = ShufflingRoundRobin(Vector(e))

    "update the element" in {
      srr.next
      srr.current(2)
      srr.current mustBe 2
    }
  }
  "2-element `current(newVal)`" must {
    val v = Vector(10, 30)
    val srr = ShufflingRoundRobin(v)
    var acc = 0

    "update the 1st element" in {
      srr.next
      srr.current(2)
      srr.current mustBe 2
    }
    "update the 2nd element" in {
      srr.next
      srr.current(3)
      srr.current mustBe 3
    }
    "update the elment after the 3rd `next` call" in {
      srr.next
      srr.current(15)
      srr.current mustBe 15
    }
    "update the elment after the 4th `next` call" in {
      srr.next
      srr.current(50)
      srr.current mustBe 50
    }
  }
}