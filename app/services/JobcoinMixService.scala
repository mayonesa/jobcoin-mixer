package services

import javax.inject.Inject
import concurrent.ExecutionContext
import java.util.concurrent.atomic.AtomicLong
import services.auxiliaries._
import concurrent.duration._
import play.api.Logger
import util.Random
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
    val s = Scheduler()
    val t = T()

    def poll(when: FiniteDuration = currentTime + pollPeriod) = s.schedule(() => loop, when)

    def loop: Unit =
      if (currentTime < t.t2)
        exchange.balance(depositAddress).foreach {
          _.fold(poll()) { bal =>
            if (bal != 0) s.schedule(() => exchange.transfer(depositAddress, houseAccount, bal, { _ =>
              s.schedule(() => distribute(proxyRecipients, bal, t), t.t3)
            }), t.t2)
            else poll()
          }
        }
      else Logger.warn(s"deposit into $depositAddress not received by T+2")

    poll(t.t1)
  }

  private def distribute(proxyRecipients: List[Address], bal: Jobcoin, t: T) = {
    val r = new Random
    val nRecipients = proxyRecipients.size
    val sameAmt = bal / (nRecipients + 1)
    val distributedBal = List.fill(nRecipients - 1)(sameAmt) :+ bal - sameAmt * (nRecipients - 1)
    val distributions = proxyRecipients.zip(distributedBal)
    Logger.debug(s"attempting distributions: $distributions soon")
    distributions.foreach {
      case (to, amt) =>
        Scheduler().schedule(() => exchange.transfer(houseAccount, to, amt), t.t3 + (r.nextFloat minutes))
    }
  }

  private def newDepositAddress = s"da${uid.incrementAndGet}"
}