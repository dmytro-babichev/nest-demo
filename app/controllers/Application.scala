package controllers

import play.api.Play.current
import play.api.libs.json.JsValue
import play.api.mvc._
import ws.WebSocketActor

class Application extends Controller {

  def index = Action {
    Ok(views.html.index()).withSession()
  }

  def ws = WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
    WebSocketActor.props(out)
  }
}
