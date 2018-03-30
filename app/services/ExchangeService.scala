package services

import javax.inject.Inject
import play.api.libs.ws.{ WSClient, WSResponse }
import scala.concurrent.{ ExecutionContext, Future, Promise }
import play.api.libs.json.{ Json, JsValue, JsObject }
import play.api.http.Status.OK
import services.auxiliaries.{ Address, Jobcoin }
import play.api.Logger
import util.{ Success, Failure }

class ExchangeService @Inject() (ws: WSClient)(implicit ec: ExecutionContext) {
  private val transactionsUrl = "http://jobcoin.gemini.com/vendetta/api/transactions"

  def balance(address: Address): Future[Option[Jobcoin]] = {
    def none(append: String) = {
      val errMsg = s"balance: `addresses` service for $address did not respond well "
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

  def transfer(from: Address, to: Address, amt: Jobcoin): Future[Unit] = {
    val requestJson = Json.parse(s"""{ "amount": $amt, "fromAddress": "$from", "toAddress": "$to" }""")
    val logMsg = s"transfer(): $from -> $to: $amt jobcoins"

    def fail(append: String) = {
      val errMsg = logMsg + " failed: " + append
      Logger.error(errMsg)
      Failure(new IllegalStateException(errMsg))
    }

    Logger.debug("initiating " + logMsg)
    ws.url(transactionsUrl).post(requestJson).transform {
      case Success(resp) =>
        if (resp.status == OK) {
          Logger.info(logMsg + " succeeded")
          Success(())
        } else fail(error(resp))
      case Failure(e) => fail(e.getMessage)
    }
  }

  private def addressInfo(address: Address) = ws.url(addressInfoUrl(address)).get

  private def addressInfoUrl(address: Address) = "http://jobcoin.gemini.com/vendetta/api/addresses/" + address

  private def balance(addressInfoJson: JsValue) = (addressInfoJson \ "balance").as[Jobcoin]

  private def hasTransactions(addressInfoJson: JsValue) = !(addressInfoJson \ "transactions").as[List[JsObject]].isEmpty

  private def error(resp: WSResponse) = s"(status: ${resp.status}): ${(resp.json \ "error").as[String]}"
}