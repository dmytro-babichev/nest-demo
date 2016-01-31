package ws

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import play.api.libs.json.{JsValue, Json}
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
          out ! Json.parse(s"""{"message": "$message", "sessionId": "$sessionId"}""")
        case Unauthorized(message) => out ! s"{message: $message}"
        case _ =>
          log.error("Unexpected response from authorization actor")
          out ! "Unexpected response from authorization actor"
      } recover {
        case e: Exception =>
          log.error(e, "Unable to process message [{}]", msg.toString())
          out ! "Error has occurred while processing your request"
      }
  }
}

object WebSocketActor {
  def props(out: ActorRef) = Props(new WebSocketActor(out))
}