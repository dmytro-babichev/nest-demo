package ws.authorization

import java.util.UUID

import akka.actor.Actor
import play.api.Logger
import play.api.libs.Crypto
import play.api.libs.json.JsValue
import ws.authorization.AuthorizationActor.{generateSession, validateSession}

/**
  * Created by Hedgehog on 31/1/16.
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
                val playSession = generateSession()
                Authorized(s"Client with email: [$email] and password: [$password] has been authorized", playSession)
              case _ =>
                Logger.error(s"Client with email: [$email] has not been authorized. Reason: missing password.")
                Unauthorized
            }
          case _ =>
            Logger.error(s"Client has not been authorized. Reason: missing email.")
            Unauthorized
        }
      case Some(action) =>
        Logger.error(s"Client: [${emailOpt.getOrElse("unknown")}] is not authorized for action: [$action]")
        Unauthorized
      case _ =>
        Logger.error(s"Unable to authorize client: [${emailOpt.getOrElse("unknown")}]. Reason: empty action.")
        Unauthorized
    }
  }

  def isAuthorized(sessionId: String) = {
    if (validateSession(sessionId)) {
      Logger.debug(s"Client's existing sessionId: [$sessionId] is valid.")
      Authorized(s"Client is authorized", sessionId)
    } else {
      Logger.error(s"Client's sessionId: [$sessionId] is not valid.")
      Unauthorized
    }
  }
}

object AuthorizationActor {
  // Do not change this unless you understand the security issues behind timing attacks.
  // This method intentionally runs in constant time if the two strings have the same length.
  // If it didn't, it would be vulnerable to a timing attack.
  def safeEquals(a: String, b: String) = {
    if (a.length != b.length) {
      false
    } else {
      var equal = 0
      for (i <- Array.range(0, a.length)) {
        equal |= a(i) ^ b(i)
      }
      equal == 0
    }
  }

  def generateSession() = {
    val sessionId = s"sessionId=${UUID.randomUUID().toString}"
    val signedSessionId = Crypto.sign(sessionId)
    s"$signedSessionId-$sessionId"
  }

  def validateSession(signedSessionId: String): Boolean = {
    val splitted: Array[String] = signedSessionId.split("-", 2)
    val sessionId: String = splitted.tail.mkString("")
    safeEquals(splitted.head, Crypto.sign(sessionId))
  }
}

object LoginActions {
  val LOGIN = "login"
}

class AuthorizationStatus

case class Authorized(message: String, sessionId: String) extends AuthorizationStatus {}

case class Unauthorized(message: String = "Client is not authorized") extends AuthorizationStatus {}