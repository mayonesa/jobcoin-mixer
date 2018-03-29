package models

import java.util.{ Timer, TimerTask }
import concurrent.duration._
import services.auxiliaries.currentTime
import play.api.Logger

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
}