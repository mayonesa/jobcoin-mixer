package services

import util.Random

package object auxiliaries {
  type Address = String
  type Jobcoin = BigDecimal

  def randomSummands(n: Int, sum: Jobcoin): List[Jobcoin] = {
    val r = new Random
    if (n == 1) List(sum) else {
      val summandMax = sum / (n - 1)
      val summandsTail = List.fill(n - 1)(r.nextFloat * summandMax)
      (sum - summandsTail.sum) :: summandsTail
    }
  }

  def randomString(n: Int): String = {
    val r = new Random
    r.alphanumeric.take(r.nextInt(n)).mkString
  }
}