package actors

import actors.nest.NestActor
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import actors.authorization.{AuthorizationActor, Authorized, AuthorizedFor, Unauthorized}

/**
  * Created with IntelliJ IDEA.
  * User: Dmytro_Babichev
  * Date: 31/1/16.
  */
class WebSocketActor (out: ActorRef, conf: Configuration) extends Actor with ActorLogging {

  def safeHandle(initialMsg: JsValue, sessionId: String) = {
    val action: String = (initialMsg \ "action").asOpt[String].getOrElse("undefined")
    val nestKey: String = (initialMsg \ "nestKey").asOpt[String].getOrElse("c.shyTJWo6g9rnsBRzmntao9SB0qZ2TlT8prcvwi5DB5xOrwuUo6UzQktNNvFgsgU1QlqCq1J8QYA77790z41lnrE53NneK5j3ZJjwLWknckEBCpB8ejBOrAyh6kNa53a23nP7Ydfhe6Gfm0nf")
    action match {
      case "web_cam" =>
        val firebaseUrl = conf.getString("firebaseUrl").getOrElse(WebSocketActor.defaultFirebaseUrl)
        context.actorOf(Props(new NestActor(nestKey, firebaseUrl)))
        out ! Json.obj("message" -> "OK", "sessionId" -> sessionId, "status" -> 200)
      case _ =>
        out ! Json.obj("message" -> "OK", "sessionId" -> sessionId, "status" -> 200)
    }
  }

  def receive = {
    case msg: JsValue =>
      val authorizationActor = context.actorOf(Props[AuthorizationActor])
      authorizationActor ! msg
    case Authorized(message, email, sessionId) =>
      out ! Json.obj("message" -> message, "sessionId" -> sessionId, "status" -> 200, "email" -> email)
    case Unauthorized(message, email) =>
      out ! Json.obj("message" -> message, "email" -> email, "status" -> 403)
    case AuthorizedFor(initialMsg, sessionId) =>
      safeHandle(initialMsg, sessionId)
  }
}

object WebSocketActor {
  val defaultFirebaseUrl = "https://developer-api.nest.com"
  def props(out: ActorRef, conf: Configuration) = Props(new WebSocketActor(out, conf))
}