package ws.authorization

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import dao.{AddUser, GetUser, UserDAOImpl}
import models.User
import play.api.Logger
import play.api.libs.json.JsValue

import scala.concurrent.Future
import scala.concurrent.duration.Duration

/**
  * Created with IntelliJ IDEA.
  * User: Dmytro_Babichev
  * Date: 31/1/16.
  */
class AuthorizationActor extends Actor {

  import context.dispatcher

  val UNDEFINED = "undefined"
  implicit val timeout = new Timeout(Duration.create(5, "seconds"))

  override def receive = {
    case msg: JsValue =>
      val sessionIdOpt = (msg \ "sessionId").asOpt[String]
      sessionIdOpt match {
        case None =>
          authorize(msg, sender())
        case Some(sessionId) =>
          sender ! isAuthorized(sessionId, msg)
      }
  }

  def authorize(msg: JsValue, sender: ActorRef) = {
    val action: String = (msg \ "action").asOpt[String].getOrElse(UNDEFINED)
    val email: String = (msg \ "email").asOpt[String].getOrElse(UNDEFINED)
    val password: String = (msg \ "password").asOpt[String].getOrElse(UNDEFINED)
    if (email == UNDEFINED || password == UNDEFINED) {
      Logger.error(s"Unable to authorize client: [$email]. Reason: wrong email or password: [$password].")
      sender ! Unauthorized("Wrong email or password", email)
    } else {
      action match {
        case LoginActions.LOGIN =>
          val user: Future[AuthorizationStatus] = checkUser(email, password)
          pipe(user).to(sender)
        case LoginActions.REGISTER =>
          val user: Future[AuthorizationStatus] = checkUser(email, password)
          pipe(user).to(sender)
        case _ =>
          Logger.error(s"Unable to authorize client: [$email]. Reason: wrong action: [$action].")
          sender ! Unauthorized(email = email)
      }
    }
  }

  def isAuthorized(sessionId: String, msg: JsValue) = {
    if (Authorization.validateSessionId(sessionId)) {
      Logger.debug(s"Client's existing sessionId: [$sessionId] is valid.")
      AuthorizedFor(msg, sessionId)
    } else {
      Logger.error(s"Client's sessionId: [$sessionId] is not valid.")
      Unauthorized()
    }
  }

  def checkUser(email: String, password: String) = {
    Logger.info(s"Client tries to log in with email: [$email] and password: [$password]")
    val userDao = context.actorOf(Props[UserDAOImpl])
    val futureUser = userDao ? GetUser(email)
    futureUser.map {
      case Some(user: User) =>
        if (user.password == password) {
          val sessionId = Authorization.generateSessionId()
          Authorized(s"Client with email: [$email] and password: [$password] has been authorized", email, sessionId)
        } else {
          Unauthorized(s"Client with email: [$email] and password: [$password] has not been authorized. Wrong email or password", email)
        }
      case _ =>
        Unauthorized(email = email)
    }
  }

  def registerUser(email: String, password: String) = {
    Logger.info(s"Client tries to register with email: [$email] and password: [$password]")
    val userDao = context.actorOf(Props[UserDAOImpl])
    val futureUser = userDao ? AddUser(email, password)
    futureUser.map {
      case Some(user: User) =>
        val sessionId = Authorization.generateSessionId()
        Authorized(s"Client with email: [$email] and password: [$password] has been registered", email, sessionId)
      case _ =>
        Unauthorized("Client has not been registered", email = email)
    }
  }
}

object LoginActions {
  val LOGIN = "login"
  val REGISTER = "register"
}

class AuthorizationStatus(email: String, message: String)

case class Authorized(message: String, email: String, sessionId: String) extends AuthorizationStatus(message, email) {}

case class AuthorizedFor(msg: JsValue, sessionId: String) {}

case class Unauthorized(message: String = "Client is not authorized", email: String = "") extends AuthorizationStatus(message, email) {
  override def toString: String = message
}