package utils

import java.util.UUID

import play.api.libs.Crypto

/**
  * Created with IntelliJ IDEA.
  * User: Dmytro_Babichev
  * Date: 2/1/2016
  * Time: 11:39 AM
  */
object Authorization {
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
    val sessionId = s"sessionId=${UUID.randomUUID().toString}"
    val signedSessionId = Crypto.sign(sessionId)
    s"$signedSessionId-$sessionId"
  }

  def validateSessionId(signedSessionId: String): Boolean = {
    val splitted: Array[String] = signedSessionId.split("-", 2)
    val sessionId: String = splitted.tail.mkString("")
    safeEquals(splitted.head, Crypto.sign(sessionId))
  }
}
