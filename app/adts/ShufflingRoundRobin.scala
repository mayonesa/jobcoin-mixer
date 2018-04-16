package adts

import scala.util.Random

// shuffles order after every round
trait ShufflingRoundRobin[E] {
  def size: Int
  def current(e: E): Unit
  def current: E
  def next: E
}

object ShufflingRoundRobin {
  def apply[E](es: Vector[E]): ShufflingRoundRobin[E] = new ShufflingRoundRobinImpl(es)

  private class ShufflingRoundRobinImpl[E](private var es: Vector[E]) extends ShufflingRoundRobin[E] {
    private var i = -1
    private val r = new Random

    def size = es.size
    def current = es(i)
    def current(e: E) = es = es.updated(i, e)

    def next = {
      if (i < size - 1) i = i + 1
      else {
        i = 0
        es = r.shuffle(es)
      }
      es(i)
    }

    override def toString: String = s"ShufflingRoundRobin(${es.foldLeft("")(_ + ", " + _.toString).substring(2)})"
  }
}
