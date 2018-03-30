package models

import java.util.{ Timer, TimerTask }
import concurrent.duration._
import services.auxiliaries.currentTime
import play.api.Logger
import util.Random

case class Timing(tol: Duration = 1 second) {
  private val s = new Timer
  val t0 = currentTime
  private def t(i: Int) = t0 + (i minutes)
  val t1 = t(1)
  val t2 = t(2)
  val t3 = t(3)
  val t4 = t(4)

  def atT1(body: () => Unit): Unit = schedule(body, t1)
  def atT2(body: () => Unit): Unit = schedule(body, t2)
  def atT3(body: () => Unit): Unit = schedule(body, t3)

  def delay(body: () => Unit, d: Duration): Unit =
    if (d < (0 millis) && -d < tol) {
      Logger.warn(s"schedule: `when` is less than current time but within $tol tolerance")
      body()
    } else
      s.schedule(new TimerTask {
        def run = body()
      }, d.toMillis)

  def schedule(body: () ⇒ Unit, when: Duration): Unit = delay(body, when - currentTime)

  def randomIncreasingSched(iteration: Any => Unit, ins: List[Any], floor: Duration, ceil: Duration): Unit =
    ins.zip(randomIncreasingTimes(ins.size, floor, ceil)).foreach {
      case (in, when) => schedule(() => iteration(in), when)
    }

  def randomIncreasingTimes(n: Int, floor: Duration, ceil: Duration): IndexedSeq[Duration] = {
    val r = new Random
    if (n == 1) IndexedSeq(floor + (r.nextFloat minutes))
    else {
      val sliceMax = (1 minute) / (n - 1)
      def nextWhen(base: Duration) = base + r.nextFloat * sliceMax
      val wInit = (1 until n).scanLeft(nextWhen(floor)) { (w, _) =>
        nextWhen(w)
      }
      wInit :+ (ceil - wInit.last) * r.nextFloat + wInit.last
    }
  }
}