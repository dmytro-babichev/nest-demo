package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import play.api.libs.json.{JsValue, Json}
import actors.authorization.{AuthorizationActor, Authorized, AuthorizedFor, Unauthorized}

/**
  * Created with IntelliJ IDEA.
  * User: Dmytro_Babichev
  * Date: 31/1/16.
  */
class WebSocketActor(out: ActorRef) extends Actor with ActorLogging {

  def receive = {
    case msg: JsValue =>
      val authorizationActor = context.actorOf(Props[AuthorizationActor])
      authorizationActor ! msg
    case Authorized(message, email, sessionId) =>
      out ! Json.obj("message" -> message, "sessionId" -> sessionId, "status" -> 200, "email" -> email)
    case Unauthorized(message, email) =>
      out ! Json.obj("message" -> message, "email" -> email, "status" -> 403)
    case AuthorizedFor(initialMsg, sessionId) =>
      out ! Json.obj("message" -> "OK", "sessionId" -> sessionId, "status" -> 200)
  }
}

object WebSocketActor {
  def props(out: ActorRef) = Props(new WebSocketActor(out))
}