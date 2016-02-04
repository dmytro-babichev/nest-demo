package ws.authorization

import akka.actor.Actor
import play.api.Logger
import play.api.libs.json.JsValue

/**
  * Created with IntelliJ IDEA.
  * User: Dmytro_Babichev
  * Date: 31/1/16.
  */
class AuthorizationActor extends Actor {

  override def receive = {
    case msg: JsValue =>
      val sessionIdOpt = (msg \ "sessionId").asOpt[String]
      sessionIdOpt match {
        case None =>
          sender ! authorize(msg)
        case Some(sessionId) =>
          sender ! isAuthorized(sessionId)
      }
  }

  def authorize(msg: JsValue) = {
    val actionOpt: Option[String] = (msg \ "action").asOpt[String]
    val emailOpt = (msg \ "email").asOpt[String]
    val passwordOpt = (msg \ "password").asOpt[String]
    actionOpt match {
      case Some(LoginActions.LOGIN) =>
        emailOpt match {
          case Some(email) =>
            passwordOpt match {
              case Some(password) =>
                Logger.info(s"Client tries to log in with email: [$email] and password: [$password]")
                val sessionId = Authorization.generateSessionId()
                Authorized(s"Client with email: [$email] and password: [$password] has been authorized", sessionId)
              case _ =>
                Logger.error(s"Client with email: [$email] has not been authorized. Reason: missing password.")
                Unauthorized()
            }
          case _ =>
            Logger.error(s"Client has not been authorized. Reason: missing email.")
            Unauthorized()
        }
      case Some(action) =>
        Logger.error(s"Client: [${emailOpt.getOrElse("unknown")}] is not authorized for action: [$action]")
        Unauthorized()
      case _ =>
        Logger.error(s"Unable to authorize client: [${emailOpt.getOrElse("unknown")}]. Reason: empty action.")
        Unauthorized()
    }
  }

  def isAuthorized(sessionId: String) = {
    if (Authorization.validateSessionId(sessionId)) {
      Logger.debug(s"Client's existing sessionId: [$sessionId] is valid.")
      Authorized(s"Client is authorized", sessionId)
    } else {
      Logger.error(s"Client's sessionId: [$sessionId] is not valid.")
      Unauthorized()
    }
  }
}

object LoginActions {
  val LOGIN = "login"
}

class AuthorizationStatus(message: String)

case class Authorized(message: String, sessionId: String) extends AuthorizationStatus(message) {}

case class Unauthorized(message: String = "Client is not authorized") extends AuthorizationStatus(message) {
  override def toString: String = message
}