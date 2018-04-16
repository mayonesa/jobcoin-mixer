package controllers

import javax.inject.Inject
import play.api
import api.mvc.{ AbstractController, ControllerComponents }
import api.Logger
import api.libs.json.Json
import concurrent.{ ExecutionContext, Future }
import services.JobcoinMixService
import models.Address

class JobcoinMixerController @Inject() (cc: ControllerComponents, mixer: JobcoinMixService)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def addresses() = Action.async { request =>
    request.body.asJson.fold(badReqFut("Missing request JSON")) { json =>
      (json \ "proxyRecipients").asOpt[Vector[Address]].fold {
        badReqFut("'proxyRecipients' attribute missing from request JSON")
      } { fantasmRecipients =>
        val effectiveFR = fantasmRecipients.map(_.trim).filterNot(_.isEmpty)
        if (effectiveFR.isEmpty) badReqFut("must have proxy recipients")
        else mixer(effectiveFR).map(ok).recover {
          case e => badReq(e.getMessage)
        }
      }
    }
  }

  private def ok(depositAddress: Address) = Ok(Json.parse(s"""{ "depositAddress": "$depositAddress" }"""))
  private def errorJson(errMsg: String) = Json.parse(s"""{ "errors": [{ "message": "$errMsg" }] }""")
  private def badReq(msg: String) = {
    Logger.warn(msg)
    BadRequest(errorJson(msg))
  }
  private def badReqFut(msg: String) = Future(badReq(msg))
}