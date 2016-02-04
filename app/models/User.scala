package models

import slick.driver.H2Driver.api._

/**
  * Created with IntelliJ IDEA.
  * User: Dmytro_Babichev
  * Date: 02/2/16
  * Time: 11:29 PM
  */

case class User(id: Option[Long], email: String, password: String)

class Users(tag: Tag) extends Table[User](tag, "User") {

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def email = column[String]("email")

  def password = column[String]("password")

  def * = (id.?, email, password) <> (User.tupled, User.unapply)
}
