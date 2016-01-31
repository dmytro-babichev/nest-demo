package ws

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import play.api.Logger
import play.api.libs.json.JsValue
import ws.WebSocketActor.login

/**
  * Created by Hedgehog on 31/1/16.
  */
class WebSocketActor(out: ActorRef) extends Actor with ActorLogging {
  def receive = {
    case msg: JsValue =>
      val sessionIdOpt = (msg \ "sessionId").asOpt[String]
      sessionIdOpt match {
        case None =>
          login(msg)
        case Some(sessionId) =>
          println(s"Session id: $sessionId")
      }
  }
}

object WebSocketActor {
  def props(out: ActorRef) = Props(new WebSocketActor(out))

  def login(msg: JsValue) = {
    val actionOpt: Option[String] = (msg \ "action").asOpt[String]
    actionOpt match {
      case Some(LoginActions.LOGIN) =>
        val emailOpt = (msg \ "email").asOpt[String]
        val passwordOpt = (msg \ "password").asOpt[String]
        emailOpt match {
          case Some(email) =>
            passwordOpt match {
              case Some(password) =>
                Logger.info(s"Client tries to log in with email: $email and password: $password")
              case _ =>
                Logger.error("Missing password")
            }
          case _ =>
            Logger.error("Missing email")
        }
      case Some(action) =>
        Logger.error(s"Not authorized for action: $action")
      case _ =>
        Logger.error("Unable to authorize client: empty action.")
    }
  }
}

object LoginActions {
  val LOGIN = "login"
}