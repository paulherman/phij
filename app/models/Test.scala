package models

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import play.Logger
import play.Logger._

case class Test (
	id: Long,
	pid: Long,
	answer: Float,
	variables: String,
	score: Int
)

object Test {

	def parseRow(data : SqlRow) : Test = {
		val test : Test = Test(data[Long]("id"), data[Long]("pid"), data[String]("answer").toFloat, data[String]("variables"), data[Int]("score"));
		Logger.info("Test fetch: " + test.toString);
		return test;
	}

	def getById(id : Long) : Option[Test] = {
		DB.withConnection { implicit connection =>
			val query = SQL("SELECT * FROM tests WHERE id={id}").on("id" -> id).apply().headOption;
			return query match {
				case Some(data) => Some(parseRow(data))
				case None => None
			}
		}
	}

	def getByPid(pid : Long) : List[Test] = {
		DB.withConnection { implicit connection =>
			val query = SQL("SELECT * FROM tests WHERE pid={pid}").on("pid" -> pid);
			query().map(row => parseRow(row)).toList
		}
	}
}
