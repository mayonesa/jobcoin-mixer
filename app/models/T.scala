package models

import services.auxiliaries.currentTime
import concurrent.duration._

case class T() {
  val t0 = currentTime
  private def t(i: Int) = t0 + (i minutes)
  val t1 = t(1)
  val t2 = t(2)
  val t3 = t(3)
}