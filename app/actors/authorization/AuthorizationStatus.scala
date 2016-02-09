package actors.authorization

import play.api.libs.json.JsValue

/**
  * Created with IntelliJ IDEA.
  * User: Dmytro_Babichev
  * Date: 2/9/2016
  * Time: 10:49 AM
  */
class AuthorizationStatus(email: String, message: String)

case class Authorized(message: String, email: String, sessionId: String) extends AuthorizationStatus(message, email) {}

case class AuthorizedFor(msg: JsValue, sessionId: String) {}

case class Unauthorized(message: String = "Client is not authorized", email: String = "") extends AuthorizationStatus(message, email) {
  override def toString: String = message
}