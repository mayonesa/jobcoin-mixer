package controllers

import javax.inject.Inject
import play.api.mvc.{ AbstractController, ControllerComponents }
import services.JobcoinMixService
import services.auxiliaries.Address
import play.api.Logger
import play.api.libs.json.Json

class JobcoinMixerController @Inject() (cc: ControllerComponents, mixer: JobcoinMixService) extends AbstractController(cc) {

  def addresses = Action { request =>
    request.body.asJson.fold(badReq("Missing request JSON")) { json =>
      (json \ "proxyRecipients").asOpt[List[Address]].fold(badReq("'proxyRecipients' attribute missing from request JSON")) {
        proxyRecipients =>
          val effectivePR = proxyRecipients.map(_.trim).filterNot(_.isEmpty)
          if (effectivePR.isEmpty) badReq("must have proxy recipients")
          else ok(mixer.mix(effectivePR))
      }
    }
  }

  private def ok(depositAddress: Address) = Ok(Json.parse(s"""{ "depositAddress": "$depositAddress" }"""))
  private def errorJson(errMsg: String) = Json.parse(s"""{ "errors": [{ "message": "$errMsg" }] }""")
  private def badReq(msg: String) = {
    Logger.warn(msg)
    BadRequest(errorJson(msg))
  }
}