package adts

trait RoundRobin[E] {
  def size: Int
  def current: E
  def next: E
}

object RoundRobin {
  def apply[E](es: Vector[E]): RoundRobin[E] = new RoundRobinImpl(es)

  private class RoundRobinImpl[E](es: Vector[E]) extends RoundRobin[E] {
    private var i = -1

    def size = es.size
    def current = es(i)
    def next = {
      i = i + (if (i < size - 1) 1 else (1 - size))
      es(i)
    }
    override def toString: String = s"RoundRobin(${es.foldLeft("")(_ + ", " + _.toString).substring(2)})"
  }
}