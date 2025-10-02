/*
* Copyright 2025 hbz NRW(http://www.hbz-nrw.de/)
*
* Licensed under the Apache License,Version 2.0(the"License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,software
* distributed under the License is distributed on an"AS IS"BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package helper;

import java.io.Closeable;

import org.apache.commons.httpclient.HttpClient;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.CrawlerModel;
import models.Gatherconf;

import models.Node;
import play.Play;

/**
 * a class to implement a wpull crawl
 * 
 * @author Ingolf Kuss
 *
 */
public class BrowsertrixWorkflow extends CrawlerModel {

	/* Browsertrix spezifische Variablen */
	private CloseableHttpClient httpClient = null;
	private HttpPost request = null;
	private HttpResponse response = null;
	private ObjectMapper objectMapper = new ObjectMapper();
	private String bearerToken = null;
	private String btrixWorkflowId = null;

	/*
	 * Authorisierung für Browsertrix
	 */
	final static String btrix_api_url = Play.application().configuration()
			.getString("regal-api.browsertrix.apiUrl");
	final static String btrix_admin_username = Play.application().configuration()
			.getString("regal-api.browsertrix.adminUsername");
	final static String btrix_admin_password = Play.application().configuration()
			.getString("regal-api.browsertrix.adminPassword");
	final static String btrix_org_name = Play.application().configuration()
			.getString("regal-api.browsertrix.orgName");
	final static String btrix_orgid = Play.application().configuration()
			.getString("regal-api.browsertrix.orgId");
	/**
	 * Im Verzeichnis outDir liegen die fertigen Crawls. Von hier aus werden die
	 * Crawls direkt von Wayback indexiert.
	 */
	final static String outDir = Play.application().configuration()
			.getString("regal-api.browsertrix.outDir");

	/**
	 * Konstruktor zu Browsertrix Crawler Workflow
	 * 
	 * @param node der Knoten der Website, zu der ein neuer Crawl gestartet werden
	 *          soll.
	 * @param conf the crawler configuration for the website
	 */
	public BrowsertrixWorkflow(Node node, Gatherconf conf) {
		super(node, conf);
		try {
			/*
			 * Wenn es noch keine Worfkflow ID in der conf gibt, wird jetzt eine
			 * angelegt.
			 */
			if (conf.getBtrixWorkflowId() == null) {
				getBearerToken();
				postCrawlerConfig();
			}
		} catch (Exception e) {
			WebgatherLogger.error("Browsertrix-Workflow für PID " + node.getPid()
					+ " URL " + conf.getUrl() + " kann nicht angelegt werden !");
			throw new RuntimeException(e);
		}
	}

	private void getBearerToken() {
		try {
			httpClient = HttpClients.createDefault();
			request = new HttpPost(btrix_api_url + "/auth/jwt/login");
			WebgatherLogger.debug("btrix_api_url " + btrix_api_url);
			WebgatherLogger.debug("btrix_admin_username " + btrix_admin_username);
			WebgatherLogger.debug("btrix_admin_password " + btrix_admin_password);
			request.addHeader("Content-Type", "application/x-www-form-urlencoded");
			request.setEntity(new StringEntity("username=" + btrix_admin_username
					+ "&password=" + btrix_admin_password + "&grant_type=password"));
			request.addHeader("Accept", "application/json");
			response = httpClient.execute(request);
			if (response.getStatusLine().getStatusCode() == 200) {
				String tokenResponseJson = EntityUtils.toString(response.getEntity());
				JsonNode tokenJsonNode = objectMapper.readTree(tokenResponseJson);
				this.bearerToken = tokenJsonNode.get("access_token").asText();
				WebgatherLogger.debug("Got bearer Token " + this.bearerToken);
			} else {
				throw new RuntimeException("Status-Code von /auth/jwt/login: "
						+ response.getStatusLine().getStatusCode());
			}
		} catch (Exception e) {
			msg = "Bearer-Token für Browsertrix-Workflow für PID " + node.getPid()
					+ " kann nicht geholt werden!";
			WebgatherLogger.error(msg, e.toString());
			throw new RuntimeException(e);
		} finally {
			try {
				httpClient.close();
				((Closeable) response).close();
			} catch (Exception e) {
				WebgatherLogger.warn("httpClient kann nicht geschlossen werden.",
						e.toString());
			}
		}
	}

	private void postCrawlerConfig() {
		try {
			httpClient = HttpClientBuilder.create().build();
			request = new HttpPost(
					btrix_api_url + "/orgs/" + btrix_orgid + "/crawlconfigs");
			WebgatherLogger.debug("btrix_api_url " + btrix_api_url);
			WebgatherLogger.debug("btrix_orgid " + btrix_orgid);
			request.addHeader("Authorization", "Bearer " + this.bearerToken);
			request.addHeader("Content-Type", "application/json");
			JSONObject data = new JSONObject();
			data.put("name", "Bergischer Verein für Familienkunde");
			data.put("inactive", false);
			data.put("description", "");
			// Und jetzt eine Config aufbauen:
			JSONObject config = new JSONObject();
			JSONObject seed = new JSONObject();
			seed.put("url", "https://www.bvff.de/");
			JSONArray seeds = new JSONArray();
			seeds.put(seed);
			config.put("seeds", seeds);
			config.put("depth", -1);
			data.put("config", config);
			WebgatherLogger.debug("data.toString()=" + data.toString());
			StringEntity se = new StringEntity(data.toString());
			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			request.setEntity(se);
			request.addHeader("Accept", "application/json");
			response = httpClient.execute(request);
			if (response.getStatusLine().getStatusCode() == 200) {
				String responseJson = EntityUtils.toString(response.getEntity());
				WebgatherLogger.debug("received response: " + responseJson);
				// JSON ausparsen
				JsonNode responseJsonNode = objectMapper.readTree(responseJson);
				this.btrixWorkflowId = responseJsonNode.get("id").asText();
				WebgatherLogger.debug("Crawler Config angelegt mit btrix_workflow_id: "
						+ btrixWorkflowId);
				conf.setBtrixWorkflowId(btrixWorkflowId);
				// hier: Update (Modify) der Gatherconf wie beim "Save"-Button
			} else {
				throw new RuntimeException("Status-Code von /orgs/" + btrix_orgid
						+ "/crawlconfigs : " + response.getStatusLine().getStatusCode());
			}
		} catch (Exception e) {
			msg = "Browsertrix Crawler Config für PID " + node.getPid()
					+ " kann nicht gesendet werden!";
			WebgatherLogger.error(msg, e.toString());
			throw new RuntimeException(e);
		} finally {
			try {
				httpClient.close();
				((Closeable) response).close();
			} catch (Exception e) {
				WebgatherLogger.warn("httpClient kann nicht geschlossen werden.",
						e.toString());
			}
		}
	}

	/**
	 * Ruft den CDN-Gatherer für diese Website auf, anschließend Browsertrix für
	 * den Hauptcrawl
	 */
	@Override
	public void startJob() {
		super.startJob();

		try {

			// Bereite Kommando für den Hauptcrawl vor

		} catch (Exception e) {
			WebgatherLogger.error(e.toString());
			throw new RuntimeException("Browsertrix crawl not successfully started!",
					e);
		}
	}

}
