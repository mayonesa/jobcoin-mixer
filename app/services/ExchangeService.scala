package services

import javax.inject.Inject
import play.api.libs.ws.WSClient
import scala.concurrent.{ ExecutionContext, Future }
import play.api.libs.json.JsValue
import play.api.http.Status.OK
import services.auxiliaries.{ Address, currentTime, Jobcoin }
import concurrent.duration._
import play.api.Logger
import scala.concurrent.Promise
import scala.util.Success

class ExchangeService @Inject() (ws: WSClient)(implicit ec: ExecutionContext) {
  private val transactionsUrl = "http://jobcoin.gemini.com/vendetta/api/transactions"

  def balance(address: Address): Future[Option[Jobcoin]] =
    ws.url(addressInfoUrl(address)).get.map { resp =>
      if (resp.status == OK) Some(balance(resp.json))
      else None
    }.fallbackTo(Future(None))

  def transfer(from: Address, to: Address, amt: Jobcoin): Future[Unit] = {
    val requestJson = s"""{ "amount": $amt, "fromAddress": "$from", "toAddress": "$to" }"""
    Logger.debug(s"initiating transfer from $from to $to for $amt")
    ws.url(transactionsUrl).addHttpHeaders(("Content-Type" -> "application/json")).post(requestJson).flatMap {
      resp =>
        val logMsg = s"transfer from $from to $to for $amt"
        val p = Promise[Unit]()
        val status = resp.status
        if (resp.status == OK) {
          Logger.info(logMsg + " succeeded")
          p.success(()).future
        } else {
          val errMsg = s"$logMsg failed (status: $status): ${resp.body}"
          Logger.error(errMsg)
          p.failure(new IllegalStateException(errMsg)).future
        }
    }
  }

  private def addressInfoUrl(address: Address) = "http://jobcoin.gemini.com/vendetta/api/addresses/" + address

  private def balance(addressInfoJson: JsValue) = (addressInfoJson \ "balance").as[BigDecimal]
}