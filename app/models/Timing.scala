package models

import java.util.{ Timer, TimerTask }
import concurrent.duration._
import play.api.Logger
import util.Random
import Timing._

trait Timing {
  val t0 = currentTime
  private def t(i: Int) = t0 + (i minutes)
  val t1 = t(1)
  val t2 = t(2)
  val t3 = t(3)
  val t4 = t(4)
  def atT1(body: () => Unit): Unit = schedule(body, t1)
  def atT2(body: () => Unit): Unit = schedule(body, t2)
  def atT3(body: () => Unit): Unit = schedule(body, t3)
  def delay(body: () => Unit, d: Duration): Unit
  def schedule(body: () ⇒ Unit, when: Duration): Unit = delay(body, when - currentTime)
  def randomIncreasingSched(iteration: Any => Unit, ins: List[Any], floor: Duration, ceil: Duration): Unit =
    ins.zip(randomIncreasingTimes(ins.size, floor, ceil)).foreach {
      case (in, when) => schedule(() => iteration(in), when)
    }
}

object Timing {
  def apply(): Timing = apply(1 second)
  def apply(tol: Duration): Timing = new TimingImpl(tol)
  def currentTime = System.currentTimeMillis().millis
  def randomIncreasingTimes(n: Int, floor: Duration, ceil: Duration): IndexedSeq[Duration] = {
    val r = new Random
    if (n == 1) IndexedSeq(floor + (r.nextFloat minutes))
    else {
      val sliceMax = (ceil - floor) / n
      def nextWhen(base: Duration) = base + r.nextFloat * sliceMax
      val wInit = (1 until (n - 1)).scanLeft(nextWhen(floor)) { (w, _) =>
        nextWhen(w)
      }
      wInit :+ (ceil - wInit.last) * r.nextFloat + wInit.last
    }
  }

  private class TimingImpl(tol: Duration) extends Timing {
    private val s = new Timer

    def delay(body: () => Unit, d: Duration): Unit =
      if (d < (0 millis) && -d < tol) {
        Logger.warn(s"schedule: `when` is less than current time but within $tol tolerance")
        body()
      } else
        s.schedule(new TimerTask {
          def run = body()
        }, d.toMillis)
  }
}