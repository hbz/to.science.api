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

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static archive.fedora.Vocabulary.*;
import actions.Modify;
import models.CrawlerModel;
import models.Gatherconf;
import models.Gatherconf.CrawlSubdomains;
import models.Gatherconf.QuotaUnitSelection;

import models.Node;
import play.Play;

/**
 * a class to implement a wpull crawl
 * 
 * @author Ingolf Kuss
 *
 */
public class BtrixWebclient extends CrawlerModel {

	/* Browsertrix spezifische Variablen */
	private CloseableHttpClient httpClient = null;
	private HttpPost request = null;
	private CloseableHttpResponse response = null;
	private ObjectMapper objectMapper = new ObjectMapper();
	private String bearerToken = null;
	private String scopeType = null;
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
	public BtrixWebclient(Node node, Gatherconf conf) {
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
			// WebgatherLogger.debug("btrix_admin_password " + btrix_admin_password);
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
					btrix_api_url + "/orgs/" + btrix_orgid + "/crawlconfigs/");
			WebgatherLogger.debug("btrix_api_url " + btrix_api_url);
			WebgatherLogger.debug("btrix_orgid " + btrix_orgid);
			request.addHeader("Authorization", "Bearer " + this.bearerToken);
			request.addHeader("Content-Type", "application/json");
			String jsonBody = createJsonBody();
			WebgatherLogger.debug("jsonBody=" + jsonBody);
			request.setEntity(new StringEntity(jsonBody, "UTF-8"));
			request.addHeader("Accept", "application/json");
			response = httpClient.execute(request);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				String responseJson = EntityUtils.toString(response.getEntity());
				WebgatherLogger.debug("received response: " + responseJson);
				// JSON ausparsen
				JSONObject responseJsonObject = new JSONObject(responseJson);
				this.btrixWorkflowId = responseJsonObject.getString("id");
				// JsonNode responseJsonNode = objectMapper.readTree(responseJson);
				// this.btrixWorkflowId = responseJsonNode.get("id").asText();
				WebgatherLogger.debug("Crawler Config angelegt mit btrix_workflow_id: "
						+ btrixWorkflowId);
				conf.setBtrixWorkflowId(btrixWorkflowId);
				msg = new Modify().updateConf(node, conf.toString());
				WebgatherLogger.info(msg);
			} else {
				String errorBody = EntityUtils.toString(response.getEntity());
				throw new RuntimeException("Status-Code von /orgs/" + btrix_orgid
						+ "/crawlconfigs : " + statusCode + ". Fehler-Body: " + errorBody);
			}
		} catch (Exception e) {
			msg = "Browsertrix Crawler Config für PID " + node.getPid()
					+ " kann nicht gesendet werden!";
			WebgatherLogger.error(msg, e.getMessage());
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

	private String createJsonBody() {
		JSONObject data = new JSONObject();
		try {
			// Name oder Titel der Site
			data.put("name", conf.getName());
			String md = node.getMetadata(toscience);
			if (md != null) {
				JSONObject jo = new JSONObject(md);
				if (jo.has("title")) {
					// Hole Titel aus den toscience-Metadaten
					data.put("name", jo.getJSONArray("title").get(0).toString());
				}
			}
			data.put("inactive", !conf.isActive());
			data.put("description", conf.getNotices());
			// maximale Crawlgröße in Byte
			data.put("maxCrawlSize", conf.getMaxCrawlSize());
			if (conf.getMaxCrawlSize() > 0) {
				switch (conf.getQuotaUnitSelection()) {
				case KB:
					data.put("maxCrawlSize", conf.getMaxCrawlSize() * 1000);
					break;
				case MB:
					data.put("maxCrawlSize", conf.getMaxCrawlSize() * 1000000);
					break;
				case GB:
					data.put("maxCrawlSize", conf.getMaxCrawlSize() * 1000000000);
					break;
				default:
					// standardmäßig wird Kilobyte angenommen
					data.put("maxCrawlSize", conf.getMaxCrawlSize() * 1000);
					break;
				}
			}
			// Und jetzt eine Config aufbauen:
			JSONObject config = new JSONObject();
			switch (conf.getCrawlSubdomains()) {
			case hostnames:
				this.scopeType = "host";
				break;
			case domains:
				this.scopeType = "domain";
				break;
			default:
				// standardmäßig wird die Domain ohne Subdomains eingesammelt
				this.scopeType = "host";
				break;
			}
			JSONArray seeds = new JSONArray();
			JSONObject seed =
					createSeed(this.urlAscii, this.scopeType, conf.getDeepness());
			seeds.put(seed);
			/*
			 * zu inkludierende (zusätzliche) Domains. Diese erhalten jeweils ein
			 * eigenes "Seed" und zusätzlich einen Eintrag im Array "include".
			 */
			JSONArray include = new JSONArray();
			for (String domain : conf.getDomains()) {
				include.put(domain);
				seeds.put(createSeed(domain, this.scopeType, conf.getDeepness()));
			}
			config.put("seeds", seeds);
			config.put("scopeType", this.scopeType);
			config.put("include", include);
			/*
			 * Exclusions = auszuschließende Bereiche
			 */
			JSONArray exclude = new JSONArray();
			for (String urlExcluded : conf.getUrlsExcluded()) {
				exclude.put(urlExcluded);
			}
			config.put("exclude", exclude);
			config.put("depth", conf.getDeepness());
			config.put("extraHops", 1);
			config.put("lang", "de");
			config.put("blockAds", true);
			// Limits amount of time to wait for a page to load; in Sekunden
			// nimm default Wert
			// config.put("pageLoadTimeout", 120);
			// Delay Before Next Page; in Sekunden
			config.put("pageExtraDelay", conf.getWaitSecBtRequests());
			config.put("useSitemap", true);
			config.put("userAgent",
					Gatherconf.agentTable.get(conf.getAgentIdSelection()));
			data.put("config", config);
		} catch (JSONException e) {
			msg = "Crawlerconf JSON (JsonBody) für PID " + node.getPid()
					+ " kann nicht gebaut werden!";
			WebgatherLogger.error(msg, e.getMessage());
		}
		return data.toString();
	}

	private JSONObject createSeed(String url, String seedScopeType, int depth) {
		JSONObject seed = new JSONObject();
		try {
			seed.put("url", url);
			seed.put("scopeType", seedScopeType);
			seed.put("depth", depth);
			/* one hop out -- the crawler will visit pages one link away. */
			seed.put("extraHops", 1);
		} catch (JSONException e) {
			msg = "Seed with url " + url + " could not be created!";
			WebgatherLogger.warn(msg, e.getMessage());
		}
		return seed;
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
