package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.Logger
import play.Logger._

object Application extends Controller with Secured {
	var currentUser : Option[models.User] = None

	val redirectLogin = Redirect(routes.Problem.list)

	val loginForm = Form(
	    tuple(
	    	"email" -> email,
	    	"password" -> text
	    ) verifying ("Invalid email or password.", result => result match {
	    	case (email, password) => models.User.authenticate(email, password).isDefined
	    })
	)

	val signupForm = Form(
			tuple(
				"name" -> nonEmptyText,
				"email" -> email,
				"password" -> text(minLength = 6, maxLength = 20),
				"vpassword" -> text(minLength = 6, maxLength = 20)
			) verifying ("Passwords do not match.", result => result match {
				case (name, email, password, vpassword) => password == vpassword
			}) verifying ("Email address already exists.", result => result match {
				case (name, email, password, vpassword) => !models.User.getByEmail(email).isDefined
			})
		)
	
	def token(token : String) = Action { implicit request =>
		if (currentUser.isDefined == false)
		{
			currentUser = models.User.getByToken(token)
			if (currentUser.isDefined)
		   		redirectLogin.withSession("userEmail" -> currentUser.get.email)
		   	else
		   		redirectLogin
		}
	   	else
	   		redirectLogin
	}

	def login() = Action { implicit request =>
		if (currentUser.isDefined == false)
	   		Ok(views.html.login(loginForm))
	   	else
	   		redirectLogin
	}

	def authenticate() = Action { implicit request =>
		if (currentUser.isDefined == false)
			loginForm.bindFromRequest.fold(
	    		formWithErrors => BadRequest(views.html.login(formWithErrors)),
	    	 	user => {currentUser = models.User.authenticate(user._1, user._2); redirectLogin.withSession("userEmail" -> user._1) }
	    	)
	   	else
	   		redirectLogin
	}

	def logout() = IsAuthenticated { email => implicit request =>
		currentUser = None	
		Redirect(routes.Application.login).withNewSession.flashing(
	    	"message" -> "You have logged out."
	    )
	}

	def signup() = Action { implicit request =>
		if (currentUser.isDefined == false)
			Ok(views.html.signup(signupForm));
	   	else
	   		redirectLogin
	}

	def signupDo() = Action { implicit request =>
		if (currentUser.isDefined == false)
		{
			val salt = models.User.randomString(32);
			signupForm.bindFromRequest.fold(
	    		formWithErrors => BadRequest(views.html.signup(formWithErrors)),
	    	 	user => {
	    	 		//TODO: confirmation email
	    	 		models.User.save(models.User(0, user._2, models.User.encrypt(user._3, salt), salt, user._1, 1, models.User.encrypt(user._2, models.User.randomString(32))));
	    	 		Redirect(routes.Application.login).flashing(
	    	 			"message" -> "You have successfully signed up. Please confirm your email address (not needed at the moment)."
	    	 		)
	    	 	}
	    	)
	    }
	   	else
	   		redirectLogin
	}
}

trait Secured {
	private def email(request: RequestHeader) = request.session.get("userEmail")

	private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Application.login)

	def IsAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(email, onUnauthorized) { user =>
    	Action(request => f(user)(request))
	}

	def IsUser(f: => models.User => Request[AnyContent] => Result) = Security.Authenticated(email, onUnauthorized) { user =>
    	Action(request => f(models.User.getByEmail(user).get)(request))
	}
}