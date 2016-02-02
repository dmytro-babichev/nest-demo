package dao

import java.sql.{Statement, ResultSet, SQLException}

import models.User
import play.api.db.DB

/**
  * Created with IntelliJ IDEA.
  * User: Dmytro_Babichev
  * Date: 02/2/16
  * Time: 11:17 PM
  */
class UserDAOImpl extends UserDAO {
  override def addUser(user: User) = {
    val existingUser = getUser(user.getEmail)
    existingUser match {
      case None =>
        DB.withConnection { connection =>
          val statement = connection.prepareStatement("INSERT INTO User (email, password) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)
          statement.setString(1, user.getEmail)
          statement.setString(2, user.getPassword)
          val rowsAffected = statement.executeUpdate()
          if (rowsAffected == 0) {
            throw new SQLException("Creating user failed, no rows affected")
          }
          val generatedKeys = statement.getGeneratedKeys
          try {
            if (generatedKeys.next()) {
              val generatedId: Long = generatedKeys.getLong(1)
              new User(generatedId, user.getEmail, user.getPassword)
            } else {
              throw new SQLException("Creating user failed, no ID obtained.")
            }
          } finally {
            generatedKeys.close()
          }
        }
      case Some =>
        throw new IllegalArgumentException(s"User with email: [${user.getEmail}] has already been added")
    }
    None[User]
  }

  override def getUser(email: String) = {
    None[User]
  }
}

trait UserDAO {

  def addUser(user: User): User

  def getUser(email: String): User
}
