package actors.nest

import java.lang.Iterable

import akka.actor.{ActorLogging, Actor}
import com.firebase.client.{DataSnapshot, ValueEventListener, FirebaseError, Firebase}
import com.firebase.client.Firebase.AuthListener

/**
  * Created with IntelliJ IDEA.
  * User: Dmytro_Babichev
  * Date: 2/9/2016
  * Time: 10:54 AM
  */
class NestActor(nestToken: String, firebaseUrl: String) extends Actor with ActorLogging {

  val fireBase = new Firebase(firebaseUrl)

  fireBase.auth(nestToken, new AuthListener {

    def onAuthError(e: FirebaseError) {
      log.error(e.toException, "Firebase authentication error")
    }

    def onAuthSuccess(a: AnyRef) {
      log.info("Firebase successfully authenticated. {}", a)
      // when we've successfully authed, add a change listener to the whole tree
      fireBase.addValueEventListener(new ValueEventListener {
        def onDataChange(snapshot: DataSnapshot) {
          // when data changes we send our receive block an update
          self ! snapshot
        }
        def onCancelled(err: FirebaseError) {
          // on an err we should just bail out
          self ! err
        }
      })
    }

    def onAuthRevoked(e: FirebaseError) {
      log.error(e.toException, "Firebase authentication revoked")
    }
  })


  override def receive: Receive = {

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
}
