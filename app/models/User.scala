package models

/**
  * Created with IntelliJ IDEA.
  * User: Dmytro_Babichev
  * Date: 02/2/16
  * Time: 11:29 PM
  */
class User(id: Long, email: String, password: String) {
  def getId: Long = id
  def getEmail: String = email
  def getPassword: String = password
}
