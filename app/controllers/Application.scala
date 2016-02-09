package controllers

import actors.WebSocketActor
import play.api.Play.current
import play.api.libs.json.JsValue
import play.api.mvc._

class Application extends Controller {

  def index = Action {
    Ok(views.html.index()).withSession()
  }

  def ws = WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
    WebSocketActor.props(out)
  }
}
