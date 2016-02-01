package ws

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import play.api.libs.json.{JsString, JsObject, JsValue, Json}
import ws.authorization.{AuthorizationActor, Authorized, Unauthorized}

import scala.concurrent.duration._

/**
  * Created by Hedgehog on 31/1/16.
  */
class WebSocketActor(out: ActorRef) extends Actor with ActorLogging {

  import context.dispatcher

  def receive = {
    case msg: JsValue =>
      val authorizationActor = context.actorOf(Props[AuthorizationActor])
      implicit val timeout = Timeout(5.seconds)
      val authorizationStatus = authorizationActor ? msg
      authorizationStatus map {
        case Authorized(message, sessionId) =>
          out ! Json.obj("message" -> message, "sessionId" -> sessionId, "status" -> 200)
        case Unauthorized(message) =>
          out ! Json.obj("message" -> message, "status" -> 403)
        case resp =>
          log.error("Unexpected response from authorization actor [{}]", resp)
          out ! Json.obj("message" -> "Internal server error", "status" -> 500)
      } recover {
        case e: Exception =>
          log.error(e, "Unable to process message [{}]", msg)
          out ! Json.obj("message" -> "Internal server error", "status" -> 500)
      }
  }
}

object WebSocketActor {
  def props(out: ActorRef) = Props(new WebSocketActor(out))
}