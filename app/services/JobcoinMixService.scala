package services

import javax.inject.Inject
import concurrent.{ ExecutionContext, Future }
import services.auxiliaries._
import concurrent.duration._
import play.api.Logger
import models._
import models.Timing.currentTime

class JobcoinMixService @Inject() (exchange: ExchangeService)(implicit ec: ExecutionContext) {
  private val houseAccount = "houseAcct"

  def mix(proxyRecipients: List[Address]): Future[Address] =
    newDepositAddress.map { depositAddress =>
      processDeposit(depositAddress, proxyRecipients)
      depositAddress
    }

  private def processDeposit(depositAddress: Address, proxyRecipients: List[Address]) = {
    val pollPeriod = 5 seconds
    val pad = 15 millis
    val t = Timing()

    def poll = {
      val when = currentTime + pollPeriod
      t.schedule(() => loop, if (when < t.t2) when else t.t2 - pad)
    }

    def loop: Unit =
      if (currentTime < t.t2)
        exchange.balance(depositAddress).foreach {
          _.fold(poll) { bal =>
            if (bal < 0) Logger.error(s"deposit address, $depositAddress, is negative")
            else if (bal == 0) poll
            else {
              Logger.info(s"$bal jobcoins -> $depositAddress")
              t.atT2(() => exchange.transfer(depositAddress, houseAccount, bal).foreach { _ =>
                t.atT3(() => distribute(proxyRecipients, bal, t))
              })
            }
          }
        }
      else Logger.warn(s"deposit into $depositAddress not received by T+2")

    t.atT1(() => loop)
  }

  private def distribute(proxyRecipients: List[Address], bal: Jobcoin, t: Timing) = {
    val recipientDistros = proxyRecipients.zip(randomSummands(proxyRecipients.size, bal))

    Logger.debug("scheduling distributions to: " + proxyRecipients)
    t.randomIncreasingSched({
      case (recipient: Address, amt: Jobcoin) => exchange.transfer(houseAccount, recipient, amt)
    }, recipientDistros, t.t3, t.t4)
  }

  private def newDepositAddress: Future[String] = {
    val da = randomString(10)
    exchange.exists(da).flatMap { exists =>
      if (exists) {
        Logger.warn(s"new deposit address, $da, collision")
        newDepositAddress
      } else {
        Logger.info(s"deposit account, $da, created")
        Future(da)
      }
    }
  }
}