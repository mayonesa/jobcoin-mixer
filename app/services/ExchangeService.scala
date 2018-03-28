package services

import javax.inject.Inject
import play.api.libs.ws.{ WSClient, WSResponse }
import scala.concurrent.{ ExecutionContext, Future, Promise }
import play.api.libs.json.{ Json, JsValue, JsObject }
import play.api.http.Status.OK
import services.auxiliaries.{ Address, Jobcoin }
import concurrent.duration._
import play.api.Logger
import util.{ Success, Failure }

class ExchangeService @Inject() (ws: WSClient)(implicit ec: ExecutionContext) {
  private val transactionsUrl = "http://jobcoin.gemini.com/vendetta/api/transactions"

  def balance(address: Address): Future[Option[Jobcoin]] = {
    lazy val errMsg = s"balance: `addresses` service for $address did not respond well "
    addressInfo(address).map { resp =>
      if (resp.status == OK) Some(balance(resp.json))
      else {
        Logger.warn(errMsg + error(resp))
        None
      }
    }.recover {
      case e =>
        Logger.warn(errMsg + e.getMessage)
        None
    }
  }

  def exists(address: Address): Future[Boolean] = {
    val p = Promise[Boolean]
    addressInfo(address).flatMap { resp =>
      if (resp.status == OK) p.success(hasTransactions(resp.json)).future
      else {
        val errMsg = s"`addresses` service for $address did not respond well ${error(resp)}"
        Logger.error("exists: " + errMsg)
        p.failure(new IllegalStateException(errMsg)).future
      }
    }
  }

  def transfer(from: Address, to: Address, amt: Jobcoin, cb: Unit => Unit = _ => ()): Unit = {
    val requestJson = Json.parse(s"""{ "amount": $amt, "fromAddress": "$from", "toAddress": "$to" }""")
    val logMsg = s"transfer(): $from -> $to: $amt jobcoins"
    lazy val errMsg = logMsg + " failed: "
    Logger.debug("initiating " + logMsg)
    ws.url(transactionsUrl).post(requestJson).onComplete {
      case Success(resp) =>
        if (resp.status == OK) {
          Logger.info(logMsg + " succeeded")
          cb(())
        } else Logger.error(errMsg + error(resp))
      case Failure(e) => Logger.error(errMsg + e.getMessage)
    }
  }

  private def addressInfo(address: Address) = ws.url(addressInfoUrl(address)).get

  private def addressInfoUrl(address: Address) = "http://jobcoin.gemini.com/vendetta/api/addresses/" + address

  private def balance(addressInfoJson: JsValue) = (addressInfoJson \ "balance").as[BigDecimal]

  private def hasTransactions(addressInfoJson: JsValue) = !(addressInfoJson \ "transactions").as[List[JsObject]].isEmpty

  private def error(resp: WSResponse) = s"(status: ${resp.status}): ${(resp.json \ "error").as[String]}"
}