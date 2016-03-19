package controllers

import javax.inject.Inject

import actors.WebSocketActor
import play.api.Configuration
import play.api.Play.current
import play.api.libs.json.JsValue
import play.api.mvc._

class Application @Inject() (config: Configuration) extends Controller {

  def index(code: String) = Action {
    Ok(views.html.index(code)).withSession()
  }

  def nestCode(state: String, code: String) = Action {
    Redirect(routes.Application.index(code)).withSession()
  }

  def ws = WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
    WebSocketActor.props(out, config)
  }
}
