package dao

import dao.UserDAOImpl.users
import models.{User, Users}
import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.H2Driver.api._
import slick.driver.JdbcProfile
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created with IntelliJ IDEA.
  * User: Dmytro_Babichev
  * Date: 02/2/16
  * Time: 11:17 PM
  */
class UserDAOImpl extends UserDAO {

  override def addUser(email: String, password: String) = {
    val user = User(None, email, password)
    val futureInsert = UserDAOImpl.dbConfig.db.run(
      users.filter(_.email === email).exists.result.flatMap(exists =>
        if (exists) {
          users returning users.map(_.id) += user
        } else {
          DBIO.failed(new IllegalStateException(s"User with email: [$email] already exists"))
        }
      )
    )
    futureInsert.map(id =>
      User(Some(id), email, password)
    )
  }

  override def getUser(email: String): Future[Option[User]] = {
    val futureSelect: Future[Seq[User]] = UserDAOImpl.dbConfig.db.run(
      users.filter(_.email === email).take(1).result
    )
    futureSelect.map(users => Option(users.head))
  }
}

object UserDAOImpl {
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  val users = TableQuery[Users]
}

trait UserDAO {

  def addUser(email: String, password: String): Future[User]

  def getUser(email: String): Future[Option[User]]
}
