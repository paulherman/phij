package controllers

import play.api._
import play.api.mvc._

object Problem extends Controller {

	def view(id :Long) = Action {
		Ok(views.html.problemView(models.Problem.getId(id)));
	}

	def list = Action { implicit request =>
		Ok(views.html.problemList(models.Problem.getAll));
	}
}
