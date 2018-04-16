package services

import concurrent.{ ExecutionContext, Future }
import javax.inject.{ Inject, Singleton }
import play.api.Logger
import utils.randomString
import models.Address

@Singleton
class AddressNamingService @Inject() (exchange: ExchangeService)(implicit ec: ExecutionContext) {
  def apply(): Future[Address] = {
    val da = randomString(5, 11)
    exchange.exists(da).flatMap { exists =>
      if (exists) {
        Logger.warn(s"new deposit address, $da, collision")
        apply
      } else {
        Logger.info(s"deposit account, $da, created")
        Future(da)
      }
    }
  }
}