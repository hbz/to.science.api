package controllers;

import helper.HttpArchiveException;
import play.libs.F.Promise;
import play.mvc.Result;
import views.Helper;

public class Viewers extends MyController {

	public static Promise<Result> getViewer(String type, String pid) {
		return new ReadDataAction().call(pid, node -> {
			try {
				String url = "/resource/" + pid + "/data";
				String mimetype = node.getMimeType();
				if ("video".equals(type)) {
					return ok(views.html.mediaViewers.standardViewer
							.render(Helper.getViewerInfo(node)));
				}
				if ("audio".equals(type)) {
					return ok(views.html.mediaViewers.standardViewer
							.render(Helper.getViewerInfo(node)));
				}
				if ("deepzoom".equals(type)) {
					return ok(
							views.html.mediaViewers.deepzoomViewer.render(node.getPid()));
				}
				return redirect(routes.Resource.listData(pid));
			} catch (Exception e) {
				throw new HttpArchiveException(500, e);
			}
		});
	}
}
