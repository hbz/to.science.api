package base;

import java.util.function.Supplier;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import play.libs.F;
import play.mvc.Http;
import play.mvc.Http.RequestBuilder;
import play.mvc.Result;
import play.test.FakeApplication;
import play.test.Helpers;

import com.fasterxml.jackson.databind.JsonNode;

@SuppressWarnings("javadoc")
public class BaseModelTest {
	public static FakeApplication app;

	@BeforeClass
	public static void startApp() {
		app = Helpers.fakeApplication(Helpers.inMemoryDatabase());
		Helpers.start(app);
	}

	@AfterClass
	public static void stopApp() {
		Helpers.stop(app);
	}

	protected Result controllerCall(Supplier<F.Promise<Result>> function,
			JsonNode body) {
		Result result = new Helpers().invokeWithContext(new RequestBuilder()
				.bodyJson(body).header("accept", "application/json"), () -> {
					Http.Context.current.get().args.put("role", "edoweb-admin");
					return function.get();
				}).get(5000);
		String str = Helpers.contentAsString(result);
		play.Logger.debug(str);
		return result;
	}

	protected Result controllerCall(Supplier<F.Promise<Result>> function) {
		Result result = new Helpers().invokeWithContext(
				new RequestBuilder().header("accept", "application/json"), () -> {
					Http.Context.current.get().args.put("role", "edoweb-admin");
					return function.get();
				}).get(10000);
		String str = Helpers.contentAsString(result);
		play.Logger.debug(str);
		return result;
	}
}
