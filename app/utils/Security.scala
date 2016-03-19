package utils

import java.util.UUID

import play.api.libs.Crypto

/**
  * Created with IntelliJ IDEA.
  * User: Dmytro_Babichev
  * Date: 2/1/2016
  * Time: 11:39 AM
  */
object Security {

  def generateSessionId() = {
    sign("sessionId", UUID.randomUUID().toString)
  }

  def validateSignature(signedValue: String): Boolean = {
    Crypto.extractSignedToken(signedValue) match {
      case Some(_) => true
      case _ => false
    }
  }

  def sign(key: String, value: String) = {
    val pair = s"$key=$value"
    Crypto.signToken(pair)
  }

  def getValue(signature: String, key: String): Option[String] = {
    Crypto.extractSignedToken(signature) match {
      case Some(token) => Option(token.split(key + "=", 2).tail.mkString(""))
      case _ => None
    }
  }
}
