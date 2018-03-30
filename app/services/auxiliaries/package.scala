package services

import concurrent.duration._
import util.Random

package object auxiliaries {
  type Address = String
  type Jobcoin = BigDecimal

  def currentTime = System.currentTimeMillis().millis

  def randomSummands(n: Int, sum: Jobcoin): List[Jobcoin] = {
    val r = new Random
    if (n == 1) List(sum) else {
      val summandMax = sum / (n - 1)
      val summandsTail = List.tabulate(n - 1)(_ => r.nextFloat * summandMax)
      (sum - summandsTail.sum) :: summandsTail
    }
  }

  def randomString(n: Int): String = {
    val r = new Random
    r.alphanumeric.take(r.nextInt(n)).mkString
  }
}