package models

import java.util.{ Timer, TimerTask }
import concurrent.duration.Duration
import services.auxiliaries.currentTime

case class Scheduler() {
  private val s = new Timer

  def schedule(body: () ⇒ Unit, when: Duration): TimerTask = {
    val task = new TimerTask {
      def run = body()
    }
    s.schedule(task, (when - currentTime).toMillis)
    task
  }
}