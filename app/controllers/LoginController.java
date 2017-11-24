package controllers;

import authenticate.User;
import authenticate.UserDB;
import play.data.Form;
import play.libs.F.Promise;
import play.mvc.Result;

public class LoginController extends MyController {

	public static Promise<Result> postLogin() {
		return Promise.promise(() -> {
			Form<User> userForm = Form.form(User.class).bindFromRequest();
			if (userForm.hasErrors()) {
				play.Logger.debug("Login credentials not valid.");
				flash("message", "Login credentials not valid.");
				return badRequest(views.html.login.render(userForm));
			} else {
				UserDB users = models.Globals.users;
				if (users.isValid(userForm.get().getUsername(),
						userForm.get().getPassword())) {
					User user = users.getUser(userForm.get().getUsername());
					play.Logger.debug(userForm.get().getUsername() + "");
					session().clear();
					session("username", user.getUsername());
					session("role", user.getRole().toString());
					flash("message",
							"Hello " + user.getUsername() + "! You are logged in.");
					return redirect(routes.Application.index());
				}
				return badRequest(views.html.login.render(userForm));
			}
		});
	}
}
