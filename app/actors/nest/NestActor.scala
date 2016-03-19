package actors.nest

import java.util.concurrent.TimeUnit

import actors.nest.NestActor.{GENERATE_ACCESS_TOKEN, GENERATE_NEST_LINK}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.firebase.client.{DataSnapshot, Firebase}
import dao.{GetUser, UserDAOImpl}
import models.User
import org.apache.http.HttpStatus
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WS
import play.api.libs.ws.ning.NingWSClient
import utils.Constants.UNDEFINED
import utils.Helpers.{extractSignedValue, extractValue}

import scala.concurrent.duration.Duration

/**
  * Created with IntelliJ IDEA.
  * User: Dmytro_Babichev
  * Date: 2/9/2016
  * Time: 10:54 AM
  */
class NestActor(firebaseUrl: String) extends Actor with ActorLogging {

  val fireBase = new Firebase(firebaseUrl)

  import context.dispatcher

  implicit val userOperationTimeout = new Timeout(Duration.create(3, TimeUnit.SECONDS))

  //  fireBase.auth(nestToken, new AuthListener {
  //
  //    def onAuthError(e: FirebaseError) {
  //      log.error(e.toException, "Firebase authentication error")
  //    }
  //
  //    def onAuthSuccess(a: AnyRef) {
  //      log.info("Firebase successfully authenticated. {}", a)
  //      // when we've successfully authed, add a change listener to the whole tree
  //      fireBase.addValueEventListener(new ValueEventListener {
  //        def onDataChange(snapshot: DataSnapshot) {
  //          // when data changes we send our receive block an update
  //          self ! snapshot
  //        }
  //
  //        def onCancelled(err: FirebaseError) {
  //          // on an err we should just bail out
  //          self ! err
  //        }
  //      })
  //    }
  //
  //    def onAuthRevoked(e: FirebaseError) {
  //      log.error(e.toException, "Firebase authentication revoked")
  //    }
  //  })

  override def receive: Receive = {
    case (msg: JsValue, sessionId: String, out: ActorRef) =>
      val action = extractValue(msg, "action")
      val email = extractSignedValue(msg, "email")
      val code = extractSignedValue(msg, "code")
      if (email == UNDEFINED) {
        log.error("Client's blocked. Client's email is not valid. Email signature: {}. Session id: {}", extractValue(msg, "email"), sessionId)
        out ! Json.obj("message" -> "Forbidden", "email" -> email, "status" -> HttpStatus.SC_FORBIDDEN)
      } else {
        val accessHandler = generateNestLink(sessionId, email, out) orElse generateAccessToken(sessionId, email, code, out)
        accessHandler(action)
      }
  }

  def handleSnapshots: Receive = {
    case snapshot: DataSnapshot =>
      import scala.collection.JavaConversions._
      val cameras = snapshot.child("devices").child("cameras")
      if (cameras != null && cameras.getChildren != null) {
        cameras.getChildren.foreach { camera =>
          val deviceId = camera.child("device_id").getValue().toString
          val nameLong = camera.child("name_long").getValue().toString
          val isOnline = camera.child("is_online").getValue().toString.toBoolean
          val isStreaming = camera.child("is_streaming").getValue().toString.toBoolean
          val isAudioInputEnabled = camera.child("is_audio_input_enabled").getValue().toString.toBoolean
          val webUrl = camera.child("web_url").getValue().toString
          println(s"deviceId: $deviceId, name: $nameLong, isOnline: $isOnline, isStreaming: $isStreaming, isAudioInputEnabled: $isAudioInputEnabled, webUrl: $webUrl")
        }
      }
      log.info("Data snapshot has been received from firebase: {}", snapshot)
  }

  def generateNestLink(sessionId: String, email: String, out: ActorRef): Receive = {
    case GENERATE_NEST_LINK =>
      getUser(email).map {
        case Some(user: User) =>
          out ! Json.obj("message" -> "OK", "sessionId" -> sessionId, "status" -> HttpStatus.SC_OK,
            "nest_link" -> s"https://home.nest.com/login/oauth2?client_id=${user.productId}&state=STATE",
            "action" -> GENERATE_NEST_LINK)
      }.recover {
        case e: Exception =>
          log.error(e, "Unable to get user by email: [{}]. Session id: [{}], action: [{}]", email, sessionId, GENERATE_NEST_LINK)
          out ! Json.obj("message" -> "Internal server error. ${e.getMessage}", "sessionId" -> sessionId,
            "status" -> HttpStatus.SC_INTERNAL_SERVER_ERROR, "action" -> GENERATE_NEST_LINK)
      }
  }

  def generateAccessToken(sessionId: String, email: String, code: String, out: ActorRef): Receive = {
    case GENERATE_ACCESS_TOKEN =>
      if (code == UNDEFINED) {
        log.error("Client's nest code is empty. Email: [{}]. Session id: [{}], action: [{}]", email, sessionId, GENERATE_ACCESS_TOKEN)
        out ! Json.obj("message" -> "Bad nest code. Please try again.", "email" -> email, "status" -> HttpStatus.SC_BAD_REQUEST,
          "action" -> GENERATE_ACCESS_TOKEN)
      } else {
        getUser(email).map {
          case Some(user: User) =>
            implicit val sslClient = NingWSClient()
            val body = Map(
              "client_id" -> Seq(user.productId),
              "code" -> Seq(code),
              "client_secret" -> Seq(user.productSecret),
              "grant_type" -> Seq("authorization_code")
            )
            WS.clientUrl("https://api.home.nest.com/oauth2/access_token").post(body)
              .map { wsResponse =>
                sslClient.close()
                log.info("Request from nest: {}", wsResponse.body)
                out ! Json.obj("message" -> "OK", "sessionId" -> sessionId, "status" -> HttpStatus.SC_OK,
                  "action" -> GENERATE_ACCESS_TOKEN)
              }.recover {
              case e: Exception =>
                sslClient.close()
                log.error(e, "Unable to get nest security token for email: [{}]. Session id: [{}], action: [{}], " +
                  "nest code: [{}]", email, sessionId, GENERATE_ACCESS_TOKEN, code)
                out ! Json.obj("message" -> s"Internal server error. ${e.getMessage}", "sessionId" -> sessionId,
                  "status" -> HttpStatus.SC_INTERNAL_SERVER_ERROR, "action" -> GENERATE_ACCESS_TOKEN)
            }
        }.recover {
          case e: Exception =>
            log.error(e, "Unable to get user by email: [{}]. Session id: [{}], action: [{}], nest code: [{}]", email, sessionId, GENERATE_ACCESS_TOKEN, code)
            out ! Json.obj("message" -> "Internal server error. ${e.getMessage}", "sessionId" -> sessionId,
              "status" -> HttpStatus.SC_INTERNAL_SERVER_ERROR, "action" -> GENERATE_ACCESS_TOKEN)
        }
      }
  }

  def getUser(email: String) = {
    val userDao = context.actorOf(UserDAOImpl.props)
    userDao ? GetUser(email)
  }
}

object NestActor {
  val GENERATE_ACCESS_TOKEN = "generate_access_token"
  val GENERATE_NEST_LINK = "generate_nest_link"

  def props(firebaseUrl: String) = Props(new NestActor(firebaseUrl))
}