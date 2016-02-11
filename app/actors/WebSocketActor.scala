package actors

import java.util.concurrent.TimeUnit

import actors.authorization.{AuthorizationActor, Authorized, AuthorizedFor, Unauthorized}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import dao.{GetUser, UserDAOImpl}
import models.User
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import utils.Security

import scala.concurrent.duration.Duration

/**
  * Created with IntelliJ IDEA.
  * User: Dmytro_Babichev
  * Date: 31/1/16.
  */
class WebSocketActor(out: ActorRef, conf: Configuration) extends Actor with ActorLogging {

  val UNDEFINED = "undefined"
  import context.dispatcher

  implicit val userOperationTimeout = new Timeout(Duration.create(3, TimeUnit.SECONDS))

  def safeHandle(initialMsg: JsValue, sessionId: String) = {
    val action: String = (initialMsg \ "action").asOpt[String].getOrElse("undefined")
    action match {
      case "generate_nest_link" =>
        val emailSignature: String = (initialMsg \ "email").asOpt[String].getOrElse("undefined")
        val email = Security.getValue(emailSignature, "email").getOrElse("undefined")
        if (email == UNDEFINED) {
          log.error("Client's blocked. Client's email is not valid. Email signature: {}. Session id: {}", emailSignature, sessionId)
          out ! Json.obj("message" -> "Forbidden", "email" -> email, "status" -> 403)
        } else {
          val userDao: ActorRef = context.actorOf(Props[UserDAOImpl])
          val futureUser = userDao ? GetUser(email)
          futureUser.map {
            case Some(user: User) =>
              out ! Json.obj("message" -> "OK", "sessionId" -> sessionId, "status" -> 200,
                "nest_link" -> s"https://home.nest.com/login/oauth2?client_id=${user.productId}&state=STATE")
            case _ =>
              Unauthorized(s"User with email: [$email] is not registered", email)
          }
        }
      case "web_cam" =>
        val firebaseUrl = conf.getString("firebaseUrl").getOrElse(WebSocketActor.defaultFirebaseUrl)
        //        context.actorOf(Props(new NestActor(nestCode, firebaseUrl)))
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
      out ! Json.obj("message" -> message, "sessionId" -> sessionId, "status" -> 200, "email" -> Security.sign("email", email))
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