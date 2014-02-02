package models

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import play.Logger
import play.Logger._

case class Eval (
	id : Long,
	uid : Long,
	tid : Long,
	pid : Long,
	score : Int
);

object Eval {
	def parseRow(data : SqlRow) : Eval = {
		val eval : Eval = Eval(data[Long]("id"), data[Long]("uid"), data[Long]("tid"), data[Long]("pid"), data[Int]("passed"));
		Logger.info("Eval fetch: " + eval.toString);
		return eval;
	}

	def save(uid : Long, tid : Long, pid : Long, score : Int) : Option[Eval] = {
		DB.withConnection { implicit connection =>
			val id = SQL("INSERT INTO eval (uid, tid, pid, passed) VALUES ({uid}, {tid}, {pid}, {score})").on(
				"uid" -> uid, "tid" -> tid, "pid" -> pid, "score" -> score
			).executeInsert();
			if (id.isEmpty == true)
				return None;
			else
				return Some(Eval(id.get, uid, tid, pid, score));
		}
	}

	def getByUid(uid : Long) : List[Eval] = {
		DB.withConnection { implicit connection =>
			val query = SQL("SELECT * FROM eval WHERE uid={uid} ORDER BY id DESC").on("uid" -> uid);
			query().map(row => parseRow(row)).toList
		}
	}

	def getByUidPid(uid : Long, pid : Long) : List[Eval] = {
		DB.withConnection { implicit connection =>
			val query = SQL("SELECT * FROM eval WHERE uid={uid} AND pid={pid} ORDER BY id DESC").on("uid" -> uid, "pid" -> pid);
			query().map(row => parseRow(row)).toList
		}
	}

	def getByPid(pid : Long) : List[Eval] = {
		DB.withConnection { implicit connection =>
			val query = SQL("SELECT * FROM eval WHERE pid={pid} ORDER BY id DESC").on("pid" -> pid);
			query().map(row => parseRow(row)).toList
		}
	}
}