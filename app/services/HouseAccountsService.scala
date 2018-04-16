package services

import javax.inject.{ Inject, Singleton }
import concurrent.{ ExecutionContext, Future, Await }
import concurrent.duration._
import play.api.Logger
import adts.ShufflingRoundRobin
import models._

@Singleton
class HouseAccountsService @Inject() (exchange: ExchangeService,
                                      addressNamer: AddressNamingService,
                                      scheduler: SchedulingService)(implicit ec: ExecutionContext) {
  val n = 5
  private[services] val delayMax = T.TIncr
  private val acctInitTimeout = 5 seconds
  private val haNames = Await.result(Future.sequence(Vector.fill(n)(addressNamer())), n * acctInitTimeout)
  
  // Shuffling reduces pattern recognition because house accounts do not get
  // deposited/withdrawn in the exact same sequential order everytime. 
  private val has = ShufflingRoundRobin(haNames.map((_, Jobcoin.Zero)))

  Logger.debug("house accts: " + has)

  def update(dat: (Address, T), total: Jobcoin): Unit = {
    val (da, t) = dat
    Logger.debug(s"house accounts received instruction to accept $total from $da")
    val amts = total.randomSummands(n)
    (0 until n).foreach { j =>
      val amt = amts(j)
      val (ha, _) = has.next
      scheduler.randomSched(t.t2, t.t3) {
        exchange.transfer(da, ha, amt)
      }
      // track balances reflective of scheduled (future) transactions
      deposit(amt)
    }
  }

  def transferTo(to: Address, amt: Jobcoin, startTime: FiniteDuration): Unit = {
    val ha = has.current
    val haAddress = ha._1
    Logger.debug(s"`transferTo`: scheduling $haAddress (balance: ${ha._2}) -> $to: $amt")
    scheduler.randomSched(startTime, startTime + delayMax) {
      exchange.transfer(haAddress, to, amt)
    }
    // track balances reflective of scheduled (future) transactions
    withdraw(amt)
  }

  def nextBal: Jobcoin = {
    val next = has.next
    val haBal = next._2
    Logger.debug(s"`nextBal`: ${next._1} balance: $haBal")
    haBal
  }

  private def withdraw(amt: Jobcoin) = {
    val (ha, bal) = has.current
    val newBal = bal - amt
    assert(!newBal.isNeg, s"$amt makes $ha's prior balance of $bal negative")
    has.current((ha, newBal))
  }
  
  private def deposit(amt: Jobcoin) = {
    val (ha, bal) = has.current
    val newBal = bal + amt
    has.current((ha, newBal))
  }
}
