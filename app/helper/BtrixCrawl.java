package helper;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;

import play.Logger;

/**
 * Eine Java-Klasse vom Typ Thread, in der der Browsertrix-Hauptcrawl angestoßen
 * wird. Ruft den Hauptcrawl in Browsertrix auf.
 */
public class BtrixCrawl extends Thread {

	private CloseableHttpClient httpClient = null;
	private HttpRequestBase request = null;
	private CloseableHttpResponse response = null;
	private BtrixWebclient btrixWebclient = null;

	/**
	 * ein Logger für das Webgathering
	 */
	protected static final Logger.ALogger WebgatherLogger =
			Logger.of("webgatherer");

	/**
	 * Der Konstruktor für BtrixCrawl Thread
	 * 
	 * @param model eine Instanz von Browsertrix-Webclient
	 */
	public BtrixCrawl(BtrixWebclient model) {
		this.btrixWebclient = model;
	}

	/**
	 * This methods starts a Webcrawl in Browsertrix
	 */
	@Override
	public void run() {

		try {
			httpClient = HttpClientBuilder.create().build();
			request = new HttpPost(btrixWebclient.getBtrixApiUrl() + "/orgs/"
					+ btrixWebclient.getBtrixOrgId() + "/crawlconfigs/"
					+ btrixWebclient.getBtrixWorkflowId() + "/run");
			request.addHeader("Authorization",
					"Bearer " + btrixWebclient.exportBearerToken());
			request.addHeader("Content-Type", "application/json");
			WebgatherLogger.debug("request=" + request.toString());
			request.addHeader("Accept", "application/json");
			response = httpClient.execute(request);
			String responseJson = btrixWebclient.getResponseJson(response);
			WebgatherLogger.debug("received response: " + responseJson);
			// JSON ausparsen
			JSONObject responseJsonObject = new JSONObject(responseJson);
			String started = responseJsonObject.getString("started");
			WebgatherLogger.debug("Crawl zu Workflow "
					+ btrixWebclient.getBtrixWorkflowId() + " gestartet: " + started);
		} catch (Exception e) {
			btrixWebclient.setMsg("Browsertrix Crawl für Workflow "
					+ btrixWebclient.getBtrixWorkflowId() + ", PID "
					+ btrixWebclient.getNode().getPid()
					+ " kann nicht gestartet werden!");
			WebgatherLogger.error(btrixWebclient.getMsg(), e.getMessage());
			throw new RuntimeException(e);
		} finally {
			try {
				httpClient.close();
				response.close();
			} catch (Exception e) {
				WebgatherLogger.warn("httpClient kann nicht geschlossen werden.",
						e.toString());
			}
		}
	}

}
