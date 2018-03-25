package services

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }
import java.util.concurrent.atomic.AtomicLong
import services.auxiliaries._
import concurrent.duration._
import java.util.Timer
import play.api.Logger
import scala.util.Random
import models._

class JobcoinMixService @Inject() (exchange: ExchangeService)(implicit ec: ExecutionContext) {
  private val uid = new AtomicLong(0)
  private val houseAccount = "houseAcct"

  def mix(proxyRecipients: List[Address]): Address = {
    val depositAddress = newDepositAddress
    processDeposit(depositAddress, proxyRecipients)
    depositAddress
  }

  private def processDeposit(depositAddress: Address, proxyRecipients: List[Address]) = {
    val pollPeriod = 5 seconds
    val scheduler = new Timer
    val t = T()

    def loop: Unit =
      if (currentTime <= t.t2)
        exchange.balance(depositAddress).foreach {
          _.fold(loop) { bal =>
            def dstrbt = distribute(proxyRecipients, bal, t)
            if (bal != 0)
              exchange.transfer(depositAddress, houseAccount, bal).foreach {
                if (_)
                  if (currentTime < t.t3) schedule(() => dstrbt, t.t3 - currentTime, scheduler)
                  else dstrbt
              }
            else schedule(() => loop, pollPeriod, scheduler)
          }
        }
      else Logger.warn(s"deposit into $depositAddress not received by T+2")

    schedule(() => loop, t.t1 - currentTime, scheduler)
  }

  private def distribute(proxyRecipients: List[Address], bal: Jobcoin, t: T) = {
    val r = new Random
    val nRecipients = proxyRecipients.size
    val sameAmt = bal / (nRecipients + 1)
    val distributedBal = List.fill(nRecipients - 1)(sameAmt) :+ bal - sameAmt * (nRecipients - 1)
    proxyRecipients.zip(distributedBal).foreach {
      case (to, amt) =>
        def transfer: Unit = exchange.transfer(houseAccount, to, amt)
        schedule(() => transfer, t.t3 + (r.nextFloat minutes) - currentTime, new Timer)
    }
  }

  private def newDepositAddress = s"da${uid.incrementAndGet}"
}