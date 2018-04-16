package services

import concurrent.duration._
import annotation.tailrec
import javax.inject.Inject
import util.Random
import play.api.Logger
import adts.RoundRobin
import models._
import models.Jobcoin.nonNegMin
import models.T.TIncr

class DistributionService @Inject() (houseAccounts: HouseAccountsService, scheduler: SchedulingService) {
  private val xferRate = 2
  private val r = new Random
  private var distributions = List.empty[(Jobcoin, RoundRobin[Address], FiniteDuration)]

  // periodic distributor of all queued-up distribution requests
  scheduler(TIncr * 3, houseAccounts.delayMax) {
    Logger.debug("distribution cyclical check")
    r.synchronized {
      if (!distributions.isEmpty) {
        distribute()
        distributions = Nil
      }
    }
  }

  def update(drT: (Vector[Address], T), daAmt: (Address, Jobcoin)): Unit =
    r.synchronized {
      val (distributionRecipients, t) = drT
      val (depositAddress, amt) = daAmt
      houseAccounts((depositAddress, t)) = amt
      distributions = (amt, RoundRobin(distributionRecipients), t.t3) :: distributions
    }

  private def distribute() = {
    Logger.debug("distributing: " + distributions)
    val t3 = distributions.head._3
    distributions.foreach {
      case (distributionAmt, distributionRecipients, _) =>
        val maxXfer = distributionAmt / distributionRecipients.size / houseAccounts.n * xferRate

        @tailrec
        def loop(currDistBal: Jobcoin): Unit =
          if (currDistBal.isPos) {
            val haBal = houseAccounts.nextBal
            if (haBal.isPos) {
              val rAmt = maxXfer * r.nextFloat
              val amt = nonNegMin(rAmt, currDistBal, haBal)
              val distRecipient = distributionRecipients.next
              Logger.debug(s"distRecipient: $distRecipient (current bal: $currDistBal), maxXfer: $maxXfer, rAmt: $rAmt, haBal: $haBal, amt: $amt")
              houseAccounts.transferTo(distRecipient, amt, t3)
              loop(currDistBal - amt)
            } else loop(currDistBal)
          }

        loop(distributionAmt)
    }
  }
}
