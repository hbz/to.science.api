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
			}
			UserDB users = models.Globals.users;
			if (users.isValid(userForm.get().getUsername(),
					userForm.get().getPassword())) {
				User user = users.getUser(userForm.get().getUsername());
				play.Logger.debug(userForm.get().getUsername() + "");
				String uri = ctx().session().get("CURRENT_PAGE");
				session().clear();
				session("username", user.getUsername());
				session("role", user.getRole().toString());
				flash("message",
						"Hello " + user.getUsername() + "! You are logged in.");
				if (uri != null)
					return redirect(uri);

				return redirect(Application.index().toString());
			}
			return badRequest(views.html.login.render(userForm));

		});
	}
}
