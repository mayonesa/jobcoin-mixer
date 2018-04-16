package models

import java.util.{ Timer, TimerTask }
import concurrent.duration._
import play.api.Logger
import utils.currentTime

trait T {
  val t0 = currentTime
  val t1 = t0 + T.TIncr
  val t2 = t1 + T.TIncr
  val t3 = t2 + T.TIncr

  def atT1(body: => Unit): Unit = schedule(body, t1)
  def delay(body: => Unit, d: Duration): Unit
  def schedule(body: ⇒ Unit, when: Duration): Unit = delay(body, when - currentTime)
}

object T {
  val TIncr = 1 minute

  def apply(): T = T(850 milliseconds)
  def apply(tol: Duration): T = new TImpl(tol)

  private class TImpl(tol: Duration) extends T {
    private val s = new Timer

    def delay(body: => Unit, d: Duration) =
      if (d < Duration.Zero && -d < tol) {
        Logger.warn(s"delay: execution after current time but within $tol tolerance")
        body
      } else
        s.schedule(new TimerTask {
          def run = body
        }, d.toMillis)
  }
}