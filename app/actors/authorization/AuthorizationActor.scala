package actors.authorization

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import dao.{AddUser, GetUser, UserDAOImpl}
import models.User
import play.api.libs.json.JsValue
import util.Constants.UNDEFINED
import utils.Security

import scala.concurrent.Future
import scala.concurrent.duration.Duration

/**
  * Created with IntelliJ IDEA.
  * User: Dmytro_Babichev
  * Date: 31/1/16.
  */
class AuthorizationActor extends Actor with ActorLogging {

  import context.dispatcher

  implicit val userOperationTimeout = new Timeout(Duration.create(3, TimeUnit.SECONDS))

  override def receive = {
    case msg: JsValue =>
      val sessionIdOpt = (msg \ "sessionId").asOpt[String]
      sessionIdOpt match {
        case None =>
          authorize(msg, sender())
        case Some(sessionId) =>
          sender() ! isAuthorized(sessionId, msg)
      }
  }

  def authorize(msg: JsValue, sender: ActorRef) = {
    val action: String = (msg \ "action").asOpt[String].getOrElse(UNDEFINED)
    val email: String = (msg \ "email").asOpt[String].getOrElse(UNDEFINED)
    val password: String = (msg \ "password").asOpt[String].getOrElse(UNDEFINED)
    if (email == UNDEFINED || password == UNDEFINED) {
      log.error("Unable to authorize client: [{}]. Reason: wrong email or password: [{}}].", email, password)
      sender ! Unauthorized("Wrong email or password", email)
    } else {
      action match {
        case LoginActions.LOGIN =>
          val user: Future[AuthorizationStatus] = checkUser(email, password)
          pipe(user).to(sender)
        case LoginActions.REGISTER =>
          val productId: String = (msg \ "productId").asOpt[String].getOrElse(UNDEFINED)
          val productSecret: String = (msg \ "productSecret").asOpt[String].getOrElse(UNDEFINED)
          if (productId == UNDEFINED || productSecret == UNDEFINED) {
            log.error("Unable to register client: [{}]. Reason: empty product id or product secret.", email)
            sender ! Unauthorized("Empty product id or product secret", email)
          } else {
            val user: Future[AuthorizationStatus] = registerUser(email, password, productId, productSecret)
            pipe(user).to(sender)
          }
        case _ =>
          log.error("Unable to authorize client: [{}]. Reason: wrong action: [{}].", email, action)
          sender ! Unauthorized(email = email)
      }
    }
  }

  def isAuthorized(sessionId: String, msg: JsValue) = {
    if (Security.validateSignature(sessionId)) {
      log.debug("Client's existing sessionId: [{}] is valid.", sessionId)
      AuthorizedFor(msg, sessionId)
    } else {
      log.error("Client's sessionId: [{}] is not valid.", sessionId)
      Unauthorized()
    }
  }

  def checkUser(email: String, password: String) = {
    log.info("Client tries to log in with email: [{}] and password: [{}]", email, password)
    val userDao = context.actorOf(Props[UserDAOImpl])
    val futureUser = userDao ? GetUser(email)
    futureUser.map {
      case Some(user: User) =>
        if (user.password == password) {
          log.info("Client has been authorized successfully with email: [{}] and password: [{}]", email, password)
          val sessionId = Security.generateSessionId()
          Authorized(s"Client with email: [$email] and password: [$password] has been authorized", email, sessionId)
        } else {
          Unauthorized(s"Client with email: [$email] and password: [$password] has not been authorized. Wrong email or password", email)
        }
      case _ =>
        Unauthorized(s"User with email: [$email] is not registered", email = email)
    }.recover {
      case e: Exception =>
        log.error(e, "Unable to check user by email: [{}] and password: [{}]", email, password)
        Unauthorized(s"Internal server error", email = email)
    }
  }

  def registerUser(email: String, password: String, productId: String, productSecret: String) = {
    log.info("Client tries to register with email: [{}] and password: [{}]", email, password)
    val userDao = context.actorOf(Props[UserDAOImpl])
    val futureUser = userDao ? AddUser(email, password, productId, productSecret)
    futureUser.map {
      case Some(user: User) =>
        log.info("Client has been registered successfully with email: [{}] and password: [{}]", email, password)
        val sessionId = Security.generateSessionId()
        Authorized(s"Client with email: [$email] and password: [$password] has been registered", email, sessionId)
      case _ =>
        Unauthorized("Client has not been registered", email = email)
    }.recover {
      case e: Exception =>
        log.error(e, "Unable to register user with email: [{}], password: [{}], product id: [{}] and product secret: [{}]",
          email, password, productId, productSecret)
        Unauthorized(s"Internal server error", email = email)
    }
  }
}