package models

import play.api.db._
import play.api.Play.current
import play.Logger
import play.Logger._
import anorm._
import anorm.SqlParser._

case class User (
	id : Long,
	email : String,
	password : String,
	salt : String,
	name : String,
	active : Int,
	token : String,
	score : Int = 0
)

object User {

	def parseRowScore(data : SqlRow) : User = {
		val user : User = User(data[Long]("id"), data[String]("email"), data[String]("password"), data[String]("salt"), data[String]("name"), data[Int]("active"), data[String]("token"), data[java.math.BigDecimal]("score").intValueExact());
		Logger.info("User fetch: " + user.toString);
		return user;
	}

	def parseRow(data : SqlRow) : User = {
		val user : User = User(data[Long]("id"), data[String]("email"), data[String]("password"), data[String]("salt"), data[String]("name"), data[Int]("active"), data[String]("token"));
		Logger.info("User fetch: " + user.toString);
		return user;
	}

	def getTopByScore() : List[User] = {
		DB.withConnection { implicit connection =>
			val query = SQL("""
				SELECT u.id, MAX(u.name) AS name, MAX(u.email) AS email, MAX(u.password) AS password, MAX(u.salt) AS salt, MAX(u.active) AS active, MAX(u.token) AS token, SUM(e.score) AS score 
				FROM user u JOIN (SELECT uid, tid, MAX(passed) AS score FROM eval GROUP BY tid, uid) AS e ON u.id = e.uid GROUP BY u.id LIMIT 30
			""");
			query().map(row => parseRowScore(row)).toList
		}
	}

	def getById(id : Long) : Option[User] = {
		DB.withConnection { implicit connection =>
			val query = SQL("SELECT u.id, u.email, u.password, u.salt, u.name, u.token, u.active FROM user u WHERE id={id}").on("id" -> id).apply().headOption;
			return query match {
				case Some(data) => Some(parseRow(data))
				case None => None
			}
		}
	}

	def getByToken(token : String) : Option[User] = {
		DB.withConnection { implicit connection =>
			val query = SQL("SELECT u.id, u.email, u.password, u.salt, u.name, u.token, u.active FROM user u WHERE token={token}").on("token" -> token).apply().headOption;
			return query match {
				case Some(data) => Some(parseRow(data))
				case None => None
			}
		}
	}

	def getByEmail(email : String) : Option[User] = {
		DB.withConnection { implicit connection =>
			val query = SQL("SELECT u.id, u.email, u.password, u.salt, u.name, u.token, u.active FROM user u WHERE email={email}").on("email" -> email).apply().headOption;
			return query match {
				case Some(data) => Some(parseRow(data))
				case None => None
			}
		}
	}

	def authenticate(email : String, password : String) : Option[User] = {
		DB.withConnection { implicit connection =>
			val user = getByEmail(email)
			return user match {
				case Some(data) => if (data.password == encrypt(password, data.salt) && data.active == 1) { Some(data) } else { None }
				case None => None
			}
		}
	}

	def insert(user : User) : Option[User] = {
		DB.withConnection { implicit connection =>
			val id = SQL("INSERT INTO user (email, name, password, salt, token, active) VALUES ({email}, {name}, {password}, {salt}, {token}, {active})").on(
				"email" -> user.email, "name" -> user.name, "password" -> user.password, "salt" -> user.salt, "token" -> user.token, "active" -> user.active
			).executeInsert();
			if (id.isEmpty == true)
				return None;
			else
				return Some(User(id.get, user.email, user.password, user.salt, user.name, user.active, user.token));
		}
	}

	def update(user : User) : Option[User] = {
		DB.withConnection { implicit connection =>
			SQL("UPDATE user SET email = {email}, name = {name}, password = {password}, token={token}, active={active}").on(
				"email" -> user.email, "name" -> user.name, "password" -> user.password, "salt" -> user.salt, "token" -> user.token, "active" -> user.active
			).executeUpdate();
			return Some(user);
		}
	}

	def save(user : User) : Option[User] = {
		return (if (getById(user.id).isEmpty == true) {
			insert(user);
		} else {
			update(user);
		});
	}

	def encrypt(s : String, salt : String) : String = {
		val md = java.security.MessageDigest.getInstance("SHA-256")
		// add a salt. can replace salt with generated salt value
		val v = salt + s
		// return encoded value 
		new sun.misc.BASE64Encoder().encode(md.digest(v.getBytes("UTF-8")))
	}

	def randomString(length: Int) = {
	    val r = new scala.util.Random
	    val sb = new StringBuilder
	    for (i <- 1 to length) {
	        sb.append(r.nextPrintableChar)
	    }
	    sb.toString
	}
}