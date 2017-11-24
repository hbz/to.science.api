package controllers;

import java.util.HashMap;
import java.util.Map;

import actions.Modify;
import authenticate.BasicAuth;
import authenticate.User;
import authenticate.UserDB;
import helper.HttpArchiveException;
import helper.JsonMapper;
import models.Message;
import models.Node;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.F.Promise;
import play.mvc.Result;

@BasicAuth
public class Forms extends MyController {

	public static Promise<Result> getCatalogForm() {
		return new CreateAction().call((userId) -> {
			try {
				DynamicForm form = Form.form();
				return ok(views.html.catalogForm.render(form));
			} catch (Exception e) {
				throw new HttpArchiveException(500, e);
			}
		});
	}

	public static Promise<Result> postCatalogForm() {
		return new CreateAction().call((userId) -> {
			try {
				DynamicForm form = Form.form().bindFromRequest();
				String alephId = form.get("alephId");
				Node previewNode = new Node();
				previewNode.setPid("preview:1");
				String metadata =
						new Modify().getLobid2DataAsNtripleString(previewNode, alephId);
				previewNode.setMetadata2(metadata);
				flash("message",
						"Preview! Press 'Create' buttom on the page bottom to create new object.");
				return ok(views.html.catalogPreview.render(previewNode.getLd2(), null,
						"edoweb", alephId));

			} catch (Exception e) {
				throw new HttpArchiveException(500, e);
			}
		});
	}

	public static Promise<Result> getArticleForm() {
		return new CreateAction().call((userId) -> {
			try {
				return JsonMessage(new Message(
						"Here the user will be able add bibligraphic data for articles.",
						501));
			} catch (Exception e) {
				throw new HttpArchiveException(500, e);
			}
		});
	}

	public static Promise<Result> getWebpageForm() {
		return new CreateAction().call((userId) -> {
			try {
				return JsonMessage(new Message(
						"Here the user will be able to Link a HT-Number to the Form in order to import Aleph data and to configure a webpage gathering.",
						501));
			} catch (Exception e) {
				throw new HttpArchiveException(500, e);
			}
		});
	}

	public static Promise<Result> getPartForm() {
		return new CreateAction().call((userId) -> {
			try {
				return JsonMessage(new Message(
						"Here the user will be able to create a new part Form.", 501));
			} catch (Exception e) {
				throw new HttpArchiveException(500, e);
			}
		});
	}

	public static Promise<Result> getFileForm() {
		return new CreateAction().call((userId) -> {
			try {
				return JsonMessage(new Message(
						"Here the user will be able to upload a new file.", 501));
			} catch (Exception e) {
				throw new HttpArchiveException(500, e);
			}
		});
	}

	public static Promise<Result> getLoginForm() {
		return Promise.promise(() -> {
			try {
				if (models.Globals.users.isLoggedIn(ctx())) {
					return redirect(routes.Forms.getLogoutForm());
				}
				Form<User> userForm = Form.form(User.class);
				return ok(views.html.login.render(userForm));
			} catch (Exception e) {
				throw new HttpArchiveException(500, e);
			}
		});
	}

	public static Promise<Result> getLogoutForm() {
		try {
			return Promise.promise(() -> {
				return ok(views.html.logout.render());
			});
		} catch (Exception e) {
			throw new HttpArchiveException(500, e);
		}
	}

	public static Promise<Result> postLogout() {
		return new CreateAction().call((userId) -> {

			flash("message", "Goodby " + session().get("username")
					+ ". You were successfully logged out");
			session().clear();
			return redirect(routes.Application.index());
		});
	}

}
