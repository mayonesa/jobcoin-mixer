package services

import javax.inject.{ Inject, Singleton }
import play.api
import api.libs.ws.{ WSClient, WSResponse }
import api.libs.json.{ Json, JsValue, JsObject }
import api.http.Status.OK
import api.Logger
import scala.concurrent.{ ExecutionContext, Future, Promise }
import models._
import util.{ Success, Failure }

@Singleton
class ExchangeService @Inject() (ws: WSClient)(implicit ec: ExecutionContext) {
  private val transactionsUrl = "http://jobcoin.gemini.com/vendetta/api/transactions"

  def balance(address: Address): Future[Jobcoin] = okOnlyAddress(address, balance)

  // enables potentially-temporal anomaly tolerance
  def balanceOpt(address: Address): Future[Option[Jobcoin]] = {
    def none(append: String) = {
      val errMsg = s"balanceOpt: `addresses` service for $address did not respond well "
      Logger.warn(errMsg + append)
      None
    }

    addressInfo(address).map { resp =>
      if (resp.status == OK) Some(balance(resp.json))
      else none(error(resp))
    }.recover {
      case e => none(e.getMessage)
    }
  }

  def exists(address: Address): Future[Boolean] = okOnlyAddress(address, hasTransactions)

  def transfer(from: Address, to: Address, amt: Jobcoin): Future[Unit] = {
    val amtBd = amt.bd.setScale(13, BigDecimal.RoundingMode.DOWN)
    val requestJson = Json.parse(s"""{ "amount": $amtBd, "fromAddress": "$from", "toAddress": "$to" }""")
    val logMsg = s"transfer(): $from -> $to: $amtBd"

    def fail(append: String) = {
      val errMsg = logMsg + " failed: " + append
      Logger.error(errMsg)
      Failure(new IllegalStateException(errMsg))
    }

    Logger.debug("requesting " + logMsg)
    ws.url(transactionsUrl).post(requestJson).transform {
      case Success(resp) =>
        if (resp.status == OK) {
          Logger.info(logMsg + " succeeded")
          Success(())
        } else fail(error(resp))
      case Failure(e) => fail(e.getMessage)
    }
  }

  private def okOnlyAddress[T](address: Address, f: JsValue => T): Future[T] = {
    val p = Promise[T]
    addressInfo(address).flatMap { resp =>
      if (resp.status == OK) p.success(f(resp.json)).future
      else {
        val errMsg = s"`addresses` service for $address did not respond well ${error(resp)}"
        Logger.error("okOnlyAddress: " + errMsg)
        p.failure(new IllegalStateException(errMsg)).future
      }
    }
  }

  private def addressInfo(address: Address) = ws.url(addressInfoUrl(address)).get

  private def addressInfoUrl(address: Address) = "http://jobcoin.gemini.com/vendetta/api/addresses/" + address

  private def balance(addressInfoJson: JsValue) = Jobcoin((addressInfoJson \ "balance").as[BigDecimal])

  private def hasTransactions(addressInfoJson: JsValue) = !(addressInfoJson \ "transactions").as[List[JsObject]].isEmpty

  private def error(resp: WSResponse) = {
    val json = resp.json
    val msg = (json \ "error").asOpt[String].getOrElse(json.as[String])
    s"(status: ${resp.status}): $msg"
  }
}