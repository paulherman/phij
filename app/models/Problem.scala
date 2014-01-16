package models

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._

case class Problem (
	id: Int,
	name: String,
	description: String,
	statement: String,
	submissions: Int,
	solved: Int,
	tags: String
)

object Problem {
	def getAll : List[Problem] = {
		DB.withConnection { implicit connection =>
			val problemQuery = SQL("SELECT p.id, p.name, p.description, p.statement, p.tags FROM problem p");
			problemQuery().map(row => {
				Problem(row[Int]("id"), row[String]("name"), row[String]("description"), row[String]("statement"), 0, 0, row[String]("tags"))
			}).toList
		}
	}

	def getId(id :Long) : Problem = {
		DB.withConnection { implicit connection =>
			val problemQuery = SQL("SELECT p.id, p.name, p.description, p.statement, p.tags FROM problem p WHERE id={id}").on("id" -> id);
			val row = problemQuery().head
			Problem(row[Int]("id"), row[String]("name"), row[String]("description"), row[String]("statement"), 0, 0, row[String]("tags"))
		}
	}
}
