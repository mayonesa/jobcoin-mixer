package services

import concurrent.ExecutionContext
import akka.actor.ActorSystem
import concurrent.duration._
import play.api.Logger
import util.Random
import javax.inject.{ Inject, Singleton }
import utils.currentTime

@Singleton
class SchedulingService @Inject() (system: ActorSystem)(implicit ec: ExecutionContext) {
  def apply(when: FiniteDuration)(f: => Unit): Unit = delay(when - currentTime)(f)

  def apply(initialDelay: FiniteDuration, interval: FiniteDuration)(f: => Unit): Unit =
    system.scheduler.schedule(initialDelay, interval)(f)

  def delay(delay: FiniteDuration)(f: => Unit): Unit = {
    if (delay < Duration.Zero) Logger.warn("negative delay. executing now instead.")
    system.scheduler.scheduleOnce(delay)(f)
  }

  def randomSched(floor: FiniteDuration, ceil: FiniteDuration)(f: => Unit): Unit = {
    val r = new Random
    val h = ceil - floor
    apply(floor + (r.nextFloat * h).asInstanceOf[FiniteDuration])(f)
  }
}