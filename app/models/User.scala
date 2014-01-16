package models

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._

case class User (
	id : Int,
	email : String,
	password : String
)

object User {
	def parseRow(data : SqlRow) : User = {
		return User(data[Int]("id"), data[String]("email"), data[String]("password"));
	}

	def getById(id : Int) : Option[User] = {
		DB.withConnection { implicit connection =>
			val query = SQL("SELECT u.id, u.email, u.password FROM user u WHERE id={id}").on("id" -> id).apply().headOption;
			return query match {
				case Some(data) => Some(parseRow(data))
				case None => None
			}
		}
	}

	def getByEmail(email : String) : Option[User] = {
		DB.withConnection { implicit connection =>
			val query = SQL("SELECT u.id, u.email, u.password FROM user u WHERE email={email}").on("email" -> email).apply().headOption;
			return query match {
				case Some(data) => Some(parseRow(data))
				case None => None
			}
		}
	}

	def authenticate(email : String, password : String) : Option[User] = {
		DB.withConnection { implicit connection =>
			val query = SQL("SELECT u.id, u.email, u.password FROM user u WHERE email={email} AND password={password}").on("email" -> email, "password" -> password).apply().headOption;
			return query match {
				case Some(data) => Some(parseRow(data))
				case None => None
			}
		}
	}

	def save(user : User) : User = {
		if (getById(user.id).isEmpty == true) {
		} else {
		}
		return user;
	}
}