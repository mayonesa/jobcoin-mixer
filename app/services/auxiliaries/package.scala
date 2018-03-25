package services

import concurrent.duration._
import java.util.{ Timer, TimerTask }
import scala.util.Random

package object auxiliaries {
  type Address = String
  type Jobcoin = BigDecimal
  
  def schedule(body: () ⇒ Unit, delay: Duration, scheduler: Timer): TimerTask = {
    val task = new TimerTask {
      def run = body()
    }
    scheduler.schedule(task, delay.toMillis)
    task
  }

  def currentTime = System.currentTimeMillis().millis  
}