package models

import util.Random
import scala.Vector

object Jobcoin {
  val Zero: Jobcoin = Jobcoin(0)
  private val Precision = 14

  def apply(bd: BigDecimal): Jobcoin = new Jobcoin(bd)
  def nonNegMin(jcs: Jobcoin*): Jobcoin = Jobcoin(jcs.filter(!_.isNeg).map(_.bd).min)
}

class Jobcoin(val bd: BigDecimal) extends AnyVal {
  def isPos: Boolean = bd > 0
  def isNeg: Boolean = bd < 0
  def isZero: Boolean = bd == 0
  def unary_- = Jobcoin(-bd)
  def + (jc: Jobcoin): Jobcoin = Jobcoin(bd + jc.bd)
  def - (jc: Jobcoin): Jobcoin = Jobcoin(bd - jc.bd)
  def * (flt: Float): Jobcoin = Jobcoin(bd * flt)
  def / (flt: Float): Jobcoin = Jobcoin(bd / flt)
  def randomSummands(n: Int): Vector[Jobcoin] = {
    val r = new Random
    if (n == 1) Vector(Jobcoin(bd))
    else {
      val summandMax = bd / n
      val summandsInit = Vector.fill(n - 1)(r.nextFloat * summandMax)
      val rem = bd - summandsInit.sum
      (summandsInit :+ rem).map(Jobcoin(_))
    }
  }
  override def toString: String = s"Jobcoin($bd)"
}
