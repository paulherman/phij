package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.ws.WS
import play.api.libs.concurrent.Execution.Implicits._
import play.Logger
import play.Logger._
import scala.concurrent.duration._
import scala.concurrent.Await

object Problem extends Controller with Secured {
	case class Submission(value: String)

	val problemForm = Form(
		mapping(
    		"value" -> text
    	)(Submission.apply)(Submission.unapply)
	)

	def evaluate(expr : String, vars : String) : Option[Float] = {
  		val future = WS.url("http://phijeval.herokuapp.com").post(Map("appKey" -> Seq("e5ee871ea55fada67f16b1ddf89087aa997822a1"), "json" -> Seq("{\"expression\": \"" + expr + "\", \"args\": " + vars + "}")));
		val response = Await.result(future, 10 seconds);
		if (response.body.length > 0)
			return Some(response.body.toFloat);
		return None;
	}

	def saveAnswer(user : models.User, pid : Long, test : models.Test, solved : Boolean) : Option[models.Eval] = {
		val score : Int = if (solved) test.score else 0;
		models.Eval.save(user.id, test.id, pid, score);
	}

	def checkAnswer(expr : String, test : models.Test) : Boolean = {
		val ans = evaluate(expr, test.variables);
		if (ans.isDefined)
			return Math.abs(ans.get - test.answer) < 0.001;
		return false;
	}

	def submit(id : Long) = IsUser { user => implicit request =>
		val expr = problemForm.bindFromRequest().get.value;
		val tests : List[models.Test] = models.Test.getByPid(id);
		tests.map(x => saveAnswer(user, id, x, checkAnswer(expr, x)));
		Redirect(routes.User.submissionPid(id));
	}

	def view(id :Long) = IsUser { email => implicit request =>
		Ok(views.html.problemView(models.Problem.getId(id)));
	}

	def list = IsUser { email => implicit request =>
		Ok(views.html.problemList(models.Problem.getAll));
	}
}