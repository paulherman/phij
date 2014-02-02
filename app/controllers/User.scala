package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

object User extends Controller with Secured {

	def view(id :Long) = IsUser { email => implicit request =>
		Ok(views.html.problemView(models.Problem.getId(id)));
	}

	def submissionPid(pid : Long) = IsUser { user => implicit request =>
		Ok(views.html.userSubmission(models.Eval.getByUidPid(user.id, pid)));
	}

	def submission() = IsUser { user => implicit request =>
		Ok(views.html.userSubmission(models.Eval.getByUid(user.id)));
	}

	def list = IsUser { email => implicit request =>
		Ok(views.html.problemList(models.Problem.getAll));
	}
	def leaderboard() = IsUser { user => implicit request =>
		Ok(views.html.userLeaderboard(models.User.getTopByScore()));
	}
}
