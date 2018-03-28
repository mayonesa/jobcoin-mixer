package models

import java.util.{ Timer, TimerTask }
import concurrent.duration._
import services.auxiliaries.currentTime
import play.api.Logger

case class Scheduler(tol: Duration = 1 second) {
  private val s = new Timer

  def schedule(body: () ⇒ Unit, when: Duration): Unit = {
    val delay = when - currentTime
    if (delay < (0 millis) && -delay < tol) {
      Logger.warn(s"schedule: `when` is less than current time but within $tol tolerance")
      body()
    } else
      s.schedule(new TimerTask {
        def run = body()
      }, delay.toMillis)
  }
}