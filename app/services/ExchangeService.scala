package services

import javax.inject.Inject
import play.api.libs.ws.WSClient
import scala.concurrent.{ ExecutionContext, Future }
import play.api.libs.json.JsValue
import play.api.http.Status.OK
import services.auxiliaries.{ Address, currentTime, Jobcoin }
import concurrent.duration._
import play.api.Logger

class ExchangeService @Inject() (ws: WSClient)(implicit ec: ExecutionContext) {
  private val transactionsUrl = "http://jobcoin.gemini.com/vendetta/api/transactions"

  def balance(address: Address): Future[Option[Jobcoin]] =
    ws.url(addressInfoUrl(address)).get.map { resp =>
      if (resp.status == OK) Some(balance(resp.json))
      else None
    }

  def transfer(from: Address, to: Address, amt: Jobcoin, tryUntil: Int = 4): Future[Boolean] = {
    val requestJson = s"""{ "amount": $amt, "fromAddress": "$from", "toAddress": "$to" }"""

    def loop(i: Int): Future[Boolean] =
      ws.url(transactionsUrl).addHttpHeaders(("Content-Type" -> "application/json")).post(requestJson).flatMap {
        resp =>
          if (resp.status == OK) Future(true)
          else {
            Logger.warn(s"transfer attempt $i from $from to $to for $amt failed: ${resp.body}")
            if (i < tryUntil) loop(i + 1)
            else {
              Logger.error(s"all transfer attempts from $from to $to for $amt failed")
              Future(false)
            }
          }
      }

    loop(0)
  }

  private def addressInfoUrl(address: Address) = "http://jobcoin.gemini.com/vendetta/api/addresses/" + address

  private def balance(addressInfoJson: JsValue) = (addressInfoJson \ "balance").as[BigDecimal]
}