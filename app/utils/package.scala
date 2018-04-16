import util.Random
import concurrent.duration._

package object utils {
  def currentTime: FiniteDuration = System.currentTimeMillis().millis

  def randomString(floor: Int, roof: Int): String = {
    val r = new Random
    val h = roof - floor
    r.alphanumeric.take(r.nextInt(h) + h - 1).mkString
  }
}