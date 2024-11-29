package controllers;

import authenticate.BasicAuth;
import controllers.MyController.BulkActionAccessor;
import models.Globals;
import net.sf.ehcache.pool.sizeof.annotations.IgnoreSizeOf;
import play.libs.F.Promise;
import play.mvc.Result;

import play.cache.Cache;

@IgnoreSizeOf
public class Context extends MyController {

	public static Promise<Result> getContext() {
		return Promise.promise(() -> {
			return createResultWithJsonContext();
		});
	}

	private static Result createResultWithJsonContext() {
		if (Cache.get("getContext") == null) {
			Cache.set("getContext", json(Globals.profile.getContext()));
		}
		setJsonHeader();
		try {
			return ok((String) Cache.get("getContext"));
		} catch (Exception e) {
			play.Logger.error("", e);
			return internalServerError("Not able to create response!");
		}

	}

	@BasicAuth
	public static Promise<Result> updateContext() {
		return new BulkActionAccessor().call((userId) -> {
			Globals.profile.updateLabels(null);
			return createResultWithJsonContext();
		});
	}

}
