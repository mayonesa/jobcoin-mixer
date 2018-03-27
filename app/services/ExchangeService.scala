package services

import javax.inject.Inject
import play.api.libs.ws.WSClient
import scala.concurrent.{ ExecutionContext, Future }
import play.api.libs.json.{ Json, JsValue }
import play.api.http.Status.OK
import services.auxiliaries.{ Address, Jobcoin }
import concurrent.duration._
import play.api.Logger

class ExchangeService @Inject() (ws: WSClient)(implicit ec: ExecutionContext) {
  private val transactionsUrl = "http://jobcoin.gemini.com/vendetta/api/transactions"

  def balance(address: Address): Future[Option[Jobcoin]] =
    ws.url(addressInfoUrl(address)).get.map { resp =>
      if (resp.status == OK) Some(balance(resp.json))
      else None
    }.fallbackTo(Future(None))

  def transfer(from: Address, to: Address, amt: Jobcoin, cb: Unit => Unit = _ => ()): Unit = {
    val requestJson = Json.parse(s"""{ "amount": $amt, "fromAddress": "$from", "toAddress": "$to" }""")
    Logger.debug(s"initiating transfer from $from to $to for $amt")
    ws.url(transactionsUrl).post(requestJson).foreach { resp =>
      val logMsg = s"transfer from $from to $to for $amt jobcoins"
      val status = resp.status
      if (status == OK) {
        Logger.info(logMsg + " succeeded")
        cb(())
      } else Logger.error(s"$logMsg failed (status: $status): ${resp.body}")
    }
  }

  private def addressInfoUrl(address: Address) = "http://jobcoin.gemini.com/vendetta/api/addresses/" + address

  private def balance(addressInfoJson: JsValue) = (addressInfoJson \ "balance").as[BigDecimal]
}