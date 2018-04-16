package services

import concurrent.{ ExecutionContext, Future }
import concurrent.duration._
import javax.inject.Inject
import play.api.Logger
import models.{ T, Address }
import utils.currentTime

class JobcoinMixService @Inject() (exchange: ExchangeService,
                                   addressNamer:  AddressNamingService,
                                   distributions: DistributionService)(implicit ec: ExecutionContext) {
  def apply(fantasmRecipients: Vector[Address]): Future[Address] =
    addressNamer().map { depositAddress =>
      processDeposit(depositAddress, fantasmRecipients)
      depositAddress
    }

  private def processDeposit(depositAddress: Address, fantasmRecipients: Vector[Address]) = {
    val pollPeriod = 5 seconds
    val pad = 15 millis // allows for processing right before cutoff time
    val t = T()

    def poll() = {
      Logger.debug("polling " + depositAddress)
      val nominalNext = currentTime + pollPeriod
      lazy val lastTime = t.t2 - pad
      val when = if (nominalNext < t.t2) nominalNext else lastTime
      t.schedule(loop, when)
    }

    def loop(): Unit =
      if (currentTime < t.t2)
        exchange.balanceOpt(depositAddress).foreach {
          _.fold(poll()) { bal =>
            if (bal.isNeg) Logger.error(s"deposit address, $depositAddress, is negative")
            else if (bal.isZero) poll()
            else {
              Logger.info(s"found $bal in $depositAddress")
              distributions((fantasmRecipients, t)) = (depositAddress, bal)
            }
          }
        }
      else Logger.warn(s"deposit into $depositAddress not received by T+2")

    t.atT1(loop)
  }
}
