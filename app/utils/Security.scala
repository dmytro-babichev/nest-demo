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
  // Do not change this unless you understand the security issues behind timing attacks.
  // This method intentionally runs in constant time if the two strings have the same length.
  // If it didn't, it would be vulnerable to a timing attack.
  def safeEquals(a: String, b: String) = {
    if (a.length != b.length) {
      false
    } else {
      var equal = 0
      for (i <- Array.range(0, a.length)) {
        equal |= a(i) ^ b(i)
      }
      equal == 0
    }
  }

  def generateSessionId() = {
    sign("sessionId", UUID.randomUUID().toString)
  }

  def validateSignature(signedValue: String): Boolean = {
    val split = signedValue.split("-", 2)
    val pair = split.tail.mkString("")
    safeEquals(split.head, Crypto.sign(pair))
  }

  def sign(key: String, value: String) = {
    val pair = s"$key=$value"
    s"${Crypto.sign(pair)}-$pair"
  }

  def getValue(signature: String, key: String) = {
    if (validateSignature(signature)) {
      val split = signature.split("-", 2)
      val pair = split.tail.mkString("").split(key.concat("="), 2)
      Option(pair.tail.mkString(""))
    } else
      None
  }
}
