package models

import java.util.{ Timer, TimerTask }
import concurrent.duration.FiniteDuration
import services.auxiliaries.currentTime

case class Scheduler() {
  private val s = new Timer

  def schedule(body: () ⇒ Unit, when: FiniteDuration): TimerTask = {
    val task = new TimerTask {
      def run = body()
    }
    s.schedule(task, (when - currentTime).toMillis)
    task
  }
}