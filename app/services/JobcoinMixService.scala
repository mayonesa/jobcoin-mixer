package services

import javax.inject.Inject
import concurrent.{ ExecutionContext, Future }
import services.auxiliaries._
import concurrent.duration._
import play.api.Logger
import util.Random
import models._

class JobcoinMixService @Inject() (exchange: ExchangeService)(implicit ec: ExecutionContext) {
  private val houseAccount = "houseAcct"

  def mix(proxyRecipients: Vector[Address]): Future[Address] =
    newDepositAddress.map { depositAddress =>
      processDeposit(depositAddress, proxyRecipients)
      depositAddress
    }

  private def processDeposit(depositAddress: Address, proxyRecipients: Vector[Address]) = {
    val pollPeriod = 5 seconds
    val s = Scheduler()
    val t = T()

    def poll(when: FiniteDuration = currentTime + pollPeriod) =
      if (when < t.t2) s.schedule(() => loop, when)
      else loop

    def loop: Unit =
      if (currentTime < t.t2)
        exchange.balance(depositAddress).foreach {
          _.fold(poll()) { bal =>
            if (bal != 0) {
              Logger.info(s"$bal -> $depositAddress")
              s.schedule(() => exchange.transfer(depositAddress, houseAccount, bal, { _ =>
                s.schedule(() => distribute(proxyRecipients, bal, t), t.t3)
              }), t.t2)
            } else poll()
          }
        }
      else Logger.warn(s"deposit into $depositAddress not received by T+2")

    poll(t.t1)
  }

  private def distribute(proxyRecipients: Vector[Address], bal: Jobcoin, t: T) = {
    val r = new Random
    val nRecipients = proxyRecipients.size
    val distros = if (nRecipients == 1) Vector(bal) else {
      val distMax = bal / (nRecipients - 1)
      val distributedBalTail = Vector.tabulate(nRecipients - 1)(_ => r.nextFloat * distMax)
      (bal - distributedBalTail.sum) +: distributedBalTail
    }
    val whens = if (nRecipients == 1) Vector(t.t3 + (r.nextFloat minutes)) else {
      val sliceMax = (1 minute) / (nRecipients - 1)
      def nextWhen(base: Duration) = base + r.nextFloat * sliceMax
      val wInit = (1 until nRecipients).scanLeft(nextWhen(t.t3)) { (w, _) =>
        nextWhen(w)
      }
      wInit :+ (t.t4 - wInit.last) * r.nextFloat + wInit.last
    }

    Logger.debug("scheduling distributions to: " + proxyRecipients)
    (0 until nRecipients).foreach { i =>
      Scheduler().schedule(() => exchange.transfer(houseAccount, proxyRecipients(i), distros(i)), whens(i))
    }
  }

  private def newDepositAddress: Future[String] = {
    val r = new Random
    val da = r.alphanumeric.take(r.nextInt(10)).mkString
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