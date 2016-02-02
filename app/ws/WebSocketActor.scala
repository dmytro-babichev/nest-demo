package ws

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import play.api.libs.json.{JsString, JsObject, JsValue, Json}
import ws.authorization.{AuthorizationActor, Authorized, Unauthorized}

import scala.concurrent.duration._

/**
  * Created with IntelliJ IDEA.
  * User: Dmytro_Babichev
  * Date: 31/1/16.
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
          val email = (msg \ "email").as[String]
          out ! Json.obj("message" -> message, "sessionId" -> sessionId, "status" -> 200, "email" -> email)
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