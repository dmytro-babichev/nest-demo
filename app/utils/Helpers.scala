package utils

import play.api.libs.json.JsValue
import Constants.UNDEFINED

/**
  * Created with IntelliJ IDEA.
  * User: Dmytro_Babichev
  * Date: 19/3/16
  * Time: 9:16 PM
  */
object Helpers {
  def extractValue(msg: JsValue, key: String): String = {
    (msg \ key).asOpt[String].getOrElse(UNDEFINED)
  }

  def extractSignedValue(msg: JsValue, key: String): String = {
    val value = extractValue(msg, key)
    Security.getValue(value, key).getOrElse(UNDEFINED)
  }
}
