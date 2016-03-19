package actors

import actors.ActionType.NEST_OPERATION
import actors.WebSocketActor.defaultFirebaseUrl
import actors.authorization.{AuthorizationActor, Authorized, AuthorizedFor, Unauthorized}
import actors.nest.NestActor
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import org.apache.http.HttpStatus
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import utils.Constants.UNDEFINED
import utils.Security

/**
  * Created with IntelliJ IDEA.
  * User: Dmytro_Babichev
  * Date: 31/1/16.
  */
class WebSocketActor(out: ActorRef, conf: Configuration) extends Actor with ActorLogging {

  def safeHandle(initialMsg: JsValue, sessionId: String) = {
    val actionType: String = (initialMsg \ "actionType").asOpt[String].getOrElse(UNDEFINED)
    actionType match {
      case NEST_OPERATION =>
        val firebaseUrl = conf.getString("firebaseUrl").getOrElse(defaultFirebaseUrl)
        val nestActor = context.actorOf(NestActor.props(firebaseUrl))
        nestActor !(initialMsg, sessionId, out)
      case _ =>
        out ! Json.obj("message" -> "OK", "sessionId" -> sessionId, "status" -> HttpStatus.SC_OK)
    }
  }

  def receive = {
    case msg: JsValue =>
      val authorizationActor = context.actorOf(AuthorizationActor.props)
      authorizationActor ! msg
    case Authorized(message, email, sessionId) =>
      out ! Json.obj("message" -> message, "sessionId" -> sessionId, "status" -> HttpStatus.SC_OK, "email" -> Security.sign("email", email))
    case Unauthorized(message, email) =>
      out ! Json.obj("message" -> message, "email" -> email, "status" -> HttpStatus.SC_FORBIDDEN)
    case AuthorizedFor(initialMsg, sessionId) =>
      safeHandle(initialMsg, sessionId)
  }
}

object WebSocketActor {
  val defaultFirebaseUrl = "https://developer-api.nest.com"

  def props(out: ActorRef, conf: Configuration) = Props(new WebSocketActor(out, conf))
}

object ActionType {
  val NEST_OPERATION = "nest_operation"
}