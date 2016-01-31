package ws

import akka.actor.{Props, Actor, ActorRef}

/**
  * Created by Hedgehog on 31/1/16.
  */
class WebSocketActor(out: ActorRef) extends Actor {
  def receive = {
    case msg: String =>
      println(msg)
      out ! ("I received your message: " + msg)
  }
}

object WebSocketActor {
  def props(out: ActorRef) = Props(new WebSocketActor(out))
}